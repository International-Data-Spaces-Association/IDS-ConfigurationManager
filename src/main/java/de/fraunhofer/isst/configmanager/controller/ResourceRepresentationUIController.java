package de.fraunhofer.isst.configmanager.controller;


import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.configmanagement.service.RepresentationEndpointService;
import de.fraunhofer.isst.configmanager.configmanagement.service.ResourceService;
import de.fraunhofer.isst.configmanager.util.ValidateApiInput;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
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
 * The controller class implements the ResourceRepresentationApi and offers the possibilities to
 * manage
 * the resource representations in the configuration manager.
 */
@RestController
@Slf4j
@RequestMapping("/api/ui")
@Tag(name = "Resource representation Management", description = "Endpoints for managing the " +
        "representation of a resource")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ResourceRepresentationUIController implements ResourceRepresentationApi {
    transient ConfigModelService configModelService;
    transient RepresentationEndpointService representationEndpointService;
    transient ResourceService resourceService;
    transient DefaultConnectorClient client;
    transient Serializer serializer;

    @Autowired
    public ResourceRepresentationUIController(final ConfigModelService configModelService,
                                              final ResourceService resourceService,
                                              final DefaultConnectorClient client,
                                              final Serializer serializer,
                                              final RepresentationEndpointService representationEndpointService) {
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
    public ResponseEntity<String> createResourceRepresentation(final URI resourceId,
                                                               final URI endpointId,
                                                               final String language,
                                                               final String filenameExtension,
                                                               final Long bytesize,
                                                               final String sourceType) {
        log.info(">> POST /resource/representation resourceId: " + resourceId + " endpointId: " + endpointId + " language: " + language
                + " filenameExtension: " + filenameExtension + " bytesize: " + bytesize + " " +
                "sourceType: " + sourceType);

        if (ValidateApiInput.notValid(resourceId.toString(), sourceType)) {
            return ResponseEntity.badRequest().body("All validated parameter have undefined as " +
                    "value!");
        }

        if (resourceService.getResources() == null || resourceService.getResources().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Could not find " +
                    "any resources!\"}");
        }

        // Create representation for resource
        final var representation = new RepresentationBuilder()
                ._language_(Language.valueOf(language))
                ._mediaType_(new IANAMediaTypeBuilder()._filenameExtension_(filenameExtension).build())
                ._instance_(Util.asList(new ArtifactBuilder()
                        ._byteSize_(BigInteger.valueOf(bytesize)).build())).build();
        representation.setProperty("ids:sourceType", sourceType);

        final var jsonObject = new JSONObject();
        try {
            configModelService.saveState();
            jsonObject.put("resourceID", resourceId.toString());
            jsonObject.put("representationID", representation.getId().toString());

            final var response = client.registerResourceRepresentation(resourceId.toString(),
                    representation, endpointId.toString());
            jsonObject.put("connectorResponse", response);
            representationEndpointService.createRepresentationEndpoint(endpointId,
                    representation.getId());
            return ResponseEntity.ok(jsonObject.toJSONString());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            jsonObject.put("message", "Could not register the resource representation at the " +
                    "connector");
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
    public ResponseEntity<String> updateResourceRepresentation(final URI resourceId,
                                                               final URI representationId,
                                                               final URI endpointId,
                                                               final String language,
                                                               final String filenameExtension,
                                                               final Long bytesize,
                                                               final String sourceType) {
        log.info(">> PUT /resource/representation resourceId: " + resourceId + " representationId" +
                ": " + representationId
                + " endpointId: " + endpointId + " language: " + language + " filenameExtension: "
                + filenameExtension + " bytesize: " + bytesize
                + " sourceType: " + sourceType);

        final var oldResourceCatalog = (ResourceImpl) resourceService.getResource(resourceId);
        if (oldResourceCatalog != null) {
            final var oldRepresentationId = oldResourceCatalog.getRepresentation().get(0).getId();
            oldResourceCatalog.setRepresentation(null);
            if (configModelService.getConfigModel().getAppRoute() == null) {
                log.info("---- No AppRoute in ConfigModel!");
            } else {
                final var oldResourceRoute =
                        (ResourceImpl) resourceService.getResourceInAppRoute(resourceId);
                if (oldResourceRoute != null) {
                    oldResourceRoute.setRepresentation(null);
                }
            }
            // Create representation for resource
            final var representation = new RepresentationBuilder(oldRepresentationId).build();
            final var representationImpl = (RepresentationImpl) representation;
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
                for (var appRoute : configModelService.getConfigModel().getAppRoute()) {
                    for (var routeStep : appRoute.getHasSubRoute()) {
                        for (var resource : routeStep.getAppRouteOutput()) {
                            if (resourceId.equals(resource.getId())) {
                                final var resourceImpl = (ResourceImpl) resource;
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
                    final var response = client.updateResourceRepresentation(
                            resourceId.toString(),
                            representationId.toString(),
                            representationImpl,
                            endpointId.toString()
                    );
                    final var jsonObject = new JSONObject();
                    jsonObject.put("connectorResponse", response);
                    jsonObject.put("resourceID", resourceId.toString());
                    jsonObject.put("representationID", representationId.toString());
                    return ResponseEntity.ok(jsonObject.toJSONString());
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No representation " +
                            "with given IDs found!");
                }
            } catch (IOException e) {
                configModelService.saveState();
                log.error(e.getMessage(), e);
            }
        }
        return ResponseEntity.badRequest().body("Could not update the representation of the " +
                "resource");
    }

    /**
     * This method returns the specific representation from a resource with the given parameters.
     *
     * @param representationId id of the representation
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getResourceRepresentation(final URI representationId) {
        log.info(">> GET /resource/representation representationId: " + representationId);

        final var representation =
                resourceService.getResourceRepresentationInCatalog(representationId);
        if (representation != null) {
            try {
                return ResponseEntity.ok(serializer.serialize(representation));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems " +
                        "while serializing the " +
                        "representation");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not get resource representation");

        }
    }

    /**
     * This method returns the specific representation from a resource in JSON format with the
     * given parameters.
     *
     * @param resourceId       id of the resource
     * @param representationId id of the representation
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getResourceRepresentationInJson(final URI resourceId,
                                                                  final URI representationId) {
        log.info(">> GET /resource/representation/json resourceId: " + resourceId + " " +
                "representationId: " + representationId);

        for (var resource : resourceService.getResources()) {
            if (resourceId.equals(resource.getId())) {
                for (var representation : resource.getRepresentation()) {
                    if (representationId.equals(representation.getId())) {

                        final var representationJson = new JSONObject();
                        representationJson.put("language", representation.getLanguage());
                        representationJson.put("filenameExtension", representation.getMediaType()
                                .getFilenameExtension());
                        final var artifact = (Artifact) representation.getInstance().get(0);
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
    public ResponseEntity<String> deleteResourceRepresentation(final URI resourceId,
                                                               final URI representationId) {
        log.info(">> DELETE /resource/representation resourceId: " + resourceId + " " +
                "representationId: " + representationId);
        try {
            final var response = client.deleteResourceRepresentation(resourceId.toString(),
                    representationId.toString());
            resourceService.deleteResourceRepresentationFromAppRoute(resourceId, representationId);
            final var jsonObject = new JSONObject();
            jsonObject.put("connectorResponse", response);
            jsonObject.put("resourceID", resourceId.toString());
            jsonObject.put("representationID", representationId.toString());
            return ResponseEntity.ok(jsonObject.toJSONString());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body("Problems while deleting the representation " +
                    "at the connector");
        }
    }
}
