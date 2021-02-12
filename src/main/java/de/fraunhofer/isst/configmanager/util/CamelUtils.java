package de.fraunhofer.isst.configmanager.util;

import de.fraunhofer.iais.eis.AppRoute;
import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.ConnectorEndpoint;
import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.iais.eis.GenericEndpoint;
import de.fraunhofer.iais.eis.RouteStep;
import de.fraunhofer.isst.configmanager.communication.trustedconnector.TrustedConnectorRouteConfigurer;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating Camel routes from AppRoutes.
 */
@Component
public class CamelUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelUtils.class);

    private static String filePath;

    private static boolean dataspaceConnectorEnabled;

    private CamelUtils() {}

    //cannot inject values into static variables directly
    @Value("${camel.xml-routes.directory}")
    public void setFilePath(String value) {
        filePath = value;
    }

    @Value("${dataspace.connector.enabled}")
    public void setDataspaceConnectorEnabled(boolean value) {
        dataspaceConnectorEnabled = value;
    }

    /**
     * Creates a Camel XML route from a given app route and writes it to a file. Currently this works only for the
     * Trusted Connector as the files are written to a designated directory for deployment.
     *
     * @param configurationModel config model the app route belongs to; contains key- and truststore information
     * @param appRoute the app route to create a Camel route for
     */
    public static void generateXMLRoute (ConfigurationModel configurationModel, AppRoute appRoute) {
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.init();
        VelocityContext velocityContext = new VelocityContext();

        String appRouteUuid = appRoute.getId().toString()
                .split("/")[appRoute.getId().toString().split("/").length - 1];
        String connectionId = "app-route_" + appRouteUuid;
        velocityContext.put("connectionId", connectionId);

        //get start and endpoint (will either be connector, app or generic endpoint)
        //if start is generic -> outgoing connection, if start is connector -> incoming connection

        ArrayList<? extends Endpoint> routeStart = appRoute.getAppRouteStart();
        if (routeStart.get(0) instanceof ConnectorEndpoint) {
            ConnectorEndpoint connectorEndpoint = (ConnectorEndpoint) routeStart.get(0);
            velocityContext.put("startUrl", removeUriScheme(connectorEndpoint.getAccessURL()));
        } else if (routeStart.get(0) instanceof GenericEndpoint) {
            GenericEndpoint genericEndpoint = (GenericEndpoint) routeStart.get(0);
            velocityContext.put("startUrl", genericEndpoint.getAccessURL().toString());
        } else {
            //app is route start
        }

        ArrayList<? extends Endpoint> routeEnd = appRoute.getAppRouteEnd();
        if (routeEnd.get(0) instanceof ConnectorEndpoint) {
            ConnectorEndpoint connectorEndpoint = (ConnectorEndpoint) routeEnd.get(0);
            velocityContext.put("endUrl", removeUriScheme(connectorEndpoint.getAccessURL()));
        } else if (routeEnd.get(0) instanceof GenericEndpoint) {
            GenericEndpoint genericEndpoint = (GenericEndpoint) routeEnd.get(0);
            velocityContext.put("endUrl", genericEndpoint.getAccessURL().toString());
        } else {
            //app is route end
        }

        //get route steps (if any)
        List<String> routeStepUrls = new ArrayList<>();
        for (RouteStep routeStep: appRoute.getHasSubRoute()) {
            routeStepUrls.add(routeStep.getAppRouteStart().get(0).getAccessURL().toString());
        }
        velocityContext.put("routeStepUrls", routeStepUrls);

        Resource template;
        if (dataspaceConnectorEnabled) {
            LOGGER.debug("Creating route for Dataspace Connector.");
            template = null;
        } else {
            LOGGER.debug("Creating route for Trusted Connector.");
            TrustedConnectorRouteConfigurer.addSslConfig(velocityContext, configurationModel);
            template = TrustedConnectorRouteConfigurer.getRouteTemplate(appRoute);
        }

        if (template != null) {
            //populate route template with properties from velocity context to create route
            StringWriter writer = populateTemplate(template, velocityEngine, velocityContext);

            //write the generated route (XML) to a file in the designated directory
            writeToFile(filePath, connectionId + ".xml", writer.toString());
        } else {
            LOGGER.warn("Template is null. Unable to create XML route file for AppRoute with ID '{}'", appRoute.getId());
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
    private static StringWriter populateTemplate (Resource resource, VelocityEngine velocityEngine,
                                                  VelocityContext velocityContext)  {
        StringWriter stringWriter = new StringWriter();
        InputStreamReader inputStreamReader;

        try {
            inputStreamReader = new InputStreamReader(resource.getInputStream());
            velocityEngine.evaluate(velocityContext, stringWriter, "", inputStreamReader);
        } catch (Exception e) {
            String id = (String) velocityContext.get("connectionId");
            LOGGER.error("An error occurred while populating template. Please check all respective files for connection"
                    + " with ID '{}' for correctness! (Error message: {})", id, e.toString());
            e.printStackTrace();
        }

        return stringWriter;
    }

    /**
     * Writes a given string to a file. Creates the file if it does not exist.
     * @param path the path to the file
     * @param fileName the filename
     * @param content the content to write to the file
     */
    private static void writeToFile(String path, String fileName, String content) {
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;

        try {
            File file = new File(path + File.separator +  fileName);
            if (!file.exists() && !file.createNewFile()) {
                LOGGER.error("Could not create file '{}{}{}'", path, File.separator, fileName);
            }
            fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(content);

            LOGGER.info("Successfully created file '{}{}{}'.", path, File.separator, fileName);
        } catch (IOException e) {
            LOGGER.error("Cannot write to file '{}{}{}' because an IO error occurred: {}",
                    path, File.separator, fileName, e.toString());
            e.printStackTrace();
        } finally {
            try {
                if (bufferedWriter != null)
                    bufferedWriter.close();
                if (fileWriter != null)
                    fileWriter.close();
            } catch (IOException e) {
                LOGGER.error("Error closing a writer: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Deletes all Camel route files associated with app routes from a given config model by calling
     * {@link CamelUtils#deleteRouteFile(AppRoute)}.
     *
     * @param configurationModel the config model
     */
    public static void deleteRouteFiles(ConfigurationModel configurationModel) {
        for (AppRoute appRoute: configurationModel.getAppRoute()) {
            deleteRouteFile(appRoute);
        }
    }

    /**
     * Deletes the Camel route file associated with a given app route.
     *
     * @param appRoute the app route
     */
    public static void deleteRouteFile(AppRoute appRoute) {
        String appRouteId = appRoute.getId().toString()
                .split("/")[appRoute.getId().toString().split("/").length - 1];
        String connectionId = "app-route_" + appRouteId;

        deleteFile(filePath, connectionId + ".xml");
    }

    /**
     * Deletes a file at a given location.
     *
     * @param path the path to the file
     * @param name the filename
     */
    private static void deleteFile (String path, String name) {
        Path file = Paths.get(path + name);
        if (Files.exists(file)) {
            try {
                Files.delete(file);
                LOGGER.info("Successfully deleted file '{}{}{}'.", path, File.separator, name);
            } catch (NoSuchFileException e) {
                LOGGER.error("Cannot delete file '{}{}{}' because file does not exist.", path, File.separator, name, e);
                e.printStackTrace();
            } catch (IOException e) {
                LOGGER.error("Cannot delete file '{}{}{}' because an IO error occurred.", path, File.separator, name, e);
                e.printStackTrace();
            }
        }
    }

    private static String removeUriScheme(URI uri) {
        String string = uri.toString();

        if (string.split("//").length > 1) {
            string = string.split("//")[1];
        }

        return string;
    }

}
