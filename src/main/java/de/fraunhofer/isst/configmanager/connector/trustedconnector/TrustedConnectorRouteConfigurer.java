package de.fraunhofer.isst.configmanager.connector.trustedconnector;

import de.fraunhofer.iais.eis.AppRoute;
import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.ConnectorEndpoint;
import de.fraunhofer.iais.eis.GenericEndpoint;
import org.apache.velocity.VelocityContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.net.URI;

/**
 * Utility class for configuring Camel routes for the TrustedConnector.
 */
public final class TrustedConnectorRouteConfigurer {

    /**
     * ResourceLoader for loading Camel route templates from the classpath.
     */
    private static final ResourceLoader RESOURCE_LOADER = new DefaultResourceLoader();

    private TrustedConnectorRouteConfigurer() {}

    /**
     * Adds key- and truststore information to the Velocity context for creating a Camel XML route to be used with
     * the Trusted Connector.
     *
     * @param velocityContext the context containing the values to insert into the route template
     * @param configurationModel the config model containing key- and truststore information
     */
    public static void addSslConfig(final VelocityContext velocityContext, final ConfigurationModel configurationModel) {
        velocityContext.put("keyStorePath", removeFileScheme(configurationModel.getKeyStore()));
        velocityContext.put("keyStorePassword", configurationModel.getKeyStorePassword());
        velocityContext.put("trustStorePath", removeFileScheme(configurationModel.getTrustStore()));
        velocityContext.put("trustStorePassword", configurationModel.getTrustStorePassword());
    }

    /**
     * Chooses and returns the route template for the Trusted Connector based on the app route.
     *
     * @param appRoute the app route
     * @return the route template
     */
    public static Resource getRouteTemplate(final AppRoute appRoute) {
        final var routeStart = appRoute.getAppRouteStart();

        Resource resource;
        if (routeStart.get(0) instanceof GenericEndpoint) {
            resource = RESOURCE_LOADER.getResource("classpath:camel-templates/trustedconnector/idscp2_client_template_1.vm");
        } else if (routeStart.get(0) instanceof ConnectorEndpoint) {
            resource = RESOURCE_LOADER.getResource("classpath:camel-templates/trustedconnector/idscp2_server_template_1.vm");
        } else {
            resource = null;
        }

        return resource;
    }

    /**
     * Removes the file scheme from an URI, if it is specified.
     *
     * @param uri the URI
     * @return the URI as a string with the file scheme removed, if it was present.
     */
    private static String removeFileScheme(final URI uri) {
        var string = uri.toString();

        if (string.startsWith("file://")) {
            string = string.substring(7);
        } else if (string.startsWith("file:")) {
            string = string.substring(5);
        }

        return string;
    }

}
