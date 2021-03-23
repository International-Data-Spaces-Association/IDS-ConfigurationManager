package de.fraunhofer.isst.configmanager.communication.dataspaceconnector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.BaseConnector;
import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model.ResourceRepresentation;
import de.fraunhofer.isst.configmanager.util.OkHttpUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;

/**
 * A prototypical implementation of the interface DefaultConnectorClient for the dataspace
 * connector.
 * This can be used as a guide and can also be used for a specific connector.
 * <p>
 * The special feature here is that requests are sent to the connector via http.
 * The corresponding host, port number and credentials can then be set to establish the connection.
 * In addition it must be considered whether the model e.g. for a resource also corresponds to
 * the model of the
 * information model. If this is not the case, a mapping has to be done, see for example
 * {@link de.fraunhofer.isst.configmanager.communication.dataspaceconnector.DataSpaceConnectorResourceMapper}.
 */
@Slf4j
@Service
@ConditionalOnExpression("${dataspace.connector.enabled:false}")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataspaceConnectorClient implements DefaultConnectorClient {
    static final Serializer SERIALIZER = new Serializer();
    static final ObjectMapper MAPPER = new ObjectMapper();

    transient final OkHttpClient client = OkHttpUtils.getUnsafeOkHttpClient();
    transient final DataSpaceConnectorResourceMapper dataSpaceConnectorResourceMapper;

    @Value("${dataspace.connector.host}")
    transient String dataSpaceConnectorHost;

    @Value("${dataspace.connector.api.username}")
    transient String dataSpaceConnectorApiUsername;

    @Value("${dataspace.connector.api.password}")
    transient String dataSpaceConnectorApiPassword;

    @Value("${dataspace.connector.port}")
    transient Integer dataSpaceConnectorPort;

    public DataspaceConnectorClient(final DataSpaceConnectorResourceMapper dataSpaceConnectorResourceMapper) {
        this.dataSpaceConnectorResourceMapper = dataSpaceConnectorResourceMapper;
    }

    @Override
    public String updateAtBroker(final String brokerURI) throws IOException {
        log.info(String.format("---- [DataspaceConnectorClient updateAtBroker] updating connector %s at broker %s", dataSpaceConnectorHost,
                brokerURI));
        final var builder = new Request.Builder();
        builder.url(new HttpUrl.Builder()
                .scheme("https")
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/broker/update")
                .addQueryParameter("broker", brokerURI)
                .build());
        builder.post(RequestBody.create(brokerURI, okhttp3.MediaType.parse("text/html")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        final var request = builder.build();
        return Objects.requireNonNull(client.newCall(request).execute().body()).string();
    }

    @Override
    public String unregisterAtBroker(final String brokerURI) throws IOException {
        log.info(String.format("---- [DataspaceConnectorClient unregisterAtBroker] unregistering connector %s at broker %s",
                dataSpaceConnectorHost, brokerURI));
        final var builder = new Request.Builder();
        builder.url(new HttpUrl.Builder()
                .scheme("https")
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/broker/unregister")
                .addQueryParameter("broker", brokerURI)
                .build());
        builder.post(RequestBody.create(brokerURI, okhttp3.MediaType.parse("text/html")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        final var request = builder.build();
        return Objects.requireNonNull(client.newCall(request).execute().body()).string();
    }

    @Override
    public ConfigurationModel getConfiguration() throws IOException {
        final var builder = new Request.Builder();
        final var connectorUrl =
                "https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin/api" +
                        "/configuration";
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        builder.url(connectorUrl);
        builder.get();
        final var request = builder.build();
        final var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient getConfiguration] Could not get ConfigurationModel from {} with user {}. Response: {} - " +
                            "{}",
                    connectorUrl, dataSpaceConnectorApiUsername, response.code(),
                    response.message());
        }
        final var body = Objects.requireNonNull(response.body()).string();
        return SERIALIZER.deserialize(body, ConfigurationModel.class);
    }

    @Override
    public BaseConnector getSelfDeclaration() throws IOException {
        final var builder = new Request.Builder();
        final var connectorUrl =
                "https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin/api" +
                        "/connector";
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        builder.url(connectorUrl);
        builder.get();
        final var request = builder.build();
        final var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient getSelfDeclaration] Could not get BaseConnector");
        }

        final var body = Objects.requireNonNull(response.body()).string();
        return SERIALIZER.deserialize(body, BaseConnector.class);
    }

    @Override
    public String getOfferedResourcesAsJsonString() throws IOException {
        final var baseConnectorNode = getJsonNodeOfBaseConnector();
        final var offeredResourceNode = baseConnectorNode.findValue("ids:offeredResource");
        return offeredResourceNode.toString();
    }

    @Override
    public String getRequestedResourcesAsJsonString() throws IOException {
        final var baseConnectorNode = getJsonNodeOfBaseConnector();
        final var requestedResourceNode = baseConnectorNode.findValue("ids:requestedResource");
        return requestedResourceNode.toString();
    }

    private JsonNode getJsonNodeOfBaseConnector() throws IOException {
        final var builder = new Request.Builder();
        final var connectorUrl =
                "https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort +
                        "/admin/api/connector";
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        builder.url(connectorUrl);
        builder.get();
        final var request = builder.build();
        final var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient getJsonNodeOfBaseConnector] Could not get BaseConnector");
        }
        final var body = Objects.requireNonNull(response.body()).string();
        final var mapper = new ObjectMapper();
        return mapper.readTree(body);
    }


    @Override
    public boolean sendConfiguration(final String configurationModel) throws IOException {
        log.info(String.format("---- [DataspaceConnectorClient sendConfiguration] sending new configuration to %s", dataSpaceConnectorHost));
        final var builder = new Request.Builder();
        builder.url("https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin" +
                "/api/configuration");
        builder.post(RequestBody.create(configurationModel, okhttp3.MediaType.parse("application" +
                "/ld+json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        final var request = builder.build();
        final var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn(String.format("---- [DataspaceConnectorClient sendConfiguration] Updating ConfigurationModel at %s failed!",
                    dataSpaceConnectorHost));
            return false;
        }
        return true;
    }

    @Override
    public BaseConnector getBaseConnector(final String accessURL, final String resourceId) throws IOException {
        final var builder = new Request.Builder();
        final var urlBuilder = new HttpUrl.Builder()
                .scheme("https")
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/request/description")
                .addQueryParameter("recipient", accessURL);
        if (resourceId != null && !resourceId.isBlank()) {
            urlBuilder.addQueryParameter("requestedResource", resourceId);
        }
        final var url = urlBuilder.build();
        log.info("---- [DataspaceConnectorClient getBaseConnector] " + url.toString());
        builder.url(url);
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        builder.post(RequestBody.create(null, new byte[0]));
        final var request = builder.build();
        final var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn(String.format("---- [DataspaceConnectorClient getBaseConnector] Could not get BaseConnector from %s!",
                    dataSpaceConnectorHost));
        }
        final var body = Objects.requireNonNull(response.body()).string();
        return SERIALIZER.deserialize(body, BaseConnector.class);
    }

    @Override
    public Resource getResource(String accessURL, String resourceId) throws IOException {
        var builder = new Request.Builder();
        var urlBuilder = new HttpUrl.Builder()
                .scheme("https")
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/request/description")
                .addQueryParameter("recipient", accessURL)
                .addQueryParameter("requestedResource", resourceId);
        var url = urlBuilder.build();
        log.info(url.toString());
        builder.url(url);
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        builder.post(RequestBody.create(null, new byte[0]));
        var request = builder.build();
        var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn(String.format("Could not get BaseConnector from %s!", dataSpaceConnectorHost));
        }
        var body = response.body().string();
        var splitBody = body.split("\n", 2);
        String uuid = splitBody[0].substring(12);
        log.info(uuid);
        String resource = splitBody[1].substring(10);
        log.info(resource);
        return SERIALIZER.deserialize(resource, Resource.class);
    }

    @Override
    public String requestContractAgreement(String recipientId, String requestedArtifactId, String contractOffer) throws IOException {
        log.info("Request contract agreement with recipient: {} and artifact: {}", recipientId, requestedArtifactId);
        var builder = new Request.Builder();
        var urlBuilder = new HttpUrl.Builder()
                .scheme("https")
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/request/contract")
                .addQueryParameter("recipient", recipientId)
                .addQueryParameter("requestedArtifact", requestedArtifactId);
        var url = urlBuilder.build();
        log.info(url.toString());
        builder.url(url);
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        if (contractOffer != null && !contractOffer.isBlank()) {
            builder.post(RequestBody.create(contractOffer, okhttp3.MediaType.parse("application/ld+json")));
        } else {
            builder.post(RequestBody.create(null, new byte[0]));
        }
        var request = builder.build();
        var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn("Could not request contract agreement");
        }
        var body = response.body().string();
        log.info("Response: " + body);
        return body;
    }

    @Override
    public String registerResource(final Resource resource) throws IOException {
        log.info(String.format("---- [DataspaceConnectorClient registerResource] registering resource at %s", dataSpaceConnectorHost));
        final var mappedResource = dataSpaceConnectorResourceMapper.getMetadata(resource);
        final var resourceJsonLD = MAPPER.writeValueAsString(mappedResource);
        log.info("---- [DataspaceConnectorClient registerResource] new resource: " + resourceJsonLD);
        final var builder = new Request.Builder();
        final var path = resource.getId().getPath();
        final var idStr = path.substring(path.lastIndexOf('/') + 1);
        final var url = new HttpUrl.Builder()
                .scheme("https")
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/resources/resource")
                .addQueryParameter("id", idStr)
                .build();
        log.info("---- [DataspaceConnectorClient registerResource] " + url.toString());
        builder.url(url);
        builder.post(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/ld" +
                "+json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        final var request = builder.build();
        final var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn(String.format("---- [DataspaceConnectorClient registerResource] Registering Resource at %s failed!",
                    dataSpaceConnectorHost));
        }
        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public String deleteResource(final URI resourceID) throws IOException {
        log.info(String.format("---- [DataspaceConnectorClient deleteResource] deleting resource %s at %s", resourceID,
                dataSpaceConnectorHost));
        final var path = resourceID.getPath();
        final var idStr = path.substring(path.lastIndexOf('/') + 1);
        final var builder = new Request.Builder();
        builder.url("https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin" +
                "/api/resources/" + idStr);
        builder.delete();
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        final var request = builder.build();
        final var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn(String.format("---- [DataspaceConnectorClient deleteResource] Deleting Resource at %s failed!", dataSpaceConnectorHost));
        }
        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public String updateResourceAtBroker(final String brokerUri, final URI resourceID) throws IOException {
        log.info(String.format("---- [DataspaceConnectorClient updateResourceAtBroker] updating resource at Broker %s", brokerUri));
        final var path = resourceID.getPath();
        final var idStr = path.substring(path.lastIndexOf('/') + 1);
        final var resourceUUID = UUID.fromString(idStr);
        final var builder = new Request.Builder();
        builder.url(new HttpUrl.Builder()
                .scheme("https")
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/broker/update/" + resourceUUID)
                .addQueryParameter("broker", brokerUri)
                .build());
        builder.post(RequestBody.create(null, new byte[0]));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        final var request = builder.build();
        final var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn(String.format("---- [DataspaceConnectorClient updateResourceAtBroker] Updating Resource at Broker %s failed!", brokerUri));
        }
        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public String deleteResourceAtBroker(final String brokerUri, final URI resourceID) throws IOException {
        log.info(String.format("---- [DataspaceConnectorClient deleteResourceAtBroker] deleting resource %s at Broker %s", resourceID, brokerUri));
        final var path = resourceID.getPath();
        final var idStr = path.substring(path.lastIndexOf('/') + 1);
        final var builder = new Request.Builder();
        builder.url(new HttpUrl.Builder()
                .scheme("https")
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/broker/remove/" + idStr)
                .addQueryParameter("broker", brokerUri)
                .build());
        builder.post(RequestBody.create(null, new byte[0]));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        final var request = builder.build();
        final var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn(String.format("---- [DataspaceConnectorClient deleteResourceAtBroker] Deleting Resource at Broker %s failed!", brokerUri));
        }
        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public String deleteResourceRepresentation(final String resourceID,
                                               final String representationID) throws IOException {
        log.info(String.format("---- [DataspaceConnectorClient deleteResourceRepresentation] deleting representation %s from resource %s at %s",
                representationID, resourceID, dataSpaceConnectorHost));
        final var builder = new Request.Builder();
        final var mappedResourceID =
                dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(resourceID));
        final var mappedRepresentationID =
                dataSpaceConnectorResourceMapper.getMappedId(URI.create(representationID));
        dataSpaceConnectorResourceMapper.deleteResourceIDPair(URI.create(representationID));
        builder.url("https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin" +
                "/api/resources/" + mappedResourceID + "/" + mappedRepresentationID);
        builder.delete();
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        final var request = builder.build();
        final var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn(String.format("---- [DataspaceConnectorClient deleteResourceRepresentation] Deleting Representation at %s failed!",
                    dataSpaceConnectorHost));
        }
        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public String registerResourceRepresentation(final String resourceID,
                                                 final Representation representation,
                                                 final String endpointId) throws IOException {
        log.info(String.format("---- [DataspaceConnectorClient registerResourceRepresentation] registering resource at %s", dataSpaceConnectorHost));
        final var mappedRepresentation =
                dataSpaceConnectorResourceMapper.mapRepresentation(representation);
        final var backendSource = dataSpaceConnectorResourceMapper.createBackendSource(endpointId
                , representation);
        mappedRepresentation.setSource(backendSource);
        final var mappedResourceID =
                dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(resourceID));
        final var mappedRepresentationID =
                dataSpaceConnectorResourceMapper.readUUIDFromURI(representation.getId());
        final var resourceJsonLD = MAPPER.writeValueAsString(mappedRepresentation);
        log.info("---- [DataspaceConnectorClient registerResourceRepresentation] mapped representation: " + resourceJsonLD);
        final var builder = new Request.Builder();
        builder.url(new HttpUrl.Builder()
                .scheme("https")
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/resources/" + mappedResourceID + "/representation")
                .addQueryParameter("id", mappedRepresentationID.toString())
                .build());
        builder.post(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/ld" +
                "+json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        final var request = builder.build();
        final var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn(String.format("---- [DataspaceConnectorClient registerResourceRepresentation] Registering Representation at %s failed!",
                    dataSpaceConnectorHost));
        }
        final var body = Objects.requireNonNull(response.body()).string();
        final var uuid = dataSpaceConnectorResourceMapper.createFromResponse(body,
                representation.getId());
        if (uuid == null) {
            log.warn("---- [DataspaceConnectorClient registerResourceRepresentation] Could not parse ID from response!");
        } else {
            log.info("---- [DataspaceConnectorClient registerResourceRepresentation] UUID is : " + uuid);
        }
        return body;
    }

    @Override
    public String updateResourceRepresentation(final String resourceID,
                                               final String representationID,
                                               final Representation representation,
                                               final String endpointId) throws IOException {
        log.info(String.format("---- [DataspaceConnectorClient updateResourceRepresentation] updating representation %s for resource %s at %s",
                representationID, resourceID, dataSpaceConnectorHost));
        final var mappedResourceID =
                dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(resourceID));
        final var mappedRepresentationID =
                dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(representationID));
        final var mappedRepresentation =
                dataSpaceConnectorResourceMapper.mapRepresentation(representation);
        final var backendSource = dataSpaceConnectorResourceMapper.createBackendSource(endpointId
                , representation);
        mappedRepresentation.setSource(backendSource);
        final var resourceJsonLD = MAPPER.writeValueAsString(mappedRepresentation);
        log.info("---- [DataspaceConnectorClient updateResourceRepresentation] mapped representation: " + resourceJsonLD);
        final var builder = new Request.Builder();
        log.info("---- [DataspaceConnectorClient updateResourceRepresentation] Calling DSC at: https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin/api/resources/" + mappedResourceID + "/" + mappedRepresentationID);
        builder.url("https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin" +
                "/api/resources/" + mappedResourceID + "/" + mappedRepresentationID);
        builder.put(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/ld" +
                "+json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        final var request = builder.build();
        final var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn(String.format("---- [DataspaceConnectorClient updateResourceRepresentation] Updating Representation at %s failed!",
                    dataSpaceConnectorHost));
        }
        final var body = Objects.requireNonNull(response.body()).string();
        log.info("---- [DataspaceConnectorClient updateResourceRepresentation] Response: " + body);
        return body;
    }

    @Override
    public String updateCustomResourceRepresentation(final String resourceID,
                                                     final String representationID,
                                                     final ResourceRepresentation resourceRepresentation) throws IOException {
        log.info(String.format("---- [DataspaceConnectorClient updateCustomResourceRepresentation] updating representation %s for resource %s at %s",
                representationID, resourceID, dataSpaceConnectorHost));
        final var mappedResourceID =
                dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(resourceID));
        final var mappedRepresentationID =
                dataSpaceConnectorResourceMapper.getMappedId(URI.create(representationID));
        final var resourceJsonLD = MAPPER.writeValueAsString(resourceRepresentation);
        log.info("---- [DataspaceConnectorClient updateCustomResourceRepresentation] mapped representation: " + resourceJsonLD);
        final var builder = new Request.Builder();
        builder.url("https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin" +
                "/api/resources/" + mappedResourceID + "/" + mappedRepresentationID);
        builder.put(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/ld" +
                "+json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        final var request = builder.build();
        final var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn(String.format("---- [DataspaceConnectorClient updateCustomResourceRepresentation] Updating custom resource Representation at %s failed!",
                    dataSpaceConnectorHost));
        }
        final var body = Objects.requireNonNull(response.body()).string();
        log.info("---- [DataspaceConnectorClient updateCustomResourceRepresentation] Response: " + body);
        return body;
    }

    @Override
    public String updateResourceContract(final String resourceID, final String contract) throws IOException {
        log.info(String.format("---- [DataspaceConnectorClient updateResourceContract] updating contract for resource at %s",
                dataSpaceConnectorHost));
        final var mappedResourceID =
                dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(resourceID));
//        var resourceJsonLD = SERIALIZER.serialize(contract);
        final var builder = new Request.Builder();
        builder.url("https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin" +
                "/api/resources/" + mappedResourceID + "/contract");
        builder.put(RequestBody.create(contract, okhttp3.MediaType.parse("application/ld+json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        final var request = builder.build();
        final var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn(String.format("---- [DataspaceConnectorClient updateResourceContract] Updating Contract at %s failed!", dataSpaceConnectorHost));
        }
        final var body = Objects.requireNonNull(response.body()).string();
        log.info("---- [DataspaceConnectorClient updateResourceContract] Response: " + body);
        return body;
    }

    @Override
    public String getPolicyPattern(final String policy) throws IOException {
        log.info(String.format("---- [DataspaceConnectorClient getPolicyPattern] Get pattern for policy"));
        final var builder = new Request.Builder();
        builder.url("https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin" +
                "/api/example/policy-validation");
        builder.post(RequestBody.create(policy, okhttp3.MediaType.parse("application/ld+json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        final var request = builder.build();
        final var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn("---- Pattern for policy could not be determined");
        }
        final var body = Objects.requireNonNull(response.body()).string();
        log.info("---- Response: " + body);
        return body;
    }

    @Override
    public String updateResource(final URI resourceID, final Resource resource) throws IOException {
        log.info(String.format("---- updating resource at %s", dataSpaceConnectorHost));
        final var mappedResource = dataSpaceConnectorResourceMapper.getMetadata(resource);
        final var path = resourceID.getPath();
        final var idStr = path.substring(path.lastIndexOf('/') + 1);
        final var resourceUUID = UUID.fromString(idStr);
        final var resourceJsonLD = MAPPER.writeValueAsString(mappedResource);
        final var builder = new Request.Builder();
        builder.url("https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin" +
                "/api/resources/" + resourceUUID);
        builder.put(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/ld" +
                "+json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        final var request = builder.build();
        final var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn(String.format("---- Updating Resource at %s failed!", dataSpaceConnectorHost));
        }
        final var body = Objects.requireNonNull(response.body()).string();
        log.info("---- Response: " + body);
        return body;
    }

}
