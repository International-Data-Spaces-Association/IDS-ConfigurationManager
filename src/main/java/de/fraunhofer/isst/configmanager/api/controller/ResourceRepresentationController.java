/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.isst.configmanager.api.controller;

import de.fraunhofer.iais.eis.ArtifactBuilder;
import de.fraunhofer.iais.eis.IANAMediaTypeBuilder;
import de.fraunhofer.iais.eis.Language;
import de.fraunhofer.iais.eis.RepresentationBuilder;
import de.fraunhofer.iais.eis.RepresentationImpl;
import de.fraunhofer.iais.eis.ResourceImpl;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.api.ResourceRepresentationApi;
import de.fraunhofer.isst.configmanager.api.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.api.service.resources.ResourceRepresentationService;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultResourceClient;
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
 * The api class implements the ResourceRepresentationApi and offers the possibilities to
 * manage the resource representations in the configuration manager.
 */
@Slf4j
@RestController
@RequestMapping("/api/ui")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "Resource representation Management", description = "Endpoints for managing the representation of a resource")
public class ResourceRepresentationController implements ResourceRepresentationApi {

    transient ConfigModelService configModelService;
    transient ResourceRepresentationService resourceRepresentationService;
    transient DefaultResourceClient client;

    @Autowired
    public ResourceRepresentationController(final ConfigModelService configModelService,
                                            final ResourceRepresentationService resourceRepresentationService,
                                            final DefaultResourceClient client) {
        this.client = client;
        this.configModelService = configModelService;
        this.resourceRepresentationService = resourceRepresentationService;
    }

    /**
     * This method creates a resource representation with the given parameters.
     *
     * @param resourceId        id of the resource
     * @param endpointId        id of the endpoint
     * @param language          the language
     * @param filenameExtension the extension of the file
     * @param bytesize          the size of the representation
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> createResourceRepresentation(final URI resourceId,
                                                               final URI endpointId,
                                                               final String language,
                                                               final String filenameExtension,
                                                               final Long bytesize) {
        if (log.isInfoEnabled()) {
            log.info(">> POST /resource/representation resourceId: " + resourceId + " endpointId: " + endpointId + " language: " + language
                    + " filenameExtension: " + filenameExtension + " bytesize: " + bytesize);
        }
        ResponseEntity<String> response;

        if (ValidateApiInput.notValid(resourceId.toString())) {
            response = ResponseEntity.badRequest().body("All validated parameter have undefined as value!");
        } else {
            if (resourceRepresentationService.getResources() == null || resourceRepresentationService.getResources().isEmpty()) {
                response = ResponseEntity.status(HttpStatus.NOT_FOUND).body("Could not find any resources!");
            } else {
                final var representation = new RepresentationBuilder()
                        ._language_(Language.valueOf(language))
                        ._mediaType_(new IANAMediaTypeBuilder()._filenameExtension_(filenameExtension).build())
                        ._instance_(Util.asList(new ArtifactBuilder()
                                ._byteSize_(BigInteger.valueOf(bytesize)).build())).build();

                final var jsonObject = new JSONObject();

                try {
                    configModelService.saveState();
                    jsonObject.put("resourceID", resourceId.toString());
                    jsonObject.put("representationID", representation.getId().toString());

                    final var clientResponse = client.registerResourceRepresentation(resourceId.toString(),
                            representation, endpointId.toString());

                    jsonObject.put("connectorResponse", clientResponse);
                    response = ResponseEntity.ok(jsonObject.toJSONString());
                } catch (IOException e) {
                    if (log.isErrorEnabled()) {
                        log.error(e.getMessage(), e);
                    }
                    jsonObject.put("message", "Could not register the resource representation at the connector");
                    response = ResponseEntity.badRequest().body(jsonObject.toJSONString());
                }
            }
        }

        return response;
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
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateResourceRepresentation(final URI resourceId,
                                                               final URI representationId,
                                                               final URI endpointId,
                                                               final String language,
                                                               final String filenameExtension,
                                                               final Long bytesize){
        if (log.isInfoEnabled()) {
            log.info(">> PUT /resource/representation resourceId: " + resourceId + " representationId: "
                    + representationId + " endpointId: " + endpointId + " language: " + language + " filenameExtension: "
                    + filenameExtension + " bytesize: " + bytesize);
        }
        ResponseEntity<String> response = null;

        final var oldResourceCatalog = (ResourceImpl) resourceRepresentationService.getResource(resourceId);

        if (oldResourceCatalog != null) {
            final var oldRepresentationId = oldResourceCatalog.getRepresentation().get(0).getId();
            oldResourceCatalog.setRepresentation(null);

            if (configModelService.getConfigModel().getAppRoute() == null) {
                if (log.isInfoEnabled()) {
                    log.info("---- [ResourceRepresentationController updateResourceRepresentation] No AppRoute in ConfigModel!");
                }
            } else {
                final var oldResourceRoute = (ResourceImpl) resourceRepresentationService.getResourceInAppRoute(resourceId);
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
                representationImpl.setMediaType(new IANAMediaTypeBuilder()._filenameExtension_(filenameExtension).build());
            }
            if (bytesize != null) {
                representationImpl.setInstance(Util.asList(new ArtifactBuilder()._byteSize_(BigInteger.valueOf(bytesize)).build()));
            }
            // Update representation in app route
            if (configModelService.getConfigModel().getAppRoute() != null) {
                for (final var appRoute : configModelService.getConfigModel().getAppRoute()) {
                    for (final var routeStep : appRoute.getHasSubRoute()) {
                        for (final var resource : routeStep.getAppRouteOutput()) {
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
            resourceRepresentationService.updateBackendConnection(resourceId, endpointId);

            try {
                // Update the resource representation in the dataspace connector
                if (representationImpl != null) {
                    final var clientResponse = client.updateResourceRepresentation(
                            resourceId.toString(),
                            representationId.toString(),
                            representationImpl,
                            endpointId.toString()
                    );
                    final var jsonObject = new JSONObject();
                    jsonObject.put("connectorResponse", clientResponse);
                    jsonObject.put("resourceID", resourceId.toString());
                    jsonObject.put("representationID", representationId.toString());

                    response = ResponseEntity.ok(jsonObject.toJSONString());
                } else {
                    response = ResponseEntity.status(HttpStatus.NOT_FOUND).body("No representation with given IDs found!");
                }
            } catch (IOException e) {
                configModelService.saveState();
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        if (response == null) {
            response = ResponseEntity.badRequest().body("Could not update the representation of the resource");
        }

        return response;
    }
}
