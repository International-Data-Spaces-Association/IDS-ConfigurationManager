package de.fraunhofer.isst.configmanager.connector.dataspaceconnector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.BaseConnector;
import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.connector.dataspaceconnector.model.BackendSource;
import de.fraunhofer.isst.configmanager.connector.dataspaceconnector.model.ResourceRepresentation;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;

/**
 * An implementation of the interface DefaultConnectorClient for the DataspaceConnector.
 */
@Slf4j
@Service
@ConditionalOnExpression("${dataspace.connector.enabled:false}")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataspaceConnectorClient implements DefaultConnectorClient {
    final static Serializer SERIALIZER = new Serializer();
    final static ObjectMapper MAPPER = new ObjectMapper();

    @Value("${dataspace.connector.host}")
    transient String dataSpaceConnectorHost;

    @Value("${dataspace.connector.api.username}")
    transient String dataSpaceConnectorApiUsername;

    @Value("${dataspace.connector.api.password}")
    transient String dataSpaceConnectorApiPassword;

    @Value("${dataspace.connector.port}")
    transient Integer dataSpaceConnectorPort;

    transient String protocol;

    final transient ResourceMapper dataSpaceConnectorResourceMapper;

    String connectorBaseUrl = "";

    public DataspaceConnectorClient(final ResourceMapper dataSpaceConnectorResourceMapper) {
        this.dataSpaceConnectorResourceMapper = dataSpaceConnectorResourceMapper;
    }

    @Autowired
    public void setProtocol(final @Value("${dataspace.communication.ssl}") String https) {
        protocol = Boolean.parseBoolean(https) ? "https" : "http";
        connectorBaseUrl = protocol + "://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/";

        log.info("---- [DataspaceConnectorClient setProtocol] Communication Protocol with DataspaceConnector is: " + protocol);
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
        final var connectorUrl = connectorBaseUrl + "admin/api/connector";

        final var builder = getRequestBuilder();
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        builder.url(connectorUrl);
        builder.get();

        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

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

        log.info("---- [DataspaceConnectorClient getBaseConnector] " + url.toString());
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
    public Resource getRequestedResource(final String accessURL, final String resourceId) throws IOException {
        final var builder = getRequestBuilder();
        final var urlBuilder = new HttpUrl.Builder()
                .scheme(protocol)
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/request/description")
                .addQueryParameter("recipient", accessURL)
                .addQueryParameter("requestedResource", resourceId);

        final var url = urlBuilder.build();
        builder.url(url);
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        builder.post(RequestBody.create(new byte[0], null));

        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful()) {
            log.warn(String.format("---- [DataspaceConnectorClient getRequestedResource] Could not get BaseConnector from %s!", dataSpaceConnectorHost));
        }

        final var body = Objects.requireNonNull(response.body()).string();
        final var splitBody = body.split("\n", 2);
        final var resource = splitBody[1].substring(10);

        Resource deserializedResource = null;
        try {
            deserializedResource = SERIALIZER.deserialize(resource, Resource.class);
        } catch (IOException e) {
            log.error("---- [DataspaceConnectorClient getRequestedResource] SERIALIZER.deserialize threw IOException");
            log.error(e.getMessage(), e);
        }

        return deserializedResource;
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
    public String registerResource(final Resource resource) throws IOException {
        log.info("---- [DataspaceConnectorClient registerResource] Registering resource...");

        final var mappedResource = dataSpaceConnectorResourceMapper.getMetadata(resource);
        final var resourceJsonLD = MAPPER.writeValueAsString(mappedResource);

        log.info("---- [DataspaceConnectorClient registerResource] New resource: " + resourceJsonLD);

        final var builder = getRequestBuilder();
        final var path = resource.getId().getPath();
        final var idStr = path.substring(path.lastIndexOf('/') + 1);

        final var url = new HttpUrl.Builder()
                .scheme(protocol)
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/resources/resource")
                .addQueryParameter("id", idStr)
                .build();
        builder.url(url);
        builder.post(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/ld" +
                "+json")));
        builder.header("Authorization",
                Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));

        log.info("---- [DataspaceConnectorClient registerResource] " + url.toString());

        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient registerResource] Registering Resource failed!");
        }

        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public String deleteResource(final URI resourceID) throws IOException {
        log.info(String.format("---- [DataspaceConnectorClient deleteResource] deleting resource %s at %s", resourceID,
                dataSpaceConnectorHost));

        final var path = resourceID.getPath();
        final var idStr = path.substring(path.lastIndexOf('/') + 1);

        final var builder = getRequestBuilder();
        builder.url(connectorBaseUrl + "admin/api/resources/" + idStr);
        builder.delete();
        builder.header("Authorization",
                Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient deleteResource] Deleting Resource failed!");
        }

        return Objects.requireNonNull(response.body()).string();
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

    @Override
    public String deleteResourceRepresentation(final String resourceID,
                                               final String representationID) throws IOException {
        log.info(String.format(
                "---- [DataspaceConnectorClient deleteResourceRepresentation] Deleting representation %s from resource %s at %s",
                representationID,
                resourceID,
                dataSpaceConnectorHost));


        final var mappedResourceID = dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(resourceID));
        final var mappedRepresentationID = dataSpaceConnectorResourceMapper.getMappedId(URI.create(representationID));
        dataSpaceConnectorResourceMapper.deleteResourceIDPair(URI.create(representationID));

        final var builder = getRequestBuilder();
        builder.url(connectorBaseUrl + "admin/api/resources/" + mappedResourceID + "/" + mappedRepresentationID);
        builder.delete();
        builder.header("Authorization",
                Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));

        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient deleteResourceRepresentation] Deleting Representation failed!");
        }

        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public String registerResourceRepresentation(final String resourceID,
                                                 final Representation representation,
                                                 final String endpointId) throws IOException {
        log.info(String.format("---- [DataspaceConnectorClient registerResourceRepresentation] Registering resource at %s", dataSpaceConnectorHost));

        final var mappedRepresentation = dataSpaceConnectorResourceMapper.mapRepresentation(representation);
        final var backendSource = dataSpaceConnectorResourceMapper.createBackendSource(endpointId, representation);
        mappedRepresentation.setSource(backendSource);

        final var mappedResourceID = dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(resourceID));
        final var mappedRepresentationID = dataSpaceConnectorResourceMapper.readUUIDFromURI(representation.getId());
        final var resourceJsonLD = MAPPER.writeValueAsString(mappedRepresentation);

        log.info("---- [DataspaceConnectorClient registerResourceRepresentation] Mapped representation: " + resourceJsonLD);

        final var builder = getRequestBuilder();
        builder.url(new HttpUrl.Builder()
                .scheme(protocol)
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/resources/" + mappedResourceID + "/representation")
                .addQueryParameter("id", mappedRepresentationID.toString())
                .build());
        builder.post(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/ld+json")));
        builder.header("Authorization",
                Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));

        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient registerResourceRepresentation] Registering Representation failed!");
        }

        final var body = Objects.requireNonNull(response.body()).string();
        final var uuid = dataSpaceConnectorResourceMapper.createFromResponse(body, representation.getId());

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
        log.info(String.format(
                "---- [DataspaceConnectorClient updateResourceRepresentation] Updating representation %s for resource %s at %s",
                representationID,
                resourceID,
                dataSpaceConnectorHost));

        final var mappedResourceID = dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(resourceID));
        final var mappedRepresentationID = dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(representationID));
        final var mappedRepresentation = dataSpaceConnectorResourceMapper.mapRepresentation(representation);
        final var backendSource = dataSpaceConnectorResourceMapper.createBackendSource(endpointId, representation);

        mappedRepresentation.setSource(backendSource);

        final var resourceJsonLD = MAPPER.writeValueAsString(mappedRepresentation);

        log.info("---- [DataspaceConnectorClient updateResourceRepresentation] Mapped representation: " + resourceJsonLD);

        final var builder = getRequestBuilder();

        builder.url(connectorBaseUrl + "admin/api/resources/" + mappedResourceID + "/" + mappedRepresentationID);
        builder.put(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/ld+json")));
        builder.header("Authorization",
                Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));

        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient updateResourceRepresentation] Updating Representation failed!");
        }

        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public String updateCustomResourceRepresentation(final String resourceID,
                                                     final String representationID,
                                                     final ResourceRepresentation resourceRepresentation) throws IOException {
        log.info(String.format(
                "---- [DataspaceConnectorClient updateCustomResourceRepresentation] Updating representation %s for resource %s at %s",
                representationID,
                resourceID,
                dataSpaceConnectorHost));

        final var mappedResourceID = dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(resourceID));
        final var mappedRepresentationID = dataSpaceConnectorResourceMapper.getMappedId(URI.create(representationID));
        final var resourceJsonLD = MAPPER.writeValueAsString(resourceRepresentation);

        log.info("---- [DataspaceConnectorClient updateCustomResourceRepresentation] Mapped representation: " + resourceJsonLD);

        final var builder = getRequestBuilder();
        builder.url(connectorBaseUrl + "admin/api/resources/" + mappedResourceID + "/" + mappedRepresentationID);
        builder.put(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/ld+json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));

        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient updateCustomResourceRepresentation] Updating Resource-Representation failed!");
        }

        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public String updateResourceContract(final String resourceID, final String contract) throws IOException {
        log.info(String.format(
                "---- [DataspaceConnectorClient updateResourceContract] updating contract for resource at %s",
                dataSpaceConnectorHost));

        final var mappedResourceID = dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(resourceID));

        final var builder = getRequestBuilder();
        builder.url(connectorBaseUrl + "admin/api/resources/" + mappedResourceID + "/contract");
        builder.put(RequestBody.create(contract, okhttp3.MediaType.parse("application/ld+json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));

        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient updateResourceContract] Updating contract failed!");
        }

        return Objects.requireNonNull(response.body()).string();
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

    @Override
    public String updateResource(final URI resourceID, final Resource resource) throws IOException {
        log.info(String.format("---- [DataspaceConnectorClient updateResource] updating resource at %s", dataSpaceConnectorHost));

        final var mappedResource = dataSpaceConnectorResourceMapper.getMetadata(resource);
        final var path = resourceID.getPath();
        final var idStr = path.substring(path.lastIndexOf('/') + 1);
        final var resourceUUID = UUID.fromString(idStr);

        final var backendSource = new BackendSource();
        try {
            final var requestBackendBuilder = getRequestBuilder();
            requestBackendBuilder.url(connectorBaseUrl + "admin/api/resources/" + resourceUUID);
            requestBackendBuilder.get();
            requestBackendBuilder.header("Authorization",
                    Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));

            final var request = requestBackendBuilder.build();
            final var response = DispatchRequest.sendToDataspaceConnector(request);
            final var mapper = new ObjectMapper();
            final var jsonTree = mapper.readTree(Objects.requireNonNull(response.body()).string());

            final var source = jsonTree.findValue("source");
            backendSource.setType(BackendSource.Type.valueOf(source.get("type").asText().toUpperCase().replace("-", "_")));
            backendSource.setUrl(URI.create(source.get("url").asText()));
            backendSource.setUsername(source.get("username").asText());
            backendSource.setPassword(source.get("password").asText());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        mappedResource.getRepresentations().get(0).setSource(backendSource);
        final var resourceJsonLD = MAPPER.writeValueAsString(mappedResource);

        final var builder = getRequestBuilder();
        builder.url(connectorBaseUrl + "admin/api/resources/" + resourceUUID);
        builder.put(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/ld+json")));
        builder.header("Authorization",
                Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceConnectorClient updateResource] Updating Resource failed!");
        }

        return Objects.requireNonNull(response.body()).string();
    }

    @NotNull
    private Request.Builder getRequestBuilder() {
        return new Request.Builder();
    }
}
