package de.fraunhofer.isst.configmanager.controller;

import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.configmanagement.service.AppRouteService;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.util.Utility;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/ui")
@Tag(name = "App Route: Start Management", description = "Endpoints for managing route starts in the configuration manager")
public class AppRouteStartUIController implements AppRouteStartApi {

    private final ConfigModelService configModelService;
    private final AppRouteService appRouteService;
    private final Serializer serializer;

    @Autowired
    public AppRouteStartUIController(ConfigModelService configModelService, AppRouteService appRouteService,
                                     Serializer serializer) {

        this.configModelService = configModelService;
        this.appRouteService = appRouteService;
        this.serializer = serializer;
    }

    @Override
    public ResponseEntity<String> createAppRouteStart(URI routeId, String accessUrl, String username, String password) {
        Endpoint endpoint = appRouteService.createAppRouteStart(routeId, accessUrl, username, password);

        if (endpoint != null) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Created a new endpoint with id: " + endpoint.getId()));
        } else {
            return ResponseEntity.badRequest().body("Could not create an endpoint");
        }
    }

    @Override
    public ResponseEntity<String> createSubrouteStart(URI routeId, URI routeStepId, String accessUrl, String username, String password) {

        Endpoint endpoint = appRouteService.createSubrouteStart(routeId, routeStepId, accessUrl, username, password);

        if (endpoint != null) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Created a new endpoint with id: " + endpoint.getId()));
        } else {
            return ResponseEntity.badRequest().body("Could not create an endpoint");
        }
    }

    @Override
    public ResponseEntity<String> getAppRouteStart(URI routeId, URI endpointId) {
        Endpoint endpoint = appRouteService.getAppRouteStart(routeId, endpointId);

        if (endpoint != null) {
            try {
                return ResponseEntity.ok(serializer.serialize(endpoint));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not serialize the endpoint");
            }
        }
        return ResponseEntity.badRequest().body("Could not get endpoint");
    }

    @Override
    public ResponseEntity<String> getSubrouteStart(URI routeId, URI routeStepId, URI endpointId) {
        Endpoint endpoint = appRouteService.getSubrouteStart(routeId, routeStepId, endpointId);

        if (endpoint != null) {
            try {
                return ResponseEntity.ok(serializer.serialize(endpoint));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not serialize the endpoint");
            }
        }
        return ResponseEntity.badRequest().body("Could not get endpoint");

    }

    @Override
    public ResponseEntity<String> deleteAppRouteStart(URI routeId, URI endpointId) {
        boolean deleted = appRouteService.deleteAppRouteStart(routeId, endpointId);

        if (deleted) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Deleted the endpdoint with id: " + endpointId));
        } else {
            return ResponseEntity.badRequest().body("Could not delete the endpoint with id: " + endpointId);
        }
    }

    @Override
    public ResponseEntity<String> deleteSubrouteStart(URI routeId, URI routeStepId, URI endpointId) {

        boolean deleted = appRouteService.deleteSubrouteStart(routeId, routeStepId, endpointId);

        if (deleted) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Deleted the endpdoint with id: " + endpointId));
        } else {
            return ResponseEntity.badRequest().body("Could not delete the endpoint with id: " + endpointId);
        }
    }

    @Override
    public ResponseEntity<String> updateAppRouteStart(URI routeId, URI endpointId,
                                                      String accessUrl, String username, String password) {

        boolean updated = appRouteService.updateAppRouteStart(routeId, endpointId, accessUrl, username, password);

        if (updated) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Updated the endpoint in the app route with id: " +
                    endpointId));
        } else {
            return ResponseEntity.badRequest().body("Could not update the endpoint with id: " + endpointId);
        }
    }

    @Override
    public ResponseEntity<String> updateSubrouteStart(URI routeId, URI routeStepId, URI endpointId,
                                                      String accessUrl, String username, String password) {

        boolean updated = appRouteService.updateSubrouteStart(routeId, routeStepId,
                endpointId, accessUrl, username, password);

        if (updated) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Updated the endpoint in the subroute with id: " +
                    endpointId));
        } else {
            return ResponseEntity.badRequest().body("Could not update the endpoint with id: " + endpointId);
        }
    }
}
