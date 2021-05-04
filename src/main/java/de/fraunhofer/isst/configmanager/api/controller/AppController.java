package de.fraunhofer.isst.configmanager.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.isst.configmanager.api.AppApi;
import de.fraunhofer.isst.configmanager.api.ExampleDemoApi;
import de.fraunhofer.isst.configmanager.api.service.AppService;
import de.fraunhofer.isst.configmanager.appstore.AppStoreClient;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/ui")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "App Management", description = "Endpoints for managing the app in the configuration manager")
public class AppController implements AppApi, ExampleDemoApi {

    transient AppService appService;
    transient ObjectMapper objectMapper;
    transient AppStoreClient appStoreClient;

    @Autowired
    public AppController(final AppService appService, final ObjectMapper objectMapper, final AppStoreClient appStoreClient) {
        this.appService = appService;
        this.objectMapper = objectMapper;
        this.appStoreClient = appStoreClient;
    }

    /**
     * This method returns a list of custom apps.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getApps() {
        if (log.isInfoEnabled()) {
            log.info(">> GET /apps");
        }
        ResponseEntity<String> response;

        final var customAppList = appService.getApps();

        if (!customAppList.isEmpty()) {
            try {
                response = ResponseEntity.ok(objectMapper.writeValueAsString(customAppList));
            } catch (JsonProcessingException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            response = ResponseEntity.badRequest().body("Could not find custom apps");
        }

        return response;
    }

    @Override
    public ResponseEntity<String> getImages() {
        return ResponseEntity.ok(appStoreClient.getImages().toString());
    }

    @Override
    public ResponseEntity<String> getImage(String imageName) {
        var result = appStoreClient.pullImage(imageName);
        ResponseEntity<String> response;
        if (result) {
            response = ResponseEntity.ok("Pulled image successfully");
        } else {
            response = ResponseEntity.badRequest().body("Could not pull image");
        }
        return response;
    }

    @Override
    public ResponseEntity<String> getContainers() {
        return ResponseEntity.ok(appStoreClient.getContainers().toString());
    }

    @Override
    public ResponseEntity<String> buildContainer(String imageName) {
        String containerID = appStoreClient.buildContainer(imageName);
        String substring = containerID.substring(0, 11);
        return ResponseEntity.ok("Created container with id: " + substring);
    }

    @Override
    public ResponseEntity<String> startContainer(String containerID) {
        appStoreClient.startContainer(containerID);
        return ResponseEntity.ok("Started Container");
    }

    @Override
    public ResponseEntity<String> stopContainer(String containerID) {
        appStoreClient.stopContainer(containerID);
        return ResponseEntity.ok("Stopped Container");
    }
}
