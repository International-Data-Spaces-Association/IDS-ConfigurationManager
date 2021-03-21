package de.fraunhofer.isst.configmanager.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customapp.CustomApp;
import de.fraunhofer.isst.configmanager.configmanagement.service.AppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ui")
@Tag(name = "App Management", description = "Endpoints for managing the app in the configuration " +
        "manager")
@Slf4j
public class AppUIController implements AppUIApi {

    private transient final AppService appService;
    private transient final ObjectMapper objectMapper;

    @Autowired
    public AppUIController(final AppService appService, final ObjectMapper objectMapper) {
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

        final var customAppList = appService.getApps();

        if (!customAppList.isEmpty()) {
            try {
                return ResponseEntity.ok(objectMapper.writeValueAsString(customAppList));
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems " +
                        "while parsing to json");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not find any app");
        }
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

        final var customApp = appService.getApp(id);

        if (customApp != null) {
            try {
                return ResponseEntity.ok(objectMapper.writeValueAsString(customApp));
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems " +
                        "while parsing to json");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not get app with id: " + id);
        }

    }
}
