package de.fraunhofer.isst.configmanager.connector.dataspaceconnector.util;

import de.fraunhofer.iais.eis.AppRoute;
import de.fraunhofer.iais.eis.ConnectorEndpoint;
import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.iais.eis.GenericEndpoint;
import org.apache.commons.codec.binary.Base64;
import org.apache.velocity.VelocityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Utility class for configuring Camel routes for the Dataspace Connector.
 */
@Component
public class DataspaceConnectorRouteConfigurer {

    private static String dataSpaceConnectorApiUsername;

    private static String dataSpaceConnectorApiPassword;

    @Value("${dataspace.connector.api.username}")
    public void setDataSpaceConnectorApiUsername(String username) {
        dataSpaceConnectorApiUsername = username;
    }

    @Value("${dataspace.connector.api.password}")
    public void setDataSpaceConnectorApiPassword(String password) {
        dataSpaceConnectorApiPassword = password;
    }

    private static final ResourceLoader resourceLoader = new DefaultResourceLoader();

    private DataspaceConnectorRouteConfigurer() {}

    /**
     * Adds basic authentication information for the Dataspace Connector to the Velocity context
     * for creating a Camel XML route to be used with the Dataspace Connector.
     *
     * @param velocityContext the context containing the values to insert into the route template
     */
    public static void addBasicAuthToContext(VelocityContext velocityContext) {
        String auth = dataSpaceConnectorApiUsername + ":" + dataSpaceConnectorApiPassword;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);
        velocityContext.put("connectorAuthHeader", authHeader);
    }

    /**
     * Chooses and returns the route template for the Dataspace Connector based on the app route.
     *
     * @param appRoute the app route
     * @return the route template
     */
    public static Resource getRouteTemplate(AppRoute appRoute) {
        ArrayList<? extends Endpoint> routeStart = appRoute.getAppRouteStart();

        Resource resource;
        if (routeStart.get(0) instanceof GenericEndpoint) {
            resource = resourceLoader.getResource("classpath:camel-templates/dataspaceconnector/http_to_connector_template.vm");
        } else if (routeStart.get(0) instanceof ConnectorEndpoint) {
            resource = resourceLoader.getResource("classpath:camel-templates/dataspaceconnector/connector_to_http_template.vm");
        } else {
            resource = null;
        }

        return resource;
    }

}
