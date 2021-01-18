package de.fraunhofer.isst.configmanager.controller;


import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.communication.dataspaceconnector.DataSpaceConnectorResourceMapper;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.configmanagement.service.RepresentationEndpointService;
import de.fraunhofer.isst.configmanager.configmanagement.service.UtilService;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.util.Collection;

/**
 * The controller class implements the ResourceRepresentationApi and offers the possibilities to manage
 * the resource representations in the configurationmanager.
 */
@RestController
@RequestMapping("/api/ui")
@Tag(name = "Resource representation Management", description = "Endpoints for managing the representation of a resource")
public class ResourceRepresentationUIController implements ResourceRepresentationApi {

    private final static Logger logger = LoggerFactory.getLogger(ResourceRepresentationUIController.class);

    private final ConfigModelService configModelService;
    private final UtilService utilService;
    private final DefaultConnectorClient client;
    private final Serializer serializer;
    private final DataSpaceConnectorResourceMapper dataSpaceConnectorResourceMapper;
    private final RepresentationEndpointService representationEndpointService;

    @Autowired
    public ResourceRepresentationUIController(ConfigModelService configModelService,
                                              UtilService utilService,
                                              DefaultConnectorClient client,
                                              Serializer serializer,
                                              DataSpaceConnectorResourceMapper dataSpaceConnectorResourceMapper,
                                              RepresentationEndpointService representationEndpointService) {
        this.client = client;
        this.configModelService = configModelService;
        this.utilService = utilService;
        this.serializer = serializer;
        this.dataSpaceConnectorResourceMapper = dataSpaceConnectorResourceMapper;
        this.representationEndpointService = representationEndpointService;
    }

    /**
     * This method creates a resource representation with the given parameters.
     *
     * @param resourceId        id of the resource
     * @param endpointId        id of the endpoint
     * @param language          the language
     * @param filenameExtension the extension of the file
     * @param bytesize          the size of the representation
     * @param sourceType        the source type of the representation
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> createResourceRepresentation(URI resourceId, URI endpointId, String language,
                                                               String filenameExtension, Long bytesize, String sourceType) {
        if (configModelService.getConfigModel() == null || configModelService.getConfigModel().getAppRoute() == null
                || configModelService.getConfigModel().getConnectorDescription().getResourceCatalog() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Could not find any resources!\"}");
        }

        // Create representation for resource
        Representation representation = new RepresentationBuilder()
                ._language_(Language.valueOf(language))
                ._mediaType_(new IANAMediaTypeBuilder()._filenameExtension_(filenameExtension).build())
                ._instance_(Util.asList(new ArtifactBuilder()
                        ._byteSize_(BigInteger.valueOf(bytesize)).build())).build();
        representation.setProperty("sourceType", sourceType);

        // Add representation in resource catalog
        for (ResourceCatalog resourceCatalog : configModelService.getConfigModel()
                .getConnectorDescription().getResourceCatalog()) {
            for (Resource resource : resourceCatalog.getOfferedResource()) {
                if (resourceId.equals(resource.getId())) {
                    var resourceImpl = (ResourceImpl) resource;
                    resourceImpl.setRepresentation(Util.asList(representation));
                    break;
                }
            }
        }

        // Add resource representation in subroute
        for (AppRoute appRoute : configModelService.getConfigModel().getAppRoute()) {
            if (appRoute.getHasSubRoute() != null) {
                for (RouteStep routeStep : appRoute.getHasSubRoute()) {
                    if (routeStep.getAppRouteOutput() != null) {
                        for (Resource resource : routeStep.getAppRouteOutput()) {
                            if (resourceId.equals(resource.getId())) {
                                var resourceImpl = (ResourceImpl) resource;
                                resourceImpl.setRepresentation(Util.asList(representation));
                                break;
                            }
                        }
                    }
                }
            }
        }

        try {
            configModelService.saveState();
            var response = client.registerResourceRepresentation(resourceId.toString(), representation);
            logger.info(response);

            // Updates the custom resource representation of the connector
            ResponseEntity<String> res =
                    utilService.addEndpointToConnectorRepresentation(endpointId, resourceId, representation);
            logger.info("Response of updates custom resource representation: {}", res);

            // Saves the endpoint id associated with the representation id in the database
            representationEndpointService.createRepresentationEndpoint(endpointId, representation.getId());

            var jsonObject = new JSONObject();
            jsonObject.put("connectorResponse", response);
            jsonObject.put("resourceID", resourceId.toString());
            jsonObject.put("representationID", representation.getId().toString());
            return ResponseEntity.ok(jsonObject.toJSONString());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return ResponseEntity.badRequest().body("Could not create resource representation");
    }

    /**
     * This method updates the resource representation with the given parameters.
     *
     * @param resourceId        id of the resource
     * @param representationId  id of the representation
     * @param endpointId        id of the endpoint
     * @param language          the language
     * @param filenameExtension the extension of the file
     * @param bytesize          the size of the representation
     * @param sourceType        the source type of the representation
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateResourceRepresentation(URI resourceId, URI representationId, URI endpointId,
                                                               String language,
                                                               String filenameExtension, Long bytesize,
                                                               String sourceType) {

        if (configModelService.getConfigModel() == null || configModelService.getConfigModel().getAppRoute() == null
                || configModelService.getConfigModel().getConnectorDescription().getResourceCatalog() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Could not find any resources!\"}");
        }

        RepresentationImpl appRouteCandidate = null;
        for (AppRoute appRoute : configModelService.getConfigModel().getAppRoute()) {
            for (RouteStep routeStep : appRoute.getHasSubRoute()) {
                if (routeStep.getAppRouteOutput() != null) {
                    for (Resource resource : routeStep.getAppRouteOutput()) {
                        if (resourceId.equals(resource.getId())) {
                            for (Representation representation : resource.getRepresentation()) {
                                if (representationId.equals(representation.getId())) {
                                    appRouteCandidate = (RepresentationImpl) representation;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Check if parameters are null and when not update it with new values
        if (appRouteCandidate != null) {
            if (language != null) {
                appRouteCandidate.setLanguage(Language.valueOf(language));
            }
            if (filenameExtension != null) {
                appRouteCandidate.setMediaType(new IANAMediaTypeBuilder()
                        ._filenameExtension_(filenameExtension).build());
            }
            if (bytesize != null) {
                appRouteCandidate.setInstance(Util.asList(new ArtifactBuilder()
                        ._byteSize_(BigInteger.valueOf(bytesize)).build()));
            }
            if (sourceType != null) {
                appRouteCandidate.setProperty("sourceType", sourceType);
            }
        }

        // Determine the correct resource representation from the resource catalog of the connector
        RepresentationImpl catalogCandidate = (RepresentationImpl) configModelService.getConfigModel()
                .getConnectorDescription().getResourceCatalog().stream()
                .map(ResourceCatalog::getOfferedResource)
                .flatMap(Collection::stream)
                .map(DigitalContent::getRepresentation)
                .flatMap(Collection::stream)
                .filter(representation -> representation.getId().equals(representationId))
                .findAny()
                .orElse(null);

        // Check if parameters are null and when not update it with new values
        if (catalogCandidate != null) {
            if (language != null) {
                catalogCandidate.setLanguage(Language.valueOf(language));
            }
            if (filenameExtension != null) {
                catalogCandidate.setMediaType(new IANAMediaTypeBuilder()
                        ._filenameExtension_(filenameExtension).build());
            }
            if (bytesize != null) {
                catalogCandidate.setInstance(Util.asList(new ArtifactBuilder()
                        ._byteSize_(BigInteger.valueOf(bytesize)).build()));
            }
        }
        try {
            // Update the resource representation in the dataspace connector
            if (catalogCandidate != null) {
                var response = client.updateResourceRepresentation(
                        resourceId.toString(),
                        representationId.toString(),
                        catalogCandidate
                );

                // Updates the custom resource representation of the connector
                ResponseEntity<String> res =
                        utilService.addEndpointToConnectorRepresentation(endpointId, resourceId, catalogCandidate);
                logger.info("Response of updates custom resource representation: {}", res);

                configModelService.saveState();
                var jsonObject = new JSONObject();
                jsonObject.put("connectorResponse", response);
                jsonObject.put("resourceID", resourceId.toString());
                jsonObject.put("representationID", representationId.toString());
                return ResponseEntity.ok(jsonObject.toJSONString());
            } else if (appRouteCandidate != null) {
                var response = client.updateResourceRepresentation(
                        resourceId.toString(),
                        representationId.toString(),
                        appRouteCandidate
                );

                ResponseEntity<String> res =
                        utilService.addEndpointToConnectorRepresentation(endpointId, resourceId, appRouteCandidate);
                logger.info("Response of updates custom resource representation: {}", res);
                configModelService.saveState();
                var jsonObject = new JSONObject();
                jsonObject.put("connectorResponse", response);
                jsonObject.put("resourceID", resourceId.toString());
                jsonObject.put("representationID", representationId.toString());
                return ResponseEntity.ok(jsonObject.toJSONString());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No representation with given IDs found!");
            }
        } catch (IOException e) {
            configModelService.saveState();
            logger.error(e.getMessage());
        }
        return ResponseEntity.badRequest().body("Could not update the representation of the resource");
    }

    /**
     * This method returns the specific representation from a resource with the given parameters.
     *
     * @param resourceId       id of the resource
     * @param representationId id of the representation
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getResourceRepresentation(URI resourceId, URI representationId) {

        if (configModelService.getConfigModel() == null || configModelService.getConfigModel().getAppRoute() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Could not find any resources!\"}");
        }
        for (AppRoute appRoute : configModelService.getConfigModel().getAppRoute()) {
            for (Resource resource : appRoute.getAppRouteOutput()) {
                if (resourceId.equals(resource.getId())) {
                    for (Representation representation : resource.getRepresentation()) {
                        if (representationId.equals(representation.getId())) {
                            try {
                                return ResponseEntity.ok(serializer.serialize(representation));
                            } catch (IOException e) {
                                logger.error(e.getMessage());
                            }

                        }
                    }
                }
            }
        }
        return ResponseEntity.badRequest().body("Could not get resource representation");
    }

    /**
     * This method returns the specific representation from a resource in JSON format with the given parameters.
     *
     * @param resourceId       id of the resource
     * @param representationId id of the representation
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getResourceRepresentationInJson(URI resourceId, URI representationId) {

        for (AppRoute appRoute : configModelService.getConfigModel().getAppRoute()) {
            for (Resource resource : appRoute.getAppRouteOutput()) {
                if (resourceId.equals(resource.getId())) {
                    for (Representation representation : resource.getRepresentation()) {
                        if (representationId.equals(representation.getId())) {

                            JSONObject representationJson = new JSONObject();
                            representationJson.put("language", representation.getLanguage());
                            representationJson.put("filenameExtension", representation.getMediaType()
                                    .getFilenameExtension());
                            Artifact artifact = (Artifact) representation.getInstance().get(0);
                            representationJson.put("byteSize", artifact.getByteSize().toString());

                            return ResponseEntity.ok(representationJson.toJSONString());

                        }
                    }
                }
            }
        }
        return ResponseEntity.badRequest().body("Could not get resource representation");
    }

    /**
     * This method deletes the resource representation with the given parameters.
     *
     * @param resourceId       id of the resource
     * @param representationId id of the representation
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> deleteResourceRepresentation(URI resourceId, URI representationId) {

        if (configModelService.getConfigModel() == null || configModelService.getConfigModel().getAppRoute() == null
                || configModelService.getConfigModel().getConnectorDescription().getResourceCatalog() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Could not find any resources!\"}");
        }

        // Delete representation in app route
        var deleted = false;
        Resource foundResource = null;

        for (AppRoute appRoute : configModelService.getConfigModel().getAppRoute()) {
            for (RouteStep routeStep : appRoute.getHasSubRoute()) {
                if (routeStep != null && routeStep.getAppRouteOutput() != null) {
                    for (Resource resource : routeStep.getAppRouteOutput()) {
                        if (resourceId.equals(resource.getId())) {
                            foundResource = resource;
                            break;
                        }
                    }
                }
            }
        }
        if (foundResource != null) {
            deleted |= foundResource.getRepresentation().removeIf(representation -> representation.getId().equals(representationId));
        }

        // Delete representation in catalog
        for (ResourceCatalog resourceCatalog : configModelService.getConfigModel().getConnectorDescription().getResourceCatalog()) {
            if (resourceCatalog != null) {
                var resource = resourceCatalog.getOfferedResource().stream()
                        .filter(resource1 -> resource1.getId().equals(resourceId)).findAny().orElse(null);
                if (resource != null) {
                    deleted |= resource.getRepresentation()
                            .removeIf(representation -> representation.getId().equals(representationId));
                }
            }
        }

        try {
            if (deleted) {
                var response = client.deleteResourceRepresentation(resourceId.toString(),
                        representationId.toString());
                configModelService.saveState();
                var jsonObject = new JSONObject();
                jsonObject.put("connectorResponse", response);
                jsonObject.put("resourceID", resourceId.toString());
                jsonObject.put("representationID", representationId.toString());
                return ResponseEntity.ok(jsonObject.toJSONString());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return ResponseEntity.badRequest().body("Could not delete the resource representation");
    }
}
