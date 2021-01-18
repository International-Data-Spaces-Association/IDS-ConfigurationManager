package de.fraunhofer.isst.configmanager.controller;

import de.fraunhofer.iais.eis.RouteStep;
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

@RestController
@RequestMapping("/api/ui")
@Tag(name = "App Route: Subroute Management", description = "Endpoints for managing subroutes in the configuration manager")
public class AppRouteSubrouteUIController implements AppRouteSubrouteApi {

    private final AppRouteService appRouteService;
    private final Serializer serializer;

    @Autowired
    public AppRouteSubrouteUIController(AppRouteService appRouteService, Serializer serializer) {
        this.appRouteService = appRouteService;
        this.serializer = serializer;
    }

    @Override
    public ResponseEntity<String> createSubroute(URI routeId, String routeDeployMethod) {
        RouteStep routeStep = appRouteService.createSubroute(routeId, routeDeployMethod);

        if (routeStep != null) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Created new subroute with id: " + routeStep.getId()));
        } else {
            return ResponseEntity.badRequest().body("Could not create subroute for the app route with id: " + routeId);
        }
    }

    @Override
    public ResponseEntity<String> updateSubroute(URI routeId, URI routeStepId, String routeDeployMethod) {
        boolean updated = appRouteService.updateSubroute(routeId, routeStepId, routeDeployMethod);

        if (updated) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Updated the given subroute with id: " + routeStepId));
        } else {
            return ResponseEntity.badRequest().body("Could not update the given subroute with id: " + routeStepId);
        }
    }

    @Override
    public ResponseEntity<String> getSubroute(URI routeId, URI routeStepId) {
        RouteStep routeStep = appRouteService.getSubroute(routeId, routeStepId);

        if (routeStep != null) {
            try {
                return ResponseEntity.ok(serializer.serialize(routeStep));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not serialize the subroute");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not get the subroute with id: " + routeId);
        }
    }

    @Override
    public ResponseEntity<String> deleteSubroute(URI routeId, URI routeStepId) {
        boolean deleted = appRouteService.deleteSubroute(routeId, routeStepId);

        if (deleted) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Deleted the subroute with id: " + routeStepId));
        } else {
            return ResponseEntity.badRequest().body("Could not delete the subroute with id: " + routeStepId);
        }
    }


}
