package de.fraunhofer.isst.configmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.iais.eis.GenericEndpoint;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.configmanagement.service.BackendConnectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ui")
@Tag(name = "Backend Connection Management", description = "Endpoints for managing the backend connections" +
        " in the configuration manager")
public class BackendConnectionUIController implements BackendConnectionApi {

    private final ObjectMapper objectMapper;
    private final BackendConnectionService backendConnectionService;
    private final Serializer serializer;

    @Autowired
    public BackendConnectionUIController(ObjectMapper objectMapper, BackendConnectionService backendConnectionService,
                                         Serializer serializer) {
        this.objectMapper = objectMapper;
        this.backendConnectionService = backendConnectionService;
        this.serializer = serializer;
    }

    /**
     * This method creates a backend connection with the given parameters.
     *
     * @param accessURL
     * @param username
     * @param password
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> createBackendConnection(String accessURL, String username, String password) {

        GenericEndpoint genericEndpoint =
                backendConnectionService.createBackendConnection(accessURL, username, password);
        if (genericEndpoint != null) {
            var jsonObject = new JSONObject();
            jsonObject.put("id", genericEndpoint.getId().toString());
            jsonObject.put("message", "Created a new backend connection");
            return ResponseEntity.ok(jsonObject.toJSONString());
        } else {
            return ResponseEntity.badRequest().body("Could not create a backend connection");
        }

    }

    /**
     * This method returns a list of backend connections.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getBackendConnections() {

        List<Endpoint> endpoints = backendConnectionService.getBackendConnections();
        try {
            return ResponseEntity.ok(serializer.serialize(endpoints));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems while serializing");
        }
    }

    /**
     * This method returns a backend connection.
     *
     * @param id id of the backend connection
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getBackendConnection(URI id) {

        GenericEndpoint genericEndpoint = backendConnectionService.getBackendConnection(id);
        if (genericEndpoint != null) {
            try {
                return ResponseEntity.ok(serializer.serialize(genericEndpoint));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems while serializing");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not get backend connection");
        }
    }

    /**
     * This method deletes a backend connection.
     *
     * @param id id of the backend connection
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> deleteBackendConnection(URI id) {
        boolean deleted = backendConnectionService.deleteBackendConnection(id);
        if (deleted) {
            return ResponseEntity.ok("Deleted the backend connection with id: " + id);
        } else {
            return ResponseEntity.badRequest().body("Could not delete the backend connection with id: " + id);
        }
    }

    /**
     * This method updates a backend connection with the given parameters.
     *
     * @param id        id of the backend connection
     * @param accessURL access url of the endpoint
     * @param username  username for authentication
     * @param password  password fot authentication
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateBackendConnection(URI id, String accessURL, String username, String
            password) {

        boolean updated = backendConnectionService.updateBackendConnection(id, accessURL, username, password);
        if (updated) {
            return ResponseEntity.ok("Updated the backend connnection with id: " + id);
        } else {
            return ResponseEntity.badRequest().body("Could not update the backend connection with id: " + id);
        }
    }
}
