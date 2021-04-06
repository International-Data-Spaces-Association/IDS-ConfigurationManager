package de.fraunhofer.isst.configmanager.connector.dataspaceconnector;

import de.fraunhofer.isst.configmanager.connector.clients.DefaultBrokerClient;
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
import java.net.URI;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnExpression("${dataspace.connector.enabled:false}")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataspaceBrokerClient extends DataspaceClient implements DefaultBrokerClient {

    public DataspaceBrokerClient(ResourceMapper dataSpaceConnectorResourceMapper) {
        super(dataSpaceConnectorResourceMapper);
    }

    @Override
    public Response updateAtBroker(final String brokerURI) throws IOException {
        log.info(String.format(
                "---- [DataspaceConnectorClient updateAtBroker] updating connector %s at broker %s",
                dataSpaceConnectorHost,
                brokerURI));

        final var builder = getRequestBuilder();
        builder.url(new HttpUrl.Builder()
                .scheme(protocol)
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/broker/update")
                .addQueryParameter("broker", brokerURI)
                .build());
        builder.post(RequestBody.create(brokerURI, okhttp3.MediaType.parse("text/html")));
        builder.header("Authorization",
                Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));

        final var request = builder.build();

        return DispatchRequest.sendToDataspaceConnector(request);
    }

    @Override
    public String unregisterAtBroker(final String brokerURI) throws IOException {
        log.info(String.format(
                "---- [DataspaceConnectorClient unregisterAtBroker] unregistering connector %s at broker %s",
                dataSpaceConnectorHost,
                brokerURI));

        final var builder = getRequestBuilder();
        builder.url(new HttpUrl.Builder()
                .scheme(protocol)
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/broker/unregister")
                .addQueryParameter("broker", brokerURI)
                .build());
        builder.post(RequestBody.create(brokerURI, okhttp3.MediaType.parse("text/html")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));

        final var request = builder.build();

        return Objects.requireNonNull(DispatchRequest.sendToDataspaceConnector(request).body()).string();
    }

    @Override
    public String updateResourceAtBroker(final String brokerUri, final URI resourceID) throws IOException {
        log.info(String.format("---- [DataspaceConnectorClient updateResourceAtBroker] updating resource at Broker %s", brokerUri));

        final var path = resourceID.getPath();
        final var idStr = path.substring(path.lastIndexOf('/') + 1);
        final var resourceUUID = UUID.fromString(idStr);

        final var builder = getRequestBuilder();
        builder.url(new HttpUrl.Builder()
                .scheme(protocol)
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/broker/update/" + resourceUUID)
                .addQueryParameter("broker", brokerUri)
                .build());
        builder.post(RequestBody.create(new byte[0], null));
        builder.header("Authorization",
                Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient updateResourceAtBroker] Updating Resource at Broker failed!");
        }

        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public String deleteResourceAtBroker(final String brokerUri, final URI resourceID) throws IOException {
        log.info(String.format("---- [DataspaceConnectorClient deleteResourceAtBroker] Deleting resource %s at Broker %s", resourceID, brokerUri));

        final var path = resourceID.getPath();
        final var idStr = path.substring(path.lastIndexOf('/') + 1);
        final var builder = getRequestBuilder();
        builder.url(new HttpUrl.Builder()
                .scheme(protocol)
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/broker/remove/" + idStr)
                .addQueryParameter("broker", brokerUri)
                .build());
        builder.post(RequestBody.create(new byte[0], null));
        builder.header("Authorization",
                Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));

        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient deleteResourceAtBroker] Deleting Resource at Broker failed!");
        }

        return Objects.requireNonNull(response.body()).string();
    }

}
