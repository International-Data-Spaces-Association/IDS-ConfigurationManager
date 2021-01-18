package de.fraunhofer.isst.configmanager.controller;

import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.configmanagement.service.AppRouteService;
import de.fraunhofer.isst.configmanager.util.Utility;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;

/**
 * The controller class implements the AppRouteEndApi and offers the possibilities to manage
 * the route ends in the configurationmanager.
 */
@RestController
@RequestMapping("/api/ui")
@Tag(name = "App Route: End Management", description = "Endpoints for managing route ends in the configuration manager")
public class AppRouteEndUIController implements AppRouteEndApi {

    private final AppRouteService appRouteService;
    private final Serializer serializer;

    @Autowired
    public AppRouteEndUIController(AppRouteService appRouteService, Serializer serializer) {
        this.appRouteService = appRouteService;
        this.serializer = serializer;
    }

    /**
     * This method creates an endpoint end for an app route.
     *
     * @param routeId   id of the app route
     * @param accessUrl access url from the endpoint
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> createAppRouteEnd(URI routeId, String accessUrl) {

        Endpoint endpoint = appRouteService.createAppRouteEnd(routeId, accessUrl);

        if (endpoint != null) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Created an endpoint for the app route with id: " +
                    endpoint.getId()));
        } else {
            return ResponseEntity.badRequest().body("Could not create an endpoint for the app route");
        }
    }

    /**
     * This method creates an endpoint end for the subroute.
     *
     * @param routeId     id id of the app route
     * @param routeStepId id of the subroute
     * @param accessUrl   access url from the endpoint
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> createSubrouteEnd(URI routeId, URI routeStepId, String accessUrl) {
        Endpoint endpoint = appRouteService.createSubrouteEnd(routeId, routeStepId, accessUrl);

        if (endpoint != null) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Created an endpoint for the subroute with id: " +
                    endpoint.getId()));
        } else {
            return ResponseEntity.badRequest().body("Could not create an endpoint for the subroute");
        }

    }

    /**
     * This method returns the endpoint end from an app route.
     *
     * @param routeId    id of the app route
     * @param endpointId id of the endpoint
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getAppRouteEnd(URI routeId, URI endpointId) {

        Endpoint endpoint = appRouteService.getAppRouteEnd(routeId, endpointId);

        if (endpoint != null) {
            try {
                return ResponseEntity.ok(serializer.serialize(endpoint));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not serialize the endpoint");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not get the endpoint with id: " + endpointId);
        }

    }

    /**
     * This method returns the endpoint end from a subroute.
     *
     * @param routeId     id of the app route
     * @param routeStepId id id of the subroute
     * @param endpointId  id id of the endpoint
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getSubrouteEnd(URI routeId, URI routeStepId, URI endpointId) {

        Endpoint endpoint = appRouteService.getSubrouteEnd(routeId, routeStepId, endpointId);
        if (endpoint != null) {
            try {
                return ResponseEntity.ok(serializer.serialize(endpoint));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not serialize the endpoint");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not get the endpoint with id: " + endpointId);
        }
    }

    /**
     * This method deletes an endpoint end from an app route
     *
     * @param routeId    id id of the app route
     * @param endpointId id id of the endpoint
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> deleteAppRouteEnd(URI routeId, URI endpointId) {

        boolean deleted = appRouteService.deleteAppRouteEnd(routeId, endpointId);
        if (deleted) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Deleted the endpoint with id: " + endpointId));
        } else {
            return ResponseEntity.badRequest().body("Could not delete the endpoint with id: " + endpointId);
        }

    }

    /**
     * This method deltes an endpoint end from the subroute
     *
     * @param routeId     id of the app route
     * @param routeStepId id of the subroute
     * @param endpointId  id of the endpoint
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> deleteSubrouteEnd(URI routeId, URI routeStepId, URI endpointId) {

        boolean deleted = appRouteService.deleteSubrouteEnd(routeId, routeStepId, endpointId);
        if (deleted) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Deleted the endpoint with id: " + endpointId));
        } else {
            return ResponseEntity.badRequest().body("Could not delete the endpoint with id: " + endpointId);
        }
    }

    /**
     * This method updates an endpoint end from an app route.
     *
     * @param routeId    id of the app route
     * @param endpointId id of the endpoint
     * @param accessUrl  access url from the endpoint
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateAppRouteEnd(URI routeId, URI endpointId, String accessUrl) {

        boolean updated = appRouteService.updateAppRouteEnd(routeId, endpointId, accessUrl);
        if (updated) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Updated the endpoint with id: " + endpointId));
        } else {
            return ResponseEntity.badRequest().body("Could not update the endpoint with id: " + endpointId);
        }

    }

    /**
     * This method updates an endpoint end from a subroute.
     *
     * @param routeId     id of the app route
     * @param routeStepId id of the subroute
     * @param endpointId  id of the endpoint
     * @param accessUrl   access url from the endpoint
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateSubrouteEnd(URI routeId, URI routeStepId, URI endpointId, String accessUrl) {


        boolean updated = appRouteService.updateSubrouteEnd(routeId, routeStepId, endpointId, accessUrl);
        if (updated) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Updated the endpoint with id: " + endpointId));
        } else {
            return ResponseEntity.badRequest().body("Could not update the endpoint with id: " + endpointId);
        }
    }
}
