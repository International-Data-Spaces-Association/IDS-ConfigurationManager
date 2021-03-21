package de.fraunhofer.isst.configmanager.communication.dataspaceconnector;

import de.fraunhofer.iais.eis.Artifact;
import de.fraunhofer.iais.eis.BasicAuthenticationImpl;
import de.fraunhofer.iais.eis.GenericEndpoint;
import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model.BackendSource;
import de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model.ResourceIDPair;
import de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model.ResourceMetadata;
import de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model.ResourceRepresentation;
import de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model.repos.ResourceIDPairRepository;
import de.fraunhofer.isst.configmanager.configmanagement.service.EndpointService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The service class helps to map the resource model from the information model to the resource
 * model from the
 * dataspace connector.
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DataSpaceConnectorResourceMapper {

    static Serializer SERIALIZER = new Serializer();
    /**
     * Pattern is created, which has the following structure:
     * Example:
     * URI: https://w3id.org/idsa/autogen/configurationModel/9abd295d-b96f-49fa-8c10-a64179c24049 ->
     * UUID_REGEX: 9abd295d-b96f-49fa-8c10-a64179c24049
     */
    static Pattern UUID_REGEX = Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a" +
            "-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");
    transient EndpointService endpointService;
    transient ResourceIDPairRepository resourceIDPairRepository;


    public DataSpaceConnectorResourceMapper(final ResourceIDPairRepository resourceIDPairRepository,
                                            final EndpointService endpointService) {
        this.resourceIDPairRepository = resourceIDPairRepository;
        this.endpointService = endpointService;
    }

    /**
     * The method return with the help of the uri from a resource the uuid.
     *
     * @param id of the resource
     * @return uuid of the resource
     */
    public UUID getMappedId(final URI id) {
        final var pairs = resourceIDPairRepository.findByUri(id);
        if (pairs.isEmpty()) {
            return null;
        }
        //uri is set to unique
        return pairs.get(0).getUuid();
    }

    /**
     * The method cuts the last part of an uri. For example:
     * URI: https://w3id.org/idsa/autogen/configurationModel/9abd295d-b96f-49fa-8c10-a64179c24049 ->
     * UUID_REGEX: 9abd295d-b96f-49fa-8c10-a64179c24049
     *
     * @param id of the resource
     * @return uuid of the resource
     */
    public UUID readUUIDFromURI(final URI id) {
        final var path = id.getPath();
        final var idStr = path.substring(path.lastIndexOf('/') + 1);
        return UUID.fromString(idStr);
    }

    /**
     * This method takes the response from the connector and generates a ResourceIDPair, which
     * holds the uri
     * and the uuid. This is be persisted in the database and finally the method returns the
     * extracted uuid
     * from the response.
     *
     * @param response of the connector
     * @param id       of the resource
     * @return uuid
     */
    public UUID createFromResponse(final String response, final URI id) {
        final var matcher = UUID_REGEX.matcher(response);
        if (matcher.find()) {
            final var uuidString = matcher.group(0);
            final var uuid = UUID.fromString(uuidString);
            final var pair = new ResourceIDPair(uuid, id);
            resourceIDPairRepository.saveAndFlush(pair);
            return uuid;
        }
        return null;
    }

    /**
     * The method helps to map resource metadata from the information model to  resource metadata
     * from the
     * dataspace connector.
     *
     * @param resource object
     * @return resource metadata object
     * @throws IOException if policy can not be serialized
     */
    public ResourceMetadata getMetadata(final Resource resource) throws IOException {
        final var metadata = new ResourceMetadata();
        metadata.setDescription(resource.getDescription().stream().map(RdfResource::getValue).collect(Collectors.joining(";")));
        metadata.setKeywords(resource.getKeyword().stream().map(RdfResource::getValue).collect(Collectors.toList()));
        metadata.setLicense(resource.getStandardLicense());
        metadata.setOwner(resource.getPublisher());
        metadata.setVersion(resource.getVersion());
        if (resource.getContractOffer() != null && !resource.getContractOffer().isEmpty()) {
            metadata.setPolicy(SERIALIZER.serialize(resource.getContractOffer()));
        }
        metadata.setRepresentations(mapRepresentations(resource.getRepresentation()));
        metadata.setTitle(resource.getTitle().stream().map(RdfResource::getValue).collect(Collectors.joining(";")));
        return metadata;
    }

    /**
     * The method maps a list of representation to the resource representation model from the
     * dataspace connector.
     *
     * @param representations list
     * @return list of mapped resource representations
     */
    private List<ResourceRepresentation> mapRepresentations(final List<? extends Representation> representations) {
        if (representations == null) {
            return List.of();
        }
        return representations.stream().map(this::mapRepresentation).collect(Collectors.toList());
    }

    /**
     * The method maps the representation from the information model to a representation object
     * of a dataspace connector.
     *
     * @param representation
     * @return resource representation
     */
    public ResourceRepresentation mapRepresentation(final Representation representation) {
        final var resourceRepresentation = new ResourceRepresentation();
        resourceRepresentation.setUuid(readUUIDFromURI(representation.getId()));
        int byteSize = 0;
        if (representation.getInstance() != null && !representation.getInstance().isEmpty()) {
            final var artifact = (Artifact) representation.getInstance().get(0);
            byteSize = artifact.getByteSize().intValue();
        }
        resourceRepresentation.setByteSize(byteSize);
        resourceRepresentation.setType(representation.getMediaType().getFilenameExtension());
        return resourceRepresentation;
    }

    /**
     * The method resolves the source type of a representation.
     *
     * @param representation
     * @return a source type
     */
    private BackendSource.Type resolveSourceType(final Representation representation) {
        BackendSource.Type sourceType;
        final var typedLiteral = (TypedLiteral) representation.getProperties()
                .getOrDefault("https://w3id.org/idsa/core/sourceType", null);
        if (typedLiteral != null) {
            sourceType = BackendSource.Type.valueOf(typedLiteral.getValue());
        } else {
            final var propName = (String) representation.getProperties().get("ids:sourceType");
            sourceType = BackendSource.Type.valueOf(propName);
        }
        return sourceType;
    }

    /**
     * The method deletes the ResourceIDPair object from the database.
     *
     * @param id of the resource
     */
    public void deleteResourceIDPair(final URI id) {
        final var pairs = resourceIDPairRepository.findByUri(id);
        if (pairs.isEmpty()) {
            return;
        }
        pairs.forEach(pair -> resourceIDPairRepository.delete(pair));
        resourceIDPairRepository.flush();
    }

    /**
     * This method creates a new backend source for the representation
     *
     * @param endpointId     id of the endpoint
     * @param representation representation
     * @return backend source
     */
    public BackendSource createBackendSource(final String endpointId,
                                             final Representation representation) {
        final var backendSource = new BackendSource();
        final var endpoint = (GenericEndpoint) endpointService.getGenericEndpoints()
                .stream()
                .filter(endP -> endP.getId().equals(URI.create(endpointId))).findAny().orElse(null);

        if (endpoint != null) {
            final var basicAuth =
                    (BasicAuthenticationImpl) endpoint.getGenericEndpointAuthentication();
            if (basicAuth != null) {
                backendSource.setPassword(basicAuth.getAuthPassword());
                backendSource.setUrl(URI.create(endpoint.getAccessURL().toString()));
                backendSource.setUsername(basicAuth.getAuthUsername());
            } else {
                backendSource.setPassword("");
                backendSource.setUrl(URI.create("https://example.com"));
                backendSource.setUsername("");
            }
        }
        backendSource.setType(resolveSourceType(representation));
        return backendSource;
    }
}
