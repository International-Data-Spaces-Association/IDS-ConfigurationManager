package de.fraunhofer.isst.configmanager.controller;


import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
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
 * the resource representations in the configuration manager.
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
    private final RepresentationEndpointService representationEndpointService;

    @Autowired
    public ResourceRepresentationUIController(ConfigModelService configModelService,
                                              UtilService utilService,
                                              DefaultConnectorClient client,
                                              Serializer serializer,
                                              RepresentationEndpointService representationEndpointService) {
        this.client = client;
        this.configModelService = configModelService;
        this.utilService = utilService;
        this.serializer = serializer;
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
        if (configModelService.getConfigModel() == null ||
                configModelService.getConfigModel().getConnectorDescription().getResourceCatalog() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Could not find any resources!\"}");
        }

        // Create representation for resource
        Representation representation = new RepresentationBuilder()
                ._language_(Language.valueOf(language))
                ._mediaType_(new IANAMediaTypeBuilder()._filenameExtension_(filenameExtension).build())
                ._instance_(Util.asList(new ArtifactBuilder()
                        ._byteSize_(BigInteger.valueOf(bytesize)).build())).build();
        representation.setProperty("ids:sourceType", sourceType);

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

        var jsonObject = new JSONObject();
        try {
            configModelService.saveState();
            jsonObject.put("resourceID", resourceId.toString());
            jsonObject.put("representationID", representation.getId().toString());

            var response = client.registerResourceRepresentation(resourceId.toString(), representation);
            jsonObject.put("connectorResponse", response);

            // Updates the custom resource representation of the connector
            ResponseEntity<String> res =
                    utilService.addEndpointToConnectorRepresentation(endpointId, resourceId, representation);
            logger.info("Response of updates custom resource representation: {}", res);

            // Saves the endpoint id associated with the representation id in the database
            representationEndpointService.createRepresentationEndpoint(endpointId, representation.getId());

            return ResponseEntity.ok(jsonObject.toJSONString());
        } catch (IOException e) {
            logger.error(e.getMessage());
            jsonObject.put("message", "Could not register the resource representation at the connector");
            return ResponseEntity.badRequest().body(jsonObject.toJSONString());
        }
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

        if (configModelService.getConfigModel() == null
                || configModelService.getConfigModel().getConnectorDescription().getResourceCatalog() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Could not find any resources!\"}");
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
        updateRepresentation(language, filenameExtension, bytesize, sourceType, catalogCandidate);

        // Try to update resource representation in the app routes, if it exists
        if (configModelService.getConfigModel().getAppRoute() == null) {
            logger.info("Could not find any app route to update the resource representation");
        } else {
            RepresentationImpl appRouteCandidate = null;
            for (AppRoute appRoute : configModelService.getConfigModel().getAppRoute()) {
                if (appRoute.getHasSubRoute() != null) {
                    for (RouteStep routeStep : appRoute.getHasSubRoute()) {
                        if (routeStep.getAppRouteOutput() != null) {
                            for (Resource resource : routeStep.getAppRouteOutput()) {
                                if (resourceId.equals(resource.getId())) {
                                    if (resource.getRepresentation() != null) {
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
                }
            }

            // Check if parameters are null and when not update it with new values
            updateRepresentation(language, filenameExtension, bytesize, sourceType, appRouteCandidate);
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

        if (configModelService.getConfigModel() == null ||
                configModelService.getConfigModel().getConnectorDescription().getResourceCatalog() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Could not find any resources!\"}");
        }
        for (ResourceCatalog resourceCatalog : configModelService.getConfigModel().getConnectorDescription().getResourceCatalog()) {
            for (Resource resource : resourceCatalog.getOfferedResource()) {
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

        for (ResourceCatalog resourceCatalog : configModelService.getConfigModel().getConnectorDescription().getResourceCatalog()) {
            for (Resource resource : resourceCatalog.getOfferedResource()) {
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

        if (configModelService.getConfigModel() == null
                || configModelService.getConfigModel().getConnectorDescription().getResourceCatalog() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Could not find any resources!\"}");
        }

        var deleted = false;

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

        // Delete representation in app route if exists
        if (configModelService.getConfigModel().getAppRoute() == null) {
            logger.info("No app route found to delete the resource representation");
        } else {
            Resource foundresource = null;
            for (AppRoute appRoute : configModelService.getConfigModel().getAppRoute()) {
                if (appRoute.getHasSubRoute() != null) {
                    for (RouteStep routeStep : appRoute.getHasSubRoute()) {
                        if (routeStep.getAppRouteOutput() != null) {
                            for (Resource resource : routeStep.getAppRouteOutput()) {
                                if (resourceId.equals(resource.getId())) {
                                    foundresource = resource;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (foundresource != null) {
                deleted |= foundresource.getRepresentation()
                        .removeIf(representation -> representation.getId().equals(representationId));
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

    /**
     * @param language           language of the representation
     * @param filenameExtension  filename extension
     * @param bytesize           byte size of the representation
     * @param sourceType         source type of the representation
     * @param representationImpl representation implementation
     */
    private void updateRepresentation(String language, String filenameExtension, Long bytesize, String sourceType,
                                      RepresentationImpl representationImpl) {
        if (representationImpl != null) {
            if (language != null) {
                representationImpl.setLanguage(Language.valueOf(language));
            }
            if (filenameExtension != null) {
                representationImpl.setMediaType(null);
                representationImpl.setMediaType(new IANAMediaTypeBuilder()
                        ._filenameExtension_(filenameExtension).build());
            }
            if (bytesize != null) {
                representationImpl.setInstance(null);
                representationImpl.setInstance(Util.asList(new ArtifactBuilder()
                        ._byteSize_(BigInteger.valueOf(bytesize)).build()));
            }
            if (sourceType != null) {
                representationImpl.setProperty("ids:sourceType", sourceType);
            }
        }
    }
}
