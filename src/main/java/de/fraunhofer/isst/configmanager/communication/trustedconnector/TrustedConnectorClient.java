package de.fraunhofer.isst.configmanager.communication.trustedconnector;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model.ResourceRepresentation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;

@Service
@ConditionalOnExpression("${dataspace.connector.enabled:false} == false")
public class TrustedConnectorClient implements DefaultConnectorClient {
    @Override
    public String updateAtBroker(String brokerURI) throws IOException {
        return null;
    }

    @Override
    public String unregisterAtBroker(String brokerURI) throws IOException {
        return null;
    }

    @Override
    public boolean sendConfiguration(String configurationModel) throws IOException {
        return false;
    }

    @Override
    public ConfigurationModel getConfiguration() throws IOException {
        return new ConfigurationModelBuilder()
                ._configurationModelLogLevel_(LogLevel.NO_LOGGING)
                ._connectorStatus_(ConnectorStatus.CONNECTOR_ONLINE)
                ._connectorDeployMode_(ConnectorDeployMode.TEST_DEPLOYMENT)
                ._keyStorePassword_("password")
                ._keyStore_(URI.create("file://cert-stores/keystore.p12"))
                ._trustStorePassword_("password")
                ._trustStore_(URI.create("file://cert-stores/truststore.p12"))
                .build();
    }

    @Override
    public String updateResource(URI resourceID, Resource resource) throws IOException {
        return null;
    }

    @Override
    public String updateResourceAtBroker(URI resourceID, Resource resource, String brokerUri) throws IOException {
        return null;
    }

    @Override
    public String registerResource(Resource resource) throws IOException {
        return null;
    }

    @Override
    public String deleteResource(URI resourceID) throws IOException {
        return null;
    }

    @Override
    public String deleteResourceAtBroker(URI resourceID, String brokerUri) throws IOException {
        return null;
    }

    @Override
    public String deleteResourceRepresentation(String resourceID, String representationID) throws IOException {
        return null;
    }

    @Override
    public String registerResourceRepresentation(String resourceID, Representation representation) throws IOException {
        return null;
    }

    @Override
    public String updateResourceRepresentation(String resourceID, String representationID, Representation representation)
            throws IOException {
        return null;
    }

    @Override
    public String updateCustomResourceRepresentation(String resourceID, String representationID,
                                                     ResourceRepresentation resourceRepresentation) throws IOException {
        return null;
    }

    @Override
    public String updateResourceContract(String resourceID, Contract contract) throws IOException {
        return null;
    }

    @Override
    public void notifyConfig(ConfigurationModel configurationModel) {

    }
}
