package de.fraunhofer.isst.configmanager.util.camel;

import de.fraunhofer.iais.eis.AppRoute;
import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.ConnectorEndpoint;
import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.iais.eis.GenericEndpoint;
import de.fraunhofer.iais.eis.RouteStep;
import de.fraunhofer.isst.configmanager.connector.trustedconnector.TrustedConnectorRouteConfigurer;
import de.fraunhofer.isst.configmanager.connector.dataspaceconnector.util.DataspaceConnectorRouteConfigurer;
import org.apache.commons.codec.binary.Base64;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating Camel routes from AppRoutes.
 */
@Component
public class RouteManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteManager.class);

    @Value("${dataspace.connector.enabled}")
    private boolean dataspaceConnectorEnabled;

    private final RouteHttpHelper routeHttpHelper;

    private final RouteFileHelper routeFileHelper;

    @Autowired
    public RouteManager(final RouteHttpHelper routeHttpHelper,
                        final RouteFileHelper routeFileHelper) {
        this.routeHttpHelper = routeHttpHelper;
        this.routeFileHelper = routeFileHelper;
    }

    /**
     * Creates a Camel XML route from a given app route for either the Dataspace Connector or the
     * Trusted Connector. If the Configuration Manager is currently managing a Dataspace Connector,
     * the generated XML route will be sent to the Camel application. If the Configuration
     * Manager is currently managing a Trusted Connector, the generated XML route will be written
     * to a file in the designated directory. Both the Camel application and the directory are
     * specified in application.properties.
     *
     * @param configurationModel config model the app route belongs to; contains key- and truststore
     *                           information
     * @param appRoute the app route to create a Camel route for
     */
    public void createAndDeployXMLRoute(ConfigurationModel configurationModel,
                                               AppRoute appRoute) {
        VelocityContext velocityContext = new VelocityContext();

        //create ID for Camel route
        String camelRouteId = getCamelRouteId(appRoute);
        velocityContext.put("routeId", camelRouteId);

        //get route start and end (will either be connector, app or generic endpoint)
        addRouteStartToContext(velocityContext, appRoute.getAppRouteStart());
        addRouteEndToContext(velocityContext, appRoute.getAppRouteEnd());

        //get route steps (if any)
        addRouteStepsToContext(velocityContext, appRoute.getHasSubRoute());

        if (dataspaceConnectorEnabled) {
            createDataspaceConnectorRoute(appRoute, velocityContext);
        } else {
            createTrustedConnectorRoute(appRoute, velocityContext, configurationModel,
                    camelRouteId);
        }
    }

    /**
     * Extracts the URL of the {@link AppRoute}'s start and adds it to the Velocity context.
     *
     * @param velocityContext the Velocity context
     * @param routeStart start of the AppRoute
     */
    private void addRouteStartToContext(VelocityContext velocityContext,
                                               ArrayList<? extends Endpoint> routeStart) {
        if (routeStart.get(0) instanceof ConnectorEndpoint) {
            ConnectorEndpoint connectorEndpoint = (ConnectorEndpoint) routeStart.get(0);
            velocityContext.put("startUrl", connectorEndpoint.getAccessURL().toString());
        } else if (routeStart.get(0) instanceof GenericEndpoint) {
            GenericEndpoint genericEndpoint = (GenericEndpoint) routeStart.get(0);
            velocityContext.put("startUrl", genericEndpoint.getAccessURL().toString());
            addBasicAuthHeaderForGenericEndpoint(velocityContext, genericEndpoint);
        } else {
            //app is route start
        }
    }

    /**
     * Extracts the URL of the {@link AppRoute}'s end and adds it to the Velocity context.
     *
     * @param velocityContext the Velocity context
     * @param routeEnd end of the AppRoute
     */
    private void addRouteEndToContext(VelocityContext velocityContext,
                                             ArrayList<? extends Endpoint> routeEnd) {
        if (routeEnd.get(0) instanceof ConnectorEndpoint) {
            ConnectorEndpoint connectorEndpoint = (ConnectorEndpoint) routeEnd.get(0);
            velocityContext.put("endUrl", connectorEndpoint.getAccessURL().toString());
        } else if (routeEnd.get(0) instanceof GenericEndpoint) {
            GenericEndpoint genericEndpoint = (GenericEndpoint) routeEnd.get(0);
            velocityContext.put("endUrl", genericEndpoint.getAccessURL().toString());
            addBasicAuthHeaderForGenericEndpoint(velocityContext, genericEndpoint);
        } else {
            //app is route end
        }
    }

    /**
     * Creates and adds the basic authentication header for calling a generic endpoint to a Velocity
     * context, if basic authentication is defined for the given endpoint.
     * @param velocityContext the Velocity context
     * @param genericEndpoint the generic endpoint
     */
    private void addBasicAuthHeaderForGenericEndpoint(VelocityContext velocityContext,
                                                             GenericEndpoint genericEndpoint) {
        if (genericEndpoint.getGenericEndpointAuthentication() != null) {
            String username = genericEndpoint.getGenericEndpointAuthentication().getAuthUsername();
            String password = genericEndpoint.getGenericEndpointAuthentication().getAuthPassword();
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes());
            String authHeader = "Basic " + new String(encodedAuth);
            velocityContext.put("genericEndpointAuthHeader", authHeader);
        }
    }

    /**
     * Extracts the URLs of the {@link AppRoute}'s steps and adds them to the Velocity context.
     *
     * @param velocityContext the Velocity context
     * @param routeSteps steps of the AppRoute
     */
    private void addRouteStepsToContext(VelocityContext velocityContext,
                                               ArrayList<? extends RouteStep> routeSteps) {
        List<String> routeStepUrls = new ArrayList<>();
        if (routeSteps != null) {
            for (RouteStep routeStep: routeSteps) {
                routeStepUrls.add(routeStep.getAppRouteStart().get(0).getAccessURL().toString());
            }
        }
        velocityContext.put("routeStepUrls", routeStepUrls);
    }

    /**
     * Creates and deploys a Camel route for the Dataspace Connector. First, Dataspace Connector
     * specific configuration is added to the Velocity Context, which should already contain
     * general route information. Then, the correct route template for the given AppRoute object
     * is chosen from the Dataspace Connector templates. Last, the generated XML route is sent to
     * the Camel application defined in application.properties.
     *
     * @param appRoute the AppRoute object
     * @param velocityContext the Velocity context
     */
    private void createDataspaceConnectorRoute(AppRoute appRoute,
                                                      VelocityContext velocityContext) {
        LOGGER.debug("Creating route for Dataspace Connector...");

        //add basic auth header for connector endpoint
        DataspaceConnectorRouteConfigurer.addBasicAuthToContext(velocityContext);

        //choose correct XML template based on route
        Resource template = DataspaceConnectorRouteConfigurer.getRouteTemplate(appRoute);

        if (template != null) {
            VelocityEngine velocityEngine = new VelocityEngine();
            velocityEngine.init();

            //populate route template with properties from velocity context to create route
            StringWriter writer = populateTemplate(template, velocityEngine, velocityContext);

            //send the generated route (XML) to Camel via HTTP
            routeHttpHelper.sendRouteFileToCamelApplication(writer.toString());
        } else {
            LOGGER.warn("Template is null. Unable to create XML route file for AppRoute"
                    + " with ID '{}'", appRoute.getId());
        }
    }

    /**
     * Creates and deploys a Camel route for the Trusted Connector. First, Trusted Connector
     * specific configuration is added to the Velocity Context, which should already contain
     * general route information. Then, the correct route template for the given AppRoute
     * object is chosen from the Trusted Connector templates. Last, the generated XML route is
     * written to the directory defined in application.properties.
     *
     * @param appRoute the AppRoute object
     * @param velocityContext the Velocity context
     * @param configurationModel the Configuration Model containing key- and truststore passwords
     *                           required for the Trusted Connector's SSL configuration
     * @param camelRouteId ID of the Camel route, which is used as the file name
     */
    private void createTrustedConnectorRoute(AppRoute appRoute,
                                                    VelocityContext velocityContext,
                                                    ConfigurationModel configurationModel,
                                                    String camelRouteId) {
        LOGGER.debug("Creating route for Trusted Connector...");

        //add SSL configuration for connector endpoint
        TrustedConnectorRouteConfigurer.addSslConfig(velocityContext, configurationModel);

        //choose correct XML template based on route
        Resource template = TrustedConnectorRouteConfigurer.getRouteTemplate(appRoute);

        if (template != null) {
            VelocityEngine velocityEngine = new VelocityEngine();
            velocityEngine.init();

            //populate route template with properties from velocity context to create route
            StringWriter writer = populateTemplate(template, velocityEngine, velocityContext);

            //write the generated route (XML) to a file in the designated directory
            routeFileHelper.writeToFile(camelRouteId + ".xml", writer.toString());
        } else {
            LOGGER.warn("Template is null. Unable to create XML route file for AppRoute"
                    + " with ID '{}'", appRoute.getId());
        }
    }

    /**
     * Populates a given Velocity template using the values from a given Velocity context.
     *
     * @param resource the template
     * @param velocityEngine the Velocity engine required for populating the template
     * @param velocityContext the context containing the values to insert into the template
     * @return the populated template as a string
     */
    private StringWriter populateTemplate (Resource resource, VelocityEngine velocityEngine,
                                                  VelocityContext velocityContext)  {
        StringWriter stringWriter = new StringWriter();
        InputStreamReader inputStreamReader;

        try {
            inputStreamReader = new InputStreamReader(resource.getInputStream());
            velocityEngine.evaluate(velocityContext, stringWriter, "", inputStreamReader);
        } catch (Exception e) {
            String camelRouteId = (String) velocityContext.get("routeId");
            LOGGER.error("An error occurred while populating template. Please check all respective "
                    + "files for connection with ID '{}' for correctness! (Error message: {})",
                    camelRouteId, e.toString());
            e.printStackTrace();
        }

        return stringWriter;
    }

    /**
     * Deletes all Camel routes associated with app routes from a given config model by calling
     * {@link RouteManager#deleteRoute(AppRoute)}.
     *
     * @param configurationModel the config model
     */
    public void deleteRouteFiles(ConfigurationModel configurationModel) {
        for (AppRoute appRoute: configurationModel.getAppRoute()) {
            deleteRoute(appRoute);
        }
    }

    /**
     * Deletes the Camel route for a given {@link AppRoute}. If the Configuration Manager is
     * currently managing a Dataspace Connector, the route is deleted at the Camel application. If
     * the Configuration Manager is currently managing a Trusted Connector, the route file is
     * removed from the designated directory.
     *
     * @param appRoute the AppRoute
     */
    public void deleteRoute(AppRoute appRoute) {
        String camelRouteId = getCamelRouteId(appRoute);

        if (dataspaceConnectorEnabled) {
            routeHttpHelper.deleteRouteAtCamelApplication(camelRouteId);
        } else {
            routeFileHelper.deleteFile(camelRouteId + ".xml");
        }
    }

    /**
     * Generated the ID of the Camel route for a given {@link AppRoute}. The Camel route ID consists
     * of the String 'app-route_' followed by the UUID from the AppRoute's ID.
     *
     * @param appRoute the AppRoute
     * @return the Camel route ID
     */
    private String getCamelRouteId(AppRoute appRoute) {
        String appRouteId = appRoute.getId().toString()
                .split("/")[appRoute.getId().toString().split("/").length - 1];
        return "app-route_" + appRouteId;
    }

}
