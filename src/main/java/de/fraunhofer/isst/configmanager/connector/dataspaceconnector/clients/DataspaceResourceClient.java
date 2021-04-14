package de.fraunhofer.isst.configmanager.connector.dataspaceconnector.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultResourceClient;
import de.fraunhofer.isst.configmanager.connector.dataspaceconnector.model.BackendSource;
import de.fraunhofer.isst.configmanager.connector.dataspaceconnector.util.DispatchRequest;
import de.fraunhofer.isst.configmanager.connector.dataspaceconnector.util.ResourceMapper;
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
public class DataspaceResourceClient extends AbstractDataspaceConnectorClient implements DefaultResourceClient {

    public DataspaceResourceClient(final ResourceMapper dataSpaceConnectorResourceMapper) {
        super(dataSpaceConnectorResourceMapper);
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
            log.warn("---- [DataspaceResourceClient getJsonNodeOfBaseConnector] Could not get BaseConnector");
        }

        final var body = Objects.requireNonNull(response.body()).string();
        final var mapper = new ObjectMapper();

        return mapper.readTree(body);
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
            log.warn(String.format("---- [DataspaceResourceClient getRequestedResource] Could not get BaseConnector from %s!", dataSpaceConnectorHost));
        }

        final var body = Objects.requireNonNull(response.body()).string();
        final var splitBody = body.split("\n", 2);
        final var resource = splitBody[1].substring(10);

        Resource deserializedResource = null;
        try {
            deserializedResource = SERIALIZER.deserialize(resource, Resource.class);
        } catch (IOException e) {
            log.error("---- [DataspaceResourceClient getRequestedResource] SERIALIZER.deserialize threw IOException");
            log.error(e.getMessage(), e);
        }

        return deserializedResource;
    }

    @Override
    public String registerResource(final Resource resource) throws IOException {
        log.info("---- [DataspaceResourceClient registerResource] Registering resource...");

        final var mappedResource = dataSpaceConnectorResourceMapper.getMetadata(resource);
        final var resourceJsonLD = MAPPER.writeValueAsString(mappedResource);

        log.info("---- [DataspaceResourceClient registerResource] New resource: " + resourceJsonLD);

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
        builder.post(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/ld+json")));
        builder.header("Authorization",
                Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));

        log.info("---- [DataspaceResourceClient registerResource] " + url.toString());

        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceResourceClient registerResource] Registering Resource failed!");
        }

        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public String deleteResource(final URI resourceID) throws IOException {
        log.info(String.format("---- [DataspaceResourceClient deleteResource] deleting resource %s at %s", resourceID,
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
            log.warn("---- [DataspaceResourceClient deleteResource] Deleting Resource failed!");
        }

        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public String registerResourceRepresentation(final String resourceID,
                                                 final Representation representation,
                                                 final String endpointId) throws IOException {
        log.info(String.format("---- [DataspaceResourceClient registerResourceRepresentation] Registering resource at %s", dataSpaceConnectorHost));

        final var mappedRepresentation = dataSpaceConnectorResourceMapper.mapRepresentation(representation);
        final var backendSource = dataSpaceConnectorResourceMapper.createBackendSource(endpointId, representation);
        mappedRepresentation.setSource(backendSource);

        final var mappedResourceID = dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(resourceID));
        final var mappedRepresentationID = dataSpaceConnectorResourceMapper.readUUIDFromURI(representation.getId());
        final var resourceJsonLD = MAPPER.writeValueAsString(mappedRepresentation);

        log.info("---- [DataspaceResourceClient registerResourceRepresentation] Mapped representation: " + resourceJsonLD);

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
            log.warn("---- [DataspaceResourceClient registerResourceRepresentation] Registering Representation failed!");
        }

        final var body = Objects.requireNonNull(response.body()).string();
        final var uuid = dataSpaceConnectorResourceMapper.createFromResponse(body, representation.getId());

        if (uuid == null) {
            log.warn("---- [DataspaceResourceClient registerResourceRepresentation] Could not parse ID from response!");
        } else {
            log.info("---- [DataspaceResourceClient registerResourceRepresentation] UUID is : " + uuid);
        }

        return body;
    }

    @Override
    public String updateResourceRepresentation(final String resourceID,
                                               final String representationID,
                                               final Representation representation,
                                               final String endpointId) throws IOException {
        log.info(String.format(
                "---- [DataspaceResourceClient updateResourceRepresentation] Updating representation %s for resource %s at %s",
                representationID,
                resourceID,
                dataSpaceConnectorHost));

        final var mappedResourceID = dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(resourceID));
        final var mappedRepresentationID = dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(representationID));
        final var mappedRepresentation = dataSpaceConnectorResourceMapper.mapRepresentation(representation);
        final var backendSource = dataSpaceConnectorResourceMapper.createBackendSource(endpointId, representation);

        mappedRepresentation.setSource(backendSource);

        final var resourceJsonLD = MAPPER.writeValueAsString(mappedRepresentation);

        log.info("---- [DataspaceResourceClient updateResourceRepresentation] Mapped representation: " + resourceJsonLD);

        final var builder = getRequestBuilder();

        builder.url(connectorBaseUrl + "admin/api/resources/" + mappedResourceID + "/" + mappedRepresentationID);
        builder.put(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/ld+json")));
        builder.header("Authorization",
                Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));

        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful()) {
            log.warn("---- [DataspaceResourceClient updateResourceRepresentation] Updating Representation failed!");
        }

        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public String updateResourceContract(final String resourceID, final String contract) throws IOException {
        log.info(String.format(
                "---- [DataspaceResourceClient updateResourceContract] updating contract for resource at %s",
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
            log.warn("---- [DataspaceResourceClient updateResourceContract] Updating contract failed!");
        }

        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public Response updateResource(final URI resourceID, final Resource resource) throws IOException {
        log.info(String.format("---- [DataspaceResourceClient updateResource] updating resource at %s", dataSpaceConnectorHost));

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
            log.warn("---- [DataspaceResourceClient updateResource] Updating Resource failed!");
        }

        return response;
    }
}
