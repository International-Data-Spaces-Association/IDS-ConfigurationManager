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

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.api.ResourceApi;
import de.fraunhofer.isst.configmanager.api.service.BrokerService;
import de.fraunhofer.isst.configmanager.api.service.resources.ResourceService;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultBrokerClient;
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
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * The api class implements the ResourceApi and offers the possibilities to manage
 * the resources in the configuration manager.
 */

@Slf4j
@RestController
@RequestMapping("/api/ui")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "Resource Management", description = "Endpoints for managing the resource in the configuration manager")
public class ResourceController implements ResourceApi {

    transient ResourceService resourceService;
    transient DefaultResourceClient client;
    transient DefaultBrokerClient brokerClient;
    transient BrokerService brokerService;
    transient Serializer serializer;

    @Autowired
    public ResourceController(final ResourceService resourceService,
                              final DefaultResourceClient client,
                              final DefaultBrokerClient brokerClient,
                              final BrokerService brokerService,
                              final Serializer serializer) {
        this.resourceService = resourceService;
        this.brokerClient = brokerClient;
        this.client = client;
        this.serializer = serializer;
        this.brokerService = brokerService;
    }

    /**
     * This method returns a resource from the connector with the given parameter.
     *
     * @param resourceId id of the resource
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getResource(final URI resourceId) {
        if (log.isInfoEnabled()) {
            log.info(">> GET /resource resourceId: " + resourceId);
        }
        ResponseEntity<String> response;

        if (ValidateApiInput.notValid(resourceId.toString())) {
            response = ResponseEntity.badRequest().body("All validated parameter have undefined as value!");
        } else {
            final var resource = resourceService.getResource(resourceId);

            if (resource != null) {
                try {
                    response = ResponseEntity.ok(serializer.serialize(resource));
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            } else {
                response = ResponseEntity.badRequest().body("Could not determine the resource");
            }
        }

        return response;
    }

    /**
     * This method returns all resources from the connector.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getResources() {
        if (log.isInfoEnabled()) {
            log.info(">> GET /resources");
        }
        ResponseEntity<String> response;
        final var resources = resourceService.getResources();
        try {
            response = ResponseEntity.ok(serializer.serialize(resources));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return response;
    }

    /**
     * This method returns a http response with all requested resources
     *
     * @return response with requested resources from the connector
     */
    @Override
    public ResponseEntity<String> getRequestedResources() {
        if (log.isInfoEnabled()) {
            log.info(">> GET /resources/requested");
        }
        ResponseEntity<String> response;
        final var resources = resourceService.getRequestedResources();
        try {
            response = ResponseEntity.ok(serializer.serialize(resources));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return response;
    }

    /**
     * This method deletes the resource from the connector and the app route with the given
     * parameter.
     * If both are deleted the dataspace connector is informed about the change.
     *
     * @param resourceId id of the resource
     * @return http response from the target connector
     */
    @Override
    public ResponseEntity<String> deleteResource(final URI resourceId) {
        if (log.isInfoEnabled()) {
            log.info(">> DELETE /resource resourceId: " + resourceId);
        }
        ResponseEntity<String> response;

        if (ValidateApiInput.notValid(resourceId.toString())) {
            response = ResponseEntity.badRequest().body("All validated parameter have undefined as value!");
        } else {
            try {
                final var clientResponse = client.deleteResource(resourceId);
                resourceService.deleteResourceFromAppRoute(resourceId);

                final var jsonObject = new JSONObject();
                jsonObject.put("connectorResponse", clientResponse);
                jsonObject.put("resourceID", resourceId.toString());

                response = ResponseEntity.ok(jsonObject.toJSONString());
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }

                response = ResponseEntity.badRequest().body("Could not send delete request to connector");
            }
        }

        return response;
    }

    /**
     * This method creates a resource with the given parameters. The special feature here is that
     * the created resource
     * is included once in the app route and once in the resource catalog of the connector.
     *
     * @param title           title of the resource
     * @param description     description of the resource
     * @param language        language of the resource
     * @param keywords        keywords for the resource
     * @param version         version of the resource
     * @param standardlicense standard license for the resource
     * @param publisher       the publisher of the resource
     * @return response from the target connector
     */
    @Override
    public ResponseEntity<String> createResource(final String title,
                                                 final String description,
                                                 final String language,
                                                 final ArrayList<String> keywords,
                                                 final String version,
                                                 final URI standardlicense,
                                                 final URI publisher) {
        if (log.isInfoEnabled()) {
            log.info(">> POST /resource title: " + title + " description: " + description
                    + " language: " + language + " keywords: " + keywords + " version: " + version
                    + " standardlicense: " + standardlicense + " publisher: " + publisher);
        }
        ResponseEntity<String> response;

        if (ValidateApiInput.notValid(title, description, language, version, standardlicense.toString(), publisher.toString())) {
            response = ResponseEntity.badRequest().body("All validated parameter have undefined as value!");
        } else {
            final var resource = resourceService.createResource(title, description, language,
                    keywords,
                    version, standardlicense, publisher);

            final var jsonObject = new JSONObject();
            try {
                jsonObject.put("resourceID", resource.getId().toString());
                final var clientResponse = client.registerResource(resource);
                jsonObject.put("connectorResponse", clientResponse);
                response = ResponseEntity.ok(jsonObject.toJSONString());
            } catch (IOException e) {
                jsonObject.put("message", "Could not register resource at connector");

                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }

                response = ResponseEntity.badRequest().body(jsonObject.toJSONString());
            }
        }

        return response;
    }

    /**
     * This method updates a resource with the given parameters. The special feature here is that
     * the resource
     * is updated once in the app route and once in the resource catalog of the connector.
     *
     * @param resourceId      id of the resource
     * @param title           title of the resource
     * @param description     description of the resource
     * @param language        language of the resource
     * @param keywords        keywords for the resource
     * @param version         version of the resource
     * @param standardlicense standard license for the resource
     * @param publisher       the publisher of the resource
     * @return response from the target connector
     */
    @Override
    public ResponseEntity<String> updateResource(final URI resourceId,
                                                 final String title,
                                                 final String description,
                                                 final String language,
                                                 final ArrayList<String> keywords,
                                                 final String version,
                                                 final URI standardlicense,
                                                 final URI publisher) {
        if (log.isInfoEnabled()) {
            log.info(">> PUT /resource title: " + title + " description: " + description + " language: "
                    + language + " keywords: " + keywords + " version: " + version
                    + " standardlicense: " + standardlicense + " publisher: " + publisher);
        }
        ResponseEntity<String> response;

        if (ValidateApiInput.notValid(resourceId.toString(), title, description, language, version, standardlicense.toString(), publisher.toString())) {
            response = ResponseEntity.badRequest().body("All validated parameter have undefined as value!");
        } else {
            try {
                final var updatedResource = resourceService.updateResource(resourceId, title,
                        description, language, keywords, version, standardlicense, publisher);

                if (updatedResource != null) {
                    final var clientResponse = client.updateResource(resourceId, updatedResource);
                    if (clientResponse.isSuccessful()) {
                        final var registered = brokerService.getRegisStatusForResource(resourceId);
                        registered.iterator().forEachRemaining(elem -> {
                            final var asJsonObject = (JSONObject) elem;
                            final var brokerId = asJsonObject.getAsString("brokerId");
                            CompletableFuture.runAsync(() -> {
                                try {
                                    brokerClient.updateAtBroker(brokerId);
                                } catch (IOException e) {
                                    if (log.isWarnEnabled()) {
                                        log.warn(String.format("Error while updating at broker: %s", e.getMessage()), e);
                                    }
                                }
                            });
                        });
                        resourceService.updateResourceInAppRoute(updatedResource);
                    }
                    final var jsonObject = new JSONObject();
                    final var responseBody = clientResponse.body();
                    jsonObject.put("connectorResponse", responseBody != null ? responseBody.string() : "");
                    jsonObject.put("resourceID", resourceId.toString());

                    response = ResponseEntity.ok(jsonObject.toJSONString());
                } else {
                    response = ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(String.format("No resource with ID %s was found!", resourceId));
                }
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
            }
        }

        return response;
    }
}
