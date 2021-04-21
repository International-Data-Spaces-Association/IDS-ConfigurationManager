package de.fraunhofer.isst.configmanager.util;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.isst.configmanager.model.config.CustomBroker;
import de.fraunhofer.isst.configmanager.model.customapp.CustomApp;
import de.fraunhofer.isst.configmanager.model.customapp.CustomAppEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TestUtil {

    public static CustomBroker createCustomBroker() {
        CustomBroker customBroker = new CustomBroker();
        customBroker.setBrokerUri(URI.create("https://example.com"));
        customBroker.setTitle("CustomBroker");
        return customBroker;
    }

    /**
     * @return list of brokers
     */
    public static List<CustomBroker> brokers() {

        List<CustomBroker> brokers = new ArrayList<>();
        brokers.add(createCustomBroker());
        return brokers;
    }

    /**
     * @return list of custom apps
     */
    public static List<CustomApp> apps() {
        final List<CustomApp> customApps = new ArrayList<>();
        final var customApp = new CustomApp();
        customApp.setTitle("Custom App One");

        final List<CustomAppEndpoint> customAppEndpoints = new ArrayList<>();

        try {
            final var appEndpoint = Utility.createAppEndpoint(AppEndpointType.INPUT_ENDPOINT,
                    new BigInteger("80"), "documentation", "information",
                    "http://app1", "iPath", "oPath",
                    Language.DE, "PDF", "path");

            final var customAppEndpoint = new CustomAppEndpoint(appEndpoint);


            final var appEndpoint2 = Utility.createAppEndpoint(AppEndpointType.OUTPUT_ENDPOINT,
                    new BigInteger("81"),
                    "documentation", "information", "http://app2",
                    "iPath", "oPath", Language.DE, "JSON", "path");
            final var customAppEndpoint2 = new CustomAppEndpoint(appEndpoint2);

            customAppEndpoints.add(customAppEndpoint);
            customAppEndpoints.add(customAppEndpoint2);
        } catch (URISyntaxException e) {
            log.error(e.getMessage());
        }
        customApp.setAppEndpointList(customAppEndpoints);
        customApps.add(customApp);
        return customApps;
    }

    /**
     * @return configuration model
     */
    public static ConfigurationModel configurationModel() {
        final var connector = new BaseConnectorBuilder()
                ._inboundModelVersion_(new ArrayList<>(List.of("3.1.0")))
                ._outboundModelVersion_("3.1.0")
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._maintainer_(URI.create("https://example.com"))
                ._curator_(URI.create("https://example.com"))
                .build();

        return new ConfigurationModelBuilder()
                ._configurationModelLogLevel_(LogLevel.NO_LOGGING)
                ._connectorDescription_(connector)
                ._connectorStatus_(ConnectorStatus.CONNECTOR_ONLINE)
                ._connectorDeployMode_(ConnectorDeployMode.TEST_DEPLOYMENT)
                ._trustStore_(URI.create("http://trustStore"))
                ._trustStorePassword_("password")
                ._keyStore_(URI.create("http://keyStore"))
                ._keyStorePassword_("password")
                .build();
    }
}
