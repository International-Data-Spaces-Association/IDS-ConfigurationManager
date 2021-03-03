package de.fraunhofer.isst.configmanager.communication.dataspaceconnector;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model.ResourceRepresentation;
import de.fraunhofer.isst.configmanager.configmanagement.service.EndpointService;
import de.fraunhofer.isst.configmanager.util.OkHttpUtils;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

/**
 * A prototypical implementation of the interface DefaultConnectorClient for the dataspace connector.
 * This can be used as a guide and can also be used for a specific connector.
 * <p>
 * The special feature here is that requests are sent to the connector via http.
 * The corresponding host, port number and credentials can then be set to establish the connection.
 * In addition it must be considered whether the model e.g. for a resource also corresponds to the model of the
 * information model. If this is not the case, a mapping has to be done, see for example
 * {@link de.fraunhofer.isst.configmanager.communication.dataspaceconnector.DataSpaceConnectorResourceMapper}.
 */
@Service
@ConditionalOnExpression("${dataspace.connector.enabled:false}")
public class DataspaceConnectorClient implements DefaultConnectorClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataspaceConnectorClient.class);
    private static final Serializer SERIALIZER = new Serializer();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final OkHttpClient client = OkHttpUtils.getUnsafeOkHttpClient();
    private final DataSpaceConnectorResourceMapper dataSpaceConnectorResourceMapper;
    private final EndpointService endpointService;

    @Value("${dataspace.connector.host}")
    private String dataSpaceConnectorHost;

    @Value("${dataspace.connector.api.username}")
    private String dataSpaceConnectorApiUsername;

    @Value("${dataspace.connector.api.password}")
    private String dataSpaceConnectorApiPassword;

    @Value("${dataspace.connector.port}")
    private Integer dataSpaceConnectorPort;

    public DataspaceConnectorClient(DataSpaceConnectorResourceMapper dataSpaceConnectorResourceMapper,
                                    EndpointService endpointService) {
        this.dataSpaceConnectorResourceMapper = dataSpaceConnectorResourceMapper;
        this.endpointService = endpointService;
    }

    @Override
    public String updateAtBroker(String brokerURI) throws IOException {
        LOGGER.info(String.format("updating connector %s at broker %s", dataSpaceConnectorHost, brokerURI));
        var builder = new Request.Builder();
        builder.url(new HttpUrl.Builder()
                .scheme("https")
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/broker/update")
                .addQueryParameter("broker", brokerURI)
                .build());
        builder.post(RequestBody.create(brokerURI, okhttp3.MediaType.parse("text/html")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        var request = builder.build();
        return client.newCall(request).execute().body().string();
    }

    @Override
    public String unregisterAtBroker(String brokerURI) throws IOException {
        LOGGER.info(String.format("unregistering connector %s at broker %s", dataSpaceConnectorHost, brokerURI));
        var builder = new Request.Builder();
        builder.url(new HttpUrl.Builder()
                .scheme("https")
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/broker/unregister")
                .addQueryParameter("broker", brokerURI)
                .build());
        builder.post(RequestBody.create(brokerURI, okhttp3.MediaType.parse("text/html")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        var request = builder.build();
        return client.newCall(request).execute().body().string();
    }

    @Override
    public ConfigurationModel getConfiguration() throws IOException {
        var builder = new Request.Builder();
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        builder.url("https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin/api/configuration");
        builder.get();
        var request = builder.build();
        var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            LOGGER.warn(String.format("Could not get ConfigurationModel from %s!", dataSpaceConnectorHost));
        }
        var body = response.body().string();
        return SERIALIZER.deserialize(body, ConfigurationModel.class);
    }

    @Override
    public boolean sendConfiguration(String configurationModel) throws IOException {
        LOGGER.info(String.format("sending new configuration to %s", dataSpaceConnectorHost));
        var builder = new Request.Builder();
        builder.url("https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin/api/configuration");
        builder.post(RequestBody.create(configurationModel, okhttp3.MediaType.parse("application/ld+json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        var request = builder.build();
        var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            LOGGER.warn(String.format("Updating ConfigurationModel at %s failed!", dataSpaceConnectorHost));
            return false;
        }
        var body = response.body().string();
        LOGGER.info("Response: " + body);
        return true;
    }

    @Override
    public BaseConnector getBaseConnector(String accessURL, String resourceId) throws IOException {
        var builder = new Request.Builder();
        var urlBuilder = new HttpUrl.Builder()
                .scheme("https")
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/request/description")
                .addQueryParameter("recipient", accessURL);
        if (resourceId != null && !resourceId.isBlank()) {
            urlBuilder.addQueryParameter("requestedResource", resourceId);
        }
        var url = urlBuilder.build();
        LOGGER.info(url.toString());
        builder.url(url);
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        builder.post(RequestBody.create(null, new byte[0]));
        var request = builder.build();
        var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            LOGGER.warn(String.format("Could not get BaseConnector from %s!", dataSpaceConnectorHost));
        }
        var body = response.body().string();
        LOGGER.info(body);
        return SERIALIZER.deserialize(body, BaseConnector.class);
    }

    @Override
    public String registerResource(Resource resource) throws IOException {
        LOGGER.info(String.format("registering resource at %s", dataSpaceConnectorHost));
        var mappedResource = dataSpaceConnectorResourceMapper.getMetadata(resource);
        var resourceJsonLD = MAPPER.writeValueAsString(mappedResource);
        LOGGER.info("new resource: " + resourceJsonLD);
        var builder = new Request.Builder();
        String path = resource.getId().getPath();
        String idStr = path.substring(path.lastIndexOf('/') + 1);
        var url = new HttpUrl.Builder()
                .scheme("https")
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/resources/resource")
                .addQueryParameter("id", idStr)
                .build();
        LOGGER.info(url.toString());
        builder.url(url);
        builder.post(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/ld+json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        var request = builder.build();
        var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            LOGGER.warn(String.format("Registering Resource at %s failed!", dataSpaceConnectorHost));
        }
        var body = response.body().string();
        LOGGER.info("Response: " + body);
        return body;
    }

    @Override
    public String deleteResource(URI resourceID) throws IOException {
        LOGGER.info(String.format("deleting resource %s at %s", resourceID, dataSpaceConnectorHost));
        String path = resourceID.getPath();
        String idStr = path.substring(path.lastIndexOf('/') + 1);
        var builder = new Request.Builder();
        builder.url("https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin/api/resources/" + idStr);
        builder.delete();
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        var request = builder.build();
        var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            LOGGER.warn(String.format("Deleting Resource at %s failed!", dataSpaceConnectorHost));
        }
        var body = response.body().string();
        LOGGER.info("Response: " + body);
        return body;
    }

    @Override
    public String updateResourceAtBroker(String brokerUri, URI resourceID) throws IOException {
        LOGGER.info(String.format("updating resource at Broker %s", brokerUri));
        String path = resourceID.getPath();
        String idStr = path.substring(path.lastIndexOf('/') + 1);
        UUID resourceUUID = UUID.fromString(idStr);
        var builder = new Request.Builder();
        builder.url(new HttpUrl.Builder()
                .scheme("https")
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/broker/update/" + resourceUUID)
                .addQueryParameter("broker", brokerUri)
                .build());
        builder.post(RequestBody.create(null, new byte[0]));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        var request = builder.build();
        var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            LOGGER.warn(String.format("Updating Resource at Broker %s failed!", brokerUri));
        }
        var body = response.body().string();
        LOGGER.info("Response: " + body);
        return body;
    }

    @Override
    public String deleteResourceAtBroker(String brokerUri, URI resourceID) throws IOException {
        LOGGER.info(String.format("deleting resource %s at Broker %s", resourceID, brokerUri));
        String path = resourceID.getPath();
        String idStr = path.substring(path.lastIndexOf('/') + 1);
        var builder = new Request.Builder();
        builder.url(new HttpUrl.Builder()
                .scheme("https")
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/broker/remove/" + idStr)
                .addQueryParameter("broker", brokerUri)
                .build());
        builder.post(RequestBody.create(null, new byte[0]));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        var request = builder.build();
        var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            LOGGER.warn(String.format("Deleting Resource at Broker %s failed!", brokerUri));
        }
        var body = response.body().string();
        LOGGER.info("Response: " + body);
        return body;
    }

    @Override
    public String deleteResourceRepresentation(String resourceID, String representationID) throws IOException {
        LOGGER.info(String.format("deleting representation %s from resource %s at %s", representationID, resourceID, dataSpaceConnectorHost));
        var builder = new Request.Builder();
        var mappedResourceID = dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(resourceID));
        var mappedRepresentationID = dataSpaceConnectorResourceMapper.getMappedId(URI.create(representationID));
        dataSpaceConnectorResourceMapper.deleteResourceIDPair(URI.create(representationID));
        builder.url("https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin/api/resources/" + mappedResourceID + "/" + mappedRepresentationID);
        builder.delete();
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        var request = builder.build();
        var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            LOGGER.warn(String.format("Deleting Representation at %s failed!", dataSpaceConnectorHost));
        }
        var body = response.body().string();
        LOGGER.info("Response: " + body);
        return body;
    }

    @Override
    public String registerResourceRepresentation(String resourceID, Representation representation, String endpointId) throws IOException {
        LOGGER.info(String.format("registering resource at %s", dataSpaceConnectorHost));
        var mappedRepresentation = dataSpaceConnectorResourceMapper.mapRepresentation(representation);
        var backendSource = dataSpaceConnectorResourceMapper.createBackendSource(endpointId, representation);
        mappedRepresentation.setSource(backendSource);
        var mappedResourceID = dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(resourceID));
        var mappedRepresentationID = dataSpaceConnectorResourceMapper.readUUIDFromURI(representation.getId());
        var resourceJsonLD = MAPPER.writeValueAsString(mappedRepresentation);
        LOGGER.info("mapped representation: " + resourceJsonLD);
        var builder = new Request.Builder();
        builder.url(new HttpUrl.Builder()
                .scheme("https")
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/resources/" + mappedResourceID + "/representation")
                .addQueryParameter("id", mappedRepresentationID.toString())
                .build());
        builder.post(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/ld+json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        var request = builder.build();
        var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            LOGGER.warn(String.format("Registering Representation at %s failed!", dataSpaceConnectorHost));
        }
        var body = response.body().string();
        var uuid = dataSpaceConnectorResourceMapper.createFromResponse(body, representation.getId());
        if (uuid == null) {
            LOGGER.warn("Could not parse ID from response!");
        } else {
            LOGGER.info("UUID is : " + uuid);
        }
        return body;
    }

    @Override
    public String updateResourceRepresentation(String resourceID, String representationID, Representation representation, String endpointId) throws IOException {
        LOGGER.info(String.format("updating representation %s for resource %s at %s", representationID, resourceID, dataSpaceConnectorHost));
        var mappedResourceID = dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(resourceID));
        var mappedRepresentationID = dataSpaceConnectorResourceMapper.getMappedId(URI.create(representationID));
        var mappedRepresentation = dataSpaceConnectorResourceMapper.mapRepresentation(representation);
        var backendSource = dataSpaceConnectorResourceMapper.createBackendSource(endpointId, representation);
        mappedRepresentation.setSource(backendSource);
        var resourceJsonLD = MAPPER.writeValueAsString(mappedRepresentation);
        LOGGER.info("mapped representation: " + resourceJsonLD);
        var builder = new Request.Builder();
        builder.url("https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin/api/resources/" + mappedResourceID + "/" + mappedRepresentationID);
        builder.put(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/ld+json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        var request = builder.build();
        var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            LOGGER.warn(String.format("Updating Representation at %s failed!", dataSpaceConnectorHost));
        }
        var body = response.body().string();
        LOGGER.info("Response: " + body);
        return body;
    }

    @Override
    public String updateCustomResourceRepresentation(String resourceID, String representationID, ResourceRepresentation resourceRepresentation) throws IOException {
        LOGGER.info(String.format("updating representation %s for resource %s at %s", representationID, resourceID, dataSpaceConnectorHost));
        var mappedResourceID = dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(resourceID));
        var mappedRepresentationID = dataSpaceConnectorResourceMapper.getMappedId(URI.create(representationID));
        var resourceJsonLD = MAPPER.writeValueAsString(resourceRepresentation);
        LOGGER.info("mapped representation: " + resourceJsonLD);
        var builder = new Request.Builder();
        builder.url("https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin/api/resources/" + mappedResourceID + "/" + mappedRepresentationID);
        builder.put(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/ld+json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        var request = builder.build();
        var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            LOGGER.warn(String.format("Updating custom resource Representation at %s failed!", dataSpaceConnectorHost));
        }
        var body = response.body().string();
        LOGGER.info("Response: " + body);
        return body;
    }

    @Override
    public String updateResourceContract(String resourceID, Contract contract) throws IOException {
        LOGGER.info(String.format("updating contract for resource at %s", dataSpaceConnectorHost));
        var mappedResourceID = dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(resourceID));
        var resourceJsonLD = SERIALIZER.serialize(contract);
        var builder = new Request.Builder();
        builder.url("https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin/api/resources/" + mappedResourceID + "/contract");
        builder.put(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/ld+json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        var request = builder.build();
        var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            LOGGER.warn(String.format("Updating Contract at %s failed!", dataSpaceConnectorHost));
        }
        var body = response.body().string();
        LOGGER.info("Response: " + body);
        return body;
    }

    @Override
    public String updateResource(URI resourceID, Resource resource) throws IOException {
        LOGGER.info(String.format("updating resource at %s", dataSpaceConnectorHost));
        var mappedResource = dataSpaceConnectorResourceMapper.getMetadata(resource);
        String path = resourceID.getPath();
        String idStr = path.substring(path.lastIndexOf('/') + 1);
        UUID resourceUUID = UUID.fromString(idStr);
        var resourceJsonLD = MAPPER.writeValueAsString(mappedResource);
        var builder = new Request.Builder();
        builder.url("https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin/api/resources/" + resourceUUID);
        builder.put(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        var request = builder.build();
        var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            LOGGER.warn(String.format("Updating Resource at %s failed!", dataSpaceConnectorHost));
        }
        var body = response.body().string();
        LOGGER.info("Response: " + body);
        return body;
    }

}