package de.fraunhofer.isst.configmanager.controller;

import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceImpl;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
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
import java.net.URI;
import java.util.ArrayList;

/**
 * The controller class implements the ResourceUIApi and offers the possibilities to manage
 * the resources in the configuration manager.
 */
@RestController
@RequestMapping("/api/ui")
@Slf4j
@Tag(name = "Resource Management", description = "Endpoints for managing the resource in the configuration manager")
public class ResourceUIController implements ResourceUIApi {
    private final ResourceService resourceService;
    private final DefaultConnectorClient client;
    private final Serializer serializer;

    @Autowired
    public ResourceUIController(ResourceService resourceService,
                                DefaultConnectorClient client, Serializer serializer) {
        this.resourceService = resourceService;
        this.client = client;
        this.serializer = serializer;
    }

    /**
     * This method returns a resource from the connector with the given paraemter.
     *
     * @param resourceId id of the resource
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getResource(URI resourceId) {
        log.info(">> GET /resource resourceId: " + resourceId);

        Resource resource = resourceService.getResource(resourceId);

        if (resource != null) {
            try {
                return ResponseEntity.ok(serializer.serialize(resource));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not serialize resource!");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not determine the resource");
        }
    }

    /**
     * This method returns all resources from the connector.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getResources() {
        log.info(">> GET /resources");
        return ResponseEntity.ok(resourceService.getOfferedResourcesAsJsonString());
    }

    @Override
    public ResponseEntity<String> getRequestedResources() {
        log.info(">> GET /resources/requested");
        return ResponseEntity.ok(resourceService.getRequestedResourcesAsJsonString());
    }

    /**
     * This method returns a specific resource in JSON format.
     *
     * @param resourceId if of the resource
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getResourceInJson(URI resourceId) {
        log.info(">> GET /resource/json resourceId: " + resourceId);

        Resource resource = resourceService.getResource(resourceId);

        JSONObject resourceJson = new JSONObject();
        resourceJson.put("title", resource.getTitle().get(0).getValue());
        resourceJson.put("description", resource.getDescription().get(0).getValue());
        resourceJson.put("keyword", resource.getKeyword());
        resourceJson.put("version", resource.getVersion());
        resourceJson.put("standardlicense", resource.getStandardLicense().toString());
        resourceJson.put("publisher", resource.getPublisher().toString());

        return ResponseEntity.ok(resourceJson.toJSONString());
    }

    /**
     * This method deletes the resource from the connector and the app route with the given parameter.
     * If both are deleted the dataspace connector is informed about the change.
     *
     * @param resourceId id of the resource
     * @return http response from the target connector
     */
    @Override
    public ResponseEntity<String> deleteResource(URI resourceId) {
        log.info(">> DELETE /resource resourceId: " + resourceId);
        try {
            var response = client.deleteResource(resourceId);
            resourceService.deleteResourceFromAppRoute(resourceId);
            var jsonObject = new JSONObject();
            jsonObject.put("connectorResponse", response);
            jsonObject.put("resourceID", resourceId.toString());
            return ResponseEntity.ok(jsonObject.toJSONString());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body("Could not send delete request to connector");
        }
    }

    /**
     * This method creates a resource with the given parameters. The special feature here is that the created resource
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
    public ResponseEntity<String> createResource(String title, String description, String language,
                                                 ArrayList<String> keywords, String version, String standardlicense,
                                                 String publisher) {
        log.info(">> POST /resource title: " + title + " description: " + description + " language: " + language + " keywords: " + keywords + " version: " + version + " standardlicense: " + standardlicense
        + " publisher: " + publisher);

        ResourceImpl resource = resourceService.createResource(title, description, language, keywords,
                version, standardlicense, publisher);

        // Save and send request to dataspace connector
        var jsonObject = new JSONObject();
        try {
            jsonObject.put("resourceID", resource.getId().toString());
            var response = client.registerResource(resource);
            jsonObject.put("connectorResponse", response);
            return ResponseEntity.ok(jsonObject.toJSONString());
        } catch (IOException e) {
            jsonObject.put("message", "Could not register resource at connector");
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body(jsonObject.toJSONString());
        }
    }

    /**
     * This method updates a resource with the given parameters. The special feature here is that the resource
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
    public ResponseEntity<String> updateResource(URI resourceId, String title, String description, String language,
                                                 ArrayList<String> keywords, String version, String standardlicense,
                                                 String publisher) {
        log.info(">> PUT /resource title: " + title + " description: " + description + " language: " + language + " keywords: " + keywords + " version: " + version + " standardlicense: " + standardlicense
                + " publisher: " + publisher);



        // Save the updated resource and update the resource in the dataspace connector
        try {
            ResourceImpl updatedResource = resourceService.updateResource(resourceId, title, description, language, keywords,
                    version, standardlicense, publisher);
            if(updatedResource != null){
                var response = client.updateResource(resourceId, updatedResource);
                resourceService.updateResourceInAppRoute(updatedResource);
                var jsonObject = new JSONObject();
                jsonObject.put("connectorResponse", response);
                jsonObject.put("resourceID", resourceId.toString());
                return ResponseEntity.ok(jsonObject.toJSONString());
            }else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("No resource with ID %s was found!", resourceId));
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
