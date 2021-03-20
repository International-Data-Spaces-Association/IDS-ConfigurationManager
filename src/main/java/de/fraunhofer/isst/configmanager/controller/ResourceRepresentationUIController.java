package de.fraunhofer.isst.configmanager.controller;


import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.configmanagement.service.RepresentationEndpointService;
import de.fraunhofer.isst.configmanager.configmanagement.service.ResourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;

/**
 * The controller class implements the ResourceRepresentationApi and offers the possibilities to manage
 * the resource representations in the configuration manager.
 */
@RestController
@Slf4j
@RequestMapping("/api/ui")
@Tag(name = "Resource representation Management", description = "Endpoints for managing the representation of a resource")
public class ResourceRepresentationUIController implements ResourceRepresentationApi {
    private transient final ConfigModelService configModelService;
    private transient final RepresentationEndpointService representationEndpointService;
    private transient final ResourceService resourceService;
    private transient final DefaultConnectorClient client;
    private transient final Serializer serializer;

    @Autowired
    public ResourceRepresentationUIController(ConfigModelService configModelService,
                                              ResourceService resourceService,
                                              DefaultConnectorClient client,
                                              Serializer serializer,
                                              RepresentationEndpointService representationEndpointService) {
        this.client = client;
        this.configModelService = configModelService;
        this.resourceService = resourceService;
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
        log.info(">> POST /resource/representation resourceId: " + resourceId + " endpointId: " + endpointId + " language: " + language
                + " filenameExtension: " + filenameExtension + " bytesize: " + bytesize + " sourceType: " + sourceType);

        if (resourceService.getResources() == null || resourceService.getResources().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Could not find any resources!\"}");
        }

        // Create representation for resource
        Representation representation = new RepresentationBuilder()
                ._language_(Language.valueOf(language))
                ._mediaType_(new IANAMediaTypeBuilder()._filenameExtension_(filenameExtension).build())
                ._instance_(Util.asList(new ArtifactBuilder()
                        ._byteSize_(BigInteger.valueOf(bytesize)).build())).build();
        representation.setProperty("ids:sourceType", sourceType);

        var jsonObject = new JSONObject();
        try {
            configModelService.saveState();
            jsonObject.put("resourceID", resourceId.toString());
            jsonObject.put("representationID", representation.getId().toString());

            var response = client.registerResourceRepresentation(resourceId.toString(), representation, endpointId.toString());
            jsonObject.put("connectorResponse", response);
            representationEndpointService.createRepresentationEndpoint(endpointId, representation.getId());
            return ResponseEntity.ok(jsonObject.toJSONString());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
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
        log.info(">> PUT /resource/representation resourceId: " + resourceId + " representationId: " + representationId
        + " endpointId: " + endpointId + " language: " + language + " filenameExtension: " + filenameExtension + " bytesize: " + bytesize
        + " sourceType: " + sourceType);

        ResourceImpl oldResourceCatalog = (ResourceImpl) resourceService.getResource(resourceId);
        if (oldResourceCatalog != null) {
            URI oldRepresentationId = oldResourceCatalog.getRepresentation().get(0).getId();
            oldResourceCatalog.setRepresentation(null);
            if (configModelService.getConfigModel().getAppRoute() == null) {
                log.info("---- No AppRoute in ConfigModel!");
            } else {
                ResourceImpl oldResourceRoute = (ResourceImpl) resourceService.getResourceInAppRoute(resourceId);
                if (oldResourceRoute != null) {
                    oldResourceRoute.setRepresentation(null);
                }
            }
            // Create representation for resource
            Representation representation = new RepresentationBuilder(oldRepresentationId).build();
            var representationImpl = (RepresentationImpl) representation;
            if (language != null) {
                representationImpl.setLanguage(Language.valueOf(language));
            }
            if (filenameExtension != null) {
                representationImpl.setMediaType(new IANAMediaTypeBuilder()
                        ._filenameExtension_(filenameExtension).build());
            }
            if (bytesize != null) {
                representationImpl.setInstance(Util.asList(new ArtifactBuilder()
                        ._byteSize_(BigInteger.valueOf(bytesize)).build()));
            }
            if (sourceType != null) {
                representationImpl.setProperty("ids:sourceType", sourceType);
            }
            // Update representation in app route
            if (configModelService.getConfigModel().getAppRoute() != null) {
                for (AppRoute appRoute : configModelService.getConfigModel().getAppRoute()) {
                    for (RouteStep routeStep : appRoute.getHasSubRoute()) {
                        for (Resource resource : routeStep.getAppRouteOutput()) {
                            if (resourceId.equals(resource.getId())) {
                                var resourceImpl = (ResourceImpl) resource;
                                resourceImpl.setRepresentation(Util.asList(representationImpl));
                                break;
                            }
                        }
                    }
                }
            }
            // Update the backend connection to the new endpoint
            resourceService.updateBackendConnection(resourceId, endpointId);

            try {
                // Update the resource representation in the dataspace connector
                if (representationImpl != null) {
                    var response = client.updateResourceRepresentation(
                            resourceId.toString(),
                            representationId.toString(),
                            representationImpl,
                            endpointId.toString()
                    );
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
                log.error(e.getMessage(), e);
            }
        }
        return ResponseEntity.badRequest().body("Could not update the representation of the resource");
    }

    /**
     * This method returns the specific representation from a resource with the given parameters.
     *
     * @param representationId id of the representation
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getResourceRepresentation(URI representationId) {
        log.info(">> GET /resource/representation representationId: " + representationId);

        RepresentationImpl representation = resourceService.getResourceRepresentationInCatalog(representationId);
        if (representation != null) {
            try {
                return ResponseEntity.ok(serializer.serialize(representation));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems while serializing the " +
                        "representation");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not get resource representation");

        }
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
        log.info(">> GET /resource/representation/json resourceId: " + resourceId + " representationId: " + representationId);

            for (Resource resource : resourceService.getResources()) {
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
        log.info(">> DELETE /resource/representation resourceId: " + resourceId + " representationId: " + representationId);
        try {
            var response = client.deleteResourceRepresentation(resourceId.toString(), representationId.toString());
            resourceService.deleteResourceRepresentationFromAppRoute(resourceId, representationId);
            var jsonObject = new JSONObject();
            jsonObject.put("connectorResponse", response);
            jsonObject.put("resourceID", resourceId.toString());
            jsonObject.put("representationID", representationId.toString());
            return ResponseEntity.ok(jsonObject.toJSONString());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body("Problems while deleting the representation at the connector");
        }
    }
}
