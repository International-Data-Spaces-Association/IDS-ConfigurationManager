package de.fraunhofer.isst.configmanager.api.controller;

import de.fraunhofer.isst.configmanager.api.AppStoreApi;
import de.fraunhofer.isst.configmanager.appstore.AppStoreClient;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/api/ui")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "App Store Management", description = "Endpoints for managing the data app images which are received from the connector")
public class AppStoreController implements AppStoreApi {

    transient AppStoreClient appStoreClient;

    @Autowired
    public AppStoreController(final AppStoreClient appStoreClient) {
        this.appStoreClient = appStoreClient;
    }

    @Override
    public ResponseEntity<String> getImages() {

        ResponseEntity<String> response;
        final var images = appStoreClient.getImages();

        if (images.isEmpty()) {
            response = ResponseEntity.ok(images.toString());
        } else {
            final var jsonArray = new JSONArray();
            for (var image : images) {
                final var jsonObject = new JSONObject();
                jsonObject.put("imageID", image.getId().substring(9).substring(0, 11));
                jsonObject.put("tag", Arrays.toString((image.getRepoTags())));
                jsonObject.put("size", Math.round(image.getSize() / 1000000d) + "MB");
                jsonArray.add(jsonObject);
            }
            response = ResponseEntity.ok(jsonArray.toJSONString());
        }
        return response;
    }

    @Override
    public ResponseEntity<String> pullImage(final String imageName) {
        final var result = appStoreClient.pullImage(imageName);
        ResponseEntity<String> response;
        if (result) {
            response = ResponseEntity.ok("Pulled successfully docker image from registry");
        } else {
            response = ResponseEntity.badRequest().body("Failed to pull docker image from registry");
        }
        return response;
    }

    @Override
    public ResponseEntity<String> pushImage(final String imageName) {
        appStoreClient.pushImage(imageName);
        return ResponseEntity.ok("Pushed image successfully to registry");
    }

    @Override
    public ResponseEntity<String> removeImage(final String imageID) {
        appStoreClient.removeImage(imageID);
        return ResponseEntity.ok("Removed image successfully");
    }

    @Override
    public ResponseEntity<String> getContainers() {

        ResponseEntity<String> response;
        final var containers = appStoreClient.getContainers();
        if (containers.isEmpty()) {
            response = ResponseEntity.ok(containers.toString());
        } else {
            final var jsonArray = new JSONArray();
            for (var container : containers) {
                final var jsonObject = new JSONObject();
                jsonObject.put("containerID", container.getId().substring(0,11));
                jsonObject.put("containerImage", container.getImage());
                jsonObject.put("containerStatus", container.getStatus());
                jsonObject.put("containerPorts", Arrays.toString(container.getPorts()));
                jsonArray.add(jsonObject);
            }
            response = ResponseEntity.ok(jsonArray.toJSONString());
        }
        return response;
    }

    @Override
    public ResponseEntity<String> buildContainer(final String imageName) {
        final var containerID = appStoreClient.buildContainer(imageName).substring(0, 11);
        return ResponseEntity.ok(String.format("Created container with id: %s successfully", containerID));
    }

    @Override
    public ResponseEntity<String> startContainer(final String containerID) {
        appStoreClient.startContainer(containerID);
        return ResponseEntity.ok("Started container successfully");
    }

    @Override
    public ResponseEntity<String> stopContainer(final String containerID) {
        appStoreClient.stopContainer(containerID);
        return ResponseEntity.ok("Stopped container successfully");
    }

    @Override
    public ResponseEntity<String> removeContainer(final String containerID) {
        appStoreClient.removeContainer(containerID);
        return ResponseEntity.ok("Removed container successfully");
    }

}
