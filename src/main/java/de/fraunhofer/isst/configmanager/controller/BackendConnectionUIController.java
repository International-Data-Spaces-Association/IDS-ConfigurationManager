package de.fraunhofer.isst.configmanager.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.isst.configmanager.configmanagement.entities.backendConnection.BackendConnection;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.BackendConnectionRepository;
import de.fraunhofer.isst.configmanager.util.Utility;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ui")
@Tag(name = "Backend Connection Management", description = "Endpoints for managing the backend connections" +
        " in the configuration manager")
public class BackendConnectionUIController implements BackendConnectionApi {

    private final ObjectMapper objectMapper;
    private final BackendConnectionRepository backendConnectionRepository;

    @Autowired
    public BackendConnectionUIController(ObjectMapper objectMapper, BackendConnectionRepository backendConnectionRepository) {
        this.objectMapper = objectMapper;
        this.backendConnectionRepository = backendConnectionRepository;
    }

    @Override
    public ResponseEntity<String> createBackendConnection(String accessURL, String username, String password) {

        BackendConnection backendConnection = new BackendConnection();
        backendConnection.setAccessURL(accessURL);
        if (username != null) {
            backendConnection.setUsername(username);
        }
        if (password != null) {
            backendConnection.setPassword(password);
        }
        backendConnectionRepository.save(backendConnection);
        return ResponseEntity.ok(Utility.jsonMessage("message", "Created a new backend connection"));
    }

    @Override
    public ResponseEntity<String> getBackendConnections() {
        List<BackendConnection> backendConnections = backendConnectionRepository.findAll();
        if (backendConnections.size() != 0) {
            try {
                return ResponseEntity.ok(objectMapper.writeValueAsString(backendConnections));
            } catch (JsonProcessingException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems while parsing to JSON");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not get any backend connection");
        }
    }

    @Override
    public ResponseEntity<String> getBackendConnection(String id) {
        Long backendConnectionId = Long.valueOf(id);
        try {
            return ResponseEntity.ok(objectMapper.writeValueAsString(backendConnectionRepository.
                    findById(backendConnectionId).orElse(null)));
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems while parsing to JSON");
        }
    }

    @Override
    public ResponseEntity<String> deleteBackendConnection(String id) {
        Long backendConnectionId = Long.valueOf(id);
        BackendConnection backendConnection = backendConnectionRepository.findById(backendConnectionId).orElse(null);

        if (backendConnection != null) {
            backendConnectionRepository.delete(backendConnection);
            return ResponseEntity.ok("Deleted the backend connection with the id: " + id);
        } else {
            return ResponseEntity.badRequest().body("Could not delete the backend connection with the id: " + id);
        }
    }

    @Override
    public ResponseEntity<String> updateBackendConnection(String id, String accessURL, String username, String password) {
        Long backendConnectionId = Long.valueOf(id);
        BackendConnection backendConnection = backendConnectionRepository.findById(backendConnectionId).orElse(null);

        if (backendConnection != null) {
            if (accessURL != null) {
                backendConnection.setAccessURL(accessURL);
            }
            if (username != null) {
                backendConnection.setUsername(username);
            }
            if (password != null) {
                backendConnection.setPassword(password);
            }
            backendConnectionRepository.save(backendConnection);
            return ResponseEntity.ok("Updated the backend connection with id: " + id);
        } else {
            return ResponseEntity.badRequest().body("Could not update the backend connection with id: " + id);
        }
    }
}
