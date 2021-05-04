package de.fraunhofer.isst.configmanager.connector.trustedconnector;

import de.fraunhofer.iais.eis.BaseConnector;
import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.ConfigurationModelBuilder;
import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.iais.eis.ConnectorStatus;
import de.fraunhofer.iais.eis.LogLevel;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.data.util.QueryInput;
import okhttp3.Response;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;

@Service
@ConditionalOnExpression("${dataspace.connector.enabled:false} == false")
public class TrustedConnectorClient implements DefaultConnectorClient {

    @Override
    public boolean sendConfiguration(final String configurationModel) throws IOException {
        return false;
    }

    @Override
    public void getConnectorStatus() {

    }

    @Override
    public ConfigurationModel getConfiguration() {
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
    public BaseConnector getBaseConnector(final String accessURL, final String resourceId) throws IOException {
        return null;
    }

    @Override
    public String getPolicyPattern(final String policy) {
        return null;
    }

    @Override
    public BaseConnector getSelfDeclaration() throws IOException {
        return null;
    }

    @Override
    public String requestContractAgreement(final String recipientId,
                                           final String requestedArtifactId,
                                           final String contractOffer) throws IOException {
        return null;
    }

    @Override
    public Response requestData(final String recipientId,
                                final String requestedArtifactId,
                                final String contractId,
                                final String key,
                                final QueryInput queryInput) throws IOException {
        return null;
    }
}
