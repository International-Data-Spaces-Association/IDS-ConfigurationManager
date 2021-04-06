package de.fraunhofer.isst.configmanager.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.isst.configmanager.api.AppApi;
import de.fraunhofer.isst.configmanager.api.service.AppService;
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
@Tag(name = "App Management", description = "Endpoints for managing the app in the configuration manager")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AppController implements AppApi {

    transient AppService appService;
    transient ObjectMapper objectMapper;

    @Autowired
    public AppController(final AppService appService, final ObjectMapper objectMapper) {
        this.appService = appService;
        this.objectMapper = objectMapper;
    }

    /**
     * This method returns a list of custom apps.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getApps() {
        log.info(">> GET /apps");
        ResponseEntity<String> response;

        final var customAppList = appService.getApps();

        if (!customAppList.isEmpty()) {
            try {
                response = ResponseEntity.ok(objectMapper.writeValueAsString(customAppList));
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Problems while parsing to json");
            }
        } else {
            response = ResponseEntity.badRequest().body("Could not find any app");
        }

        return response;
    }

    /**
     * This method returns a specific app.
     *
     * @param id id of the app
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getApp(final String id) {
        log.info(">> GET /app id: " + id);
        ResponseEntity<String> response;

        final var customApp = appService.getApp(id);

        if (customApp != null) {
            try {
                response = ResponseEntity.ok(objectMapper.writeValueAsString(customApp));
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Problems while parsing to json");
            }
        } else {
            response = ResponseEntity.badRequest().body("Could not get app with id: " + id);
        }

        return response;
    }
}
