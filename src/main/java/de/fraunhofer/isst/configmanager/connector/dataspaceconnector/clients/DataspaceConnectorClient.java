package de.fraunhofer.isst.configmanager.connector.dataspaceconnector.clients;

import de.fraunhofer.iais.eis.BaseConnector;
import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.connector.dataspaceconnector.util.DispatchRequest;
import de.fraunhofer.isst.configmanager.connector.dataspaceconnector.util.ResourceMapper;
import de.fraunhofer.isst.configmanager.model.config.QueryInput;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

/**
 * An implementation of the interface DefaultConnectorClient for the DataspaceConnector.
 */
@Slf4j
@Service
@ConditionalOnExpression("${dataspace.connector.enabled:false}")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataspaceConnectorClient extends AbstractDataspaceConnectorClient implements DefaultConnectorClient {

    public DataspaceConnectorClient(final ResourceMapper dataSpaceConnectorResourceMapper) {
        super(dataSpaceConnectorResourceMapper);
    }

    @Override
    public void getConnectorStatus() throws IOException {
        final var builder = getRequestBuilder();
        builder.url(connectorBaseUrl);
        builder.get();
        final var request = builder.build();
        DispatchRequest.sendToDataspaceConnector(request);
    }

    @Override
    public ConfigurationModel getConfiguration() throws IOException {
        final var connectorUrl = connectorBaseUrl + "admin/api/configuration";

        final var builder = getRequestBuilder();
        builder.header("Authorization",
                Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        builder.url(connectorUrl);
        builder.get();

        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient getConfiguration] Could not get ConfigurationModel from {} with user {}. Response: {} - {}",
                    connectorUrl,
                    dataSpaceConnectorApiUsername,
                    response.code(),
                    response.message());
        }

        final var body = Objects.requireNonNull(response.body()).string();

        ConfigurationModel configurationModel = null;
        try {
            configurationModel = SERIALIZER.deserialize(body, ConfigurationModel.class);
        } catch (IOException e) {
            log.error("---- [DataspaceConnectorClient getConfiguration] SERIALIZER.deserialize threw IOException");
            log.error(e.getMessage(), e);
        }

        return configurationModel;
    }

    @Override
    public BaseConnector getSelfDeclaration() throws IOException {
        final var connectorUrl = connectorBaseUrl + "admin/api/connector";

        final var builder = getRequestBuilder();
        builder.header("Authorization",
                Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        builder.url(connectorUrl);
        builder.get();

        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient getSelfDeclaration] Could not get BaseConnector");
        }

        final var body = Objects.requireNonNull(response.body()).string();

        BaseConnector baseConnector = null;
        try {
            baseConnector = SERIALIZER.deserialize(body, BaseConnector.class);
        } catch (IOException e) {
            log.error("---- [DataspaceConnectorClient getSelfDeclaration] SERIALIZER.deserialize threw IOException");
            log.error(e.getMessage(), e);
        }

        return baseConnector;
    }

    @Override
    public boolean sendConfiguration(final String configurationModel) throws IOException {
        log.info(String.format("---- [DataspaceConnectorClient sendConfiguration] sending new configuration to %s", dataSpaceConnectorHost));

        final var builder = getRequestBuilder();
        builder.url(connectorBaseUrl + "admin/api/configuration");
        builder.post(RequestBody.create(
                configurationModel,
                okhttp3.MediaType.parse("application/ld+json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));

        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);
        var success = true;

        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient sendConfiguration] Updating ConfigurationModel failed!");
            success = false;
        }

        return success;
    }

    @Override
    public BaseConnector getBaseConnector(final String accessURL, final String resourceId) throws IOException {
        final var builder = getRequestBuilder();
        final var urlBuilder = new HttpUrl.Builder()
                .scheme(protocol)
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/request/description")
                .addQueryParameter("recipient", accessURL);

        if (resourceId != null && !resourceId.isBlank()) {
            urlBuilder.addQueryParameter("requestedResource", resourceId);
        }

        final var url = urlBuilder.build();
        builder.url(url);
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        builder.post(RequestBody.create(new byte[0], null));

        log.info("---- [DataspaceConnectorClient getBaseConnector] " + url);
        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient getBaseConnector] Could not get BaseConnector Info!");
        }
        final var body = Objects.requireNonNull(response.body()).string();

        BaseConnector baseConnector = null;
        try {
            baseConnector = SERIALIZER.deserialize(body, BaseConnector.class);
        } catch (IOException e) {
            log.error("---- [DataspaceConnectorClient getBaseConnector] SERIALIZER.deserialize threw IOException");
            log.error(e.getMessage(), e);
        }

        return baseConnector;
    }

    @Override
    public String requestContractAgreement(final String recipientId,
                                           final String requestedArtifactId,
                                           final String contractOffer) throws IOException {
        log.info("---- [DataspaceConnectorClient requestContractAgreement] Request contract agreement with recipient: {} and artifact: {}", recipientId, requestedArtifactId);

        final var builder = getRequestBuilder();
        final var urlBuilder = new HttpUrl.Builder()
                .scheme(protocol)
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/request/contract")
                .addQueryParameter("recipient", recipientId)
                .addQueryParameter("requestedArtifact", requestedArtifactId);

        final var url = urlBuilder.build();
        builder.url(url);
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));

        if (contractOffer != null && !contractOffer.isBlank()) {
            builder.post(RequestBody.create(contractOffer, okhttp3.MediaType.parse("application/ld+json")));
        } else {
            builder.post(RequestBody.create(new byte[0], null));
        }

        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient requestContractAgreement] Could not request contract agreement");
        }

        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public Response requestData(String recipientId, String requestedArtifactId, String contractId,
                                String key, QueryInput queryInput) throws IOException {

        log.info("---- [DataspaceConnectorClient requestData] Request Data with recipient: {}, artifact: {}," +
                " contract: {}, key: {} and queryInput: {} ", recipientId, requestedArtifactId, contractId, key, queryInput);

        final var builder = getRequestBuilder();
        final var urlBuilder = new HttpUrl.Builder()
                .scheme(protocol)
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/request/artifact")
                .addQueryParameter("recipient", recipientId)
                .addQueryParameter("requestedArtifact", requestedArtifactId)
                .addQueryParameter("key", key);

        if (contractId != null && !contractId.isBlank()) {
            urlBuilder.addQueryParameter("transferContract", contractId);
        }

        final var url = urlBuilder.build();
        builder.url(url);
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));

        if (queryInput != null) {
            String query = MAPPER.writeValueAsString(queryInput);
            builder.post(RequestBody.create(query, okhttp3.MediaType.parse("application/json")));
        } else {
            builder.post(RequestBody.create(new byte[0], null));
        }

        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient requestData] Could not request data");
        }
        return response;
    }

    @Override
    public String getPolicyPattern(final String policy) throws IOException {
        log.info("---- [DataspaceConnectorClient getPolicyPattern] Get pattern for policy");

        final var builder = getRequestBuilder();
        builder.url(connectorBaseUrl + "/admin/api/example/policy-validation");
        builder.post(RequestBody.create(policy, okhttp3.MediaType.parse("application/ld+json")));
        builder.header("Authorization",
                Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));

        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient getPolicyPattern] Pattern for policy could not be determined");
        }

        return Objects.requireNonNull(response.body()).string();
    }
}
