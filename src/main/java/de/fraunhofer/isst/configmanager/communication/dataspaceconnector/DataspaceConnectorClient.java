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
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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
public class DataspaceConnectorClient implements DefaultConnectorClient {
    private static final Serializer SERIALIZER = new Serializer();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private transient final OkHttpClient client = OkHttpUtils.getUnsafeOkHttpClient();
    private transient final DataSpaceConnectorResourceMapper dataSpaceConnectorResourceMapper;

    @Value("${dataspace.connector.host}")
    private transient String dataSpaceConnectorHost;

    @Value("${dataspace.connector.api.username}")
    private transient String dataSpaceConnectorApiUsername;

    @Value("${dataspace.connector.api.password}")
    private transient String dataSpaceConnectorApiPassword;

    @Value("${dataspace.connector.port}")
    private transient Integer dataSpaceConnectorPort;

    public DataspaceConnectorClient(final DataSpaceConnectorResourceMapper dataSpaceConnectorResourceMapper) {
        this.dataSpaceConnectorResourceMapper = dataSpaceConnectorResourceMapper;
    }

    @Override
    public String updateAtBroker(final String brokerURI) throws IOException {
        log.info(String.format("---- updating connector %s at broker %s", dataSpaceConnectorHost,
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
        log.info(String.format("---- unregistering connector %s at broker %s",
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
            log.warn("---- Could not get ConfigurationModel from {} with user {}. Response: {} - " +
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
            log.warn("---- Could not get BaseConnector");
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
        final var offeredResourceNode = baseConnectorNode.findValue("ids:requestedResource");
        return offeredResourceNode.toString();
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
            log.warn("---- Could not get BaseConnector");
        }
        final var body = Objects.requireNonNull(response.body()).string();
        final var mapper = new ObjectMapper();
        return mapper.readTree(body);
    }


    @Override
    public boolean sendConfiguration(final String configurationModel) throws IOException {
        log.info(String.format("---- sending new configuration to %s", dataSpaceConnectorHost));
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
            log.warn(String.format("---- Updating ConfigurationModel at %s failed!",
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
        log.info("---- " + url.toString());
        builder.url(url);
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        builder.post(RequestBody.create(null, new byte[0]));
        final var request = builder.build();
        final var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn(String.format("---- Could not get BaseConnector from %s!",
                    dataSpaceConnectorHost));
        }
        final var body = Objects.requireNonNull(response.body()).string();
        return SERIALIZER.deserialize(body, BaseConnector.class);
    }

    @Override
    public String registerResource(final Resource resource) throws IOException {
        log.info(String.format("---- registering resource at %s", dataSpaceConnectorHost));
        final var mappedResource = dataSpaceConnectorResourceMapper.getMetadata(resource);
        final var resourceJsonLD = MAPPER.writeValueAsString(mappedResource);
        log.info("---- new resource: " + resourceJsonLD);
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
        log.info("---- " + url.toString());
        builder.url(url);
        builder.post(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/ld" +
                "+json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        final var request = builder.build();
        final var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn(String.format("---- Registering Resource at %s failed!",
                    dataSpaceConnectorHost));
        }
        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public String deleteResource(final URI resourceID) throws IOException {
        log.info(String.format("---- deleting resource %s at %s", resourceID,
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
            log.warn(String.format("---- Deleting Resource at %s failed!", dataSpaceConnectorHost));
        }
        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public String updateResourceAtBroker(final String brokerUri, final URI resourceID) throws IOException {
        log.info(String.format("---- updating resource at Broker %s", brokerUri));
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
            log.warn(String.format("---- Updating Resource at Broker %s failed!", brokerUri));
        }
        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public String deleteResourceAtBroker(final String brokerUri, final URI resourceID) throws IOException {
        log.info(String.format("---- deleting resource %s at Broker %s", resourceID, brokerUri));
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
            log.warn(String.format("---- Deleting Resource at Broker %s failed!", brokerUri));
        }
        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public String deleteResourceRepresentation(final String resourceID,
                                               final String representationID) throws IOException {
        log.info(String.format("---- deleting representation %s from resource %s at %s",
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
            log.warn(String.format("---- Deleting Representation at %s failed!",
                    dataSpaceConnectorHost));
        }
        return Objects.requireNonNull(response.body()).string();
    }

    @Override
    public String registerResourceRepresentation(final String resourceID,
                                                 final Representation representation,
                                                 final String endpointId) throws IOException {
        log.info(String.format("---- registering resource at %s", dataSpaceConnectorHost));
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
        log.info("---- mapped representation: " + resourceJsonLD);
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
            log.warn(String.format("---- Registering Representation at %s failed!",
                    dataSpaceConnectorHost));
        }
        final var body = Objects.requireNonNull(response.body()).string();
        final var uuid = dataSpaceConnectorResourceMapper.createFromResponse(body,
                representation.getId());
        if (uuid == null) {
            log.warn("---- Could not parse ID from response!");
        } else {
            log.info("---- UUID is : " + uuid);
        }
        return body;
    }

    @Override
    public String updateResourceRepresentation(final String resourceID,
                                               final String representationID,
                                               final Representation representation,
                                               final String endpointId) throws IOException {
        log.info(String.format("---- updating representation %s for resource %s at %s",
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
        log.info("---- mapped representation: " + resourceJsonLD);
        final var builder = new Request.Builder();
        log.info("---- Calling DSC at: https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin/api/resources/" + mappedResourceID + "/" + mappedRepresentationID);
        builder.url("https://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/admin" +
                "/api/resources/" + mappedResourceID + "/" + mappedRepresentationID);
        builder.put(RequestBody.create(resourceJsonLD, okhttp3.MediaType.parse("application/ld" +
                "+json")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));
        final var request = builder.build();
        final var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.warn(String.format("---- Updating Representation at %s failed!",
                    dataSpaceConnectorHost));
        }
        final var body = Objects.requireNonNull(response.body()).string();
        log.info("---- Response: " + body);
        return body;
    }

    @Override
    public String updateCustomResourceRepresentation(final String resourceID,
                                                     final String representationID,
                                                     final ResourceRepresentation resourceRepresentation) throws IOException {
        log.info(String.format("---- updating representation %s for resource %s at %s",
                representationID, resourceID, dataSpaceConnectorHost));
        final var mappedResourceID =
                dataSpaceConnectorResourceMapper.readUUIDFromURI(URI.create(resourceID));
        final var mappedRepresentationID =
                dataSpaceConnectorResourceMapper.getMappedId(URI.create(representationID));
        final var resourceJsonLD = MAPPER.writeValueAsString(resourceRepresentation);
        log.info("---- mapped representation: " + resourceJsonLD);
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
            log.warn(String.format("---- Updating custom resource Representation at %s failed!",
                    dataSpaceConnectorHost));
        }
        final var body = Objects.requireNonNull(response.body()).string();
        log.info("---- Response: " + body);
        return body;
    }

    @Override
    public String updateResourceContract(final String resourceID, final String contract) throws IOException {
        log.info(String.format("---- updating contract for resource at %s",
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
            log.warn(String.format("---- Updating Contract at %s failed!", dataSpaceConnectorHost));
        }
        final var body = Objects.requireNonNull(response.body()).string();
        log.info("---- Response: " + body);
        return body;
    }

    @Override
    public String getPolicyPattern(final String policy) throws IOException {
        log.info(String.format("---- Get pattern for policy"));
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
