package de.fraunhofer.isst.configmanager.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customApp.CustomApp;
import de.fraunhofer.isst.configmanager.configmanagement.service.AppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ui")
@Tag(name = "App Management", description = "Endpoints for managing the app in the configuration manager")
public class AppUIController implements AppUIApi {

    private final AppService appService;
    private final ObjectMapper objectMapper;

    @Autowired
    public AppUIController(AppService appService, ObjectMapper objectMapper) {
        this.appService = appService;
        this.objectMapper = objectMapper;
    }

//    @Override
//    public ResponseEntity<String> createApp(String title) {
//
//        CustomApp customApp = appService.createApp(title);
//
//        if (customApp != null) {
//            return ResponseEntity.ok(Utility.jsonMessage("message", "Created a new app with id: " + customApp.getId()));
//        } else {
//            return ResponseEntity.badRequest().body("Could not create app");
//        }
//    }
//
//    @Override
//    public ResponseEntity<String> updateApp(String id, String title) {
//        boolean updated = appService.updateApp(id, title);
//
//        if (updated) {
//            return ResponseEntity.ok(Utility.jsonMessage("message", "Updated the app with id: " + id));
//        } else {
//            return ResponseEntity.badRequest().body("Could not update the app with the id: " + id);
//        }
//    }

    @Override
    public ResponseEntity<String> getApps() {
        List<CustomApp> customAppList = appService.getApps();

        if (customAppList.size() != 0) {
            try {
                return ResponseEntity.ok(objectMapper.writeValueAsString(customAppList));
            } catch (JsonProcessingException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems while parsing to json");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not find any app");
        }
    }

    @Override
    public ResponseEntity<String> getApp(String id) {
        CustomApp customApp = appService.getApp(id);

        if (customApp != null) {
            try {
                return ResponseEntity.ok(objectMapper.writeValueAsString(customApp));
            } catch (JsonProcessingException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems while parsing to json");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not get app with id: " + id);
        }

    }
//
//    @Override
//    public ResponseEntity<String> deleteApp(String id) {
//        boolean deleted = appService.deleteApp(id);
//
//        if (deleted) {
//            return ResponseEntity.ok(Utility.jsonMessage("message", "Deleted the app with id: " + id));
//        } else {
//            return ResponseEntity.badRequest().body("Could not delete the app with id: " + id);
//        }
//    }
}
