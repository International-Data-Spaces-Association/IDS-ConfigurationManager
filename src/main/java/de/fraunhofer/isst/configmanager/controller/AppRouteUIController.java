package de.fraunhofer.isst.configmanager.controller;

import de.fraunhofer.iais.eis.AppRoute;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.configmanagement.service.AppRouteService;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.util.Utility;
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
@Tag(name = "App Route Management", description = "Endpoints for managing the app routes in the configuration manager")
public class AppRouteUIController implements AppRouteApi {

    private final ConfigModelService configModelService;
    private final AppRouteService appRouteService;
    private final Serializer serializer;

    @Autowired
    public AppRouteUIController(ConfigModelService configModelService, AppRouteService appRouteService,
                                Serializer serializer) {
        this.configModelService = configModelService;
        this.appRouteService = appRouteService;
        this.serializer = serializer;
    }

    @Override
    public ResponseEntity<String> createAppRoute(String routeDeployMethod) {

        AppRoute appRoute = appRouteService.createAppRoute(routeDeployMethod);

        if (appRoute != null) {
            var jsonObject = new JSONObject();
            jsonObject.put("id", appRoute.getId().toString());
            jsonObject.put("message", "Created a new app route successfully");
            return ResponseEntity.ok(jsonObject.toJSONString());
        } else {
            return ResponseEntity.badRequest().body("Could not create an app route");
        }
    }

    @Override
    public ResponseEntity<String> updateAppRoute(URI routeId, String routeDeployMethod) {
        boolean updated = appRouteService.updateAppRoute(routeId, routeDeployMethod);

        if (updated) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "App route with id: " + routeId + " is updated."));
        } else {
            return ResponseEntity.badRequest().body("Could not update app route with id: " + routeId);
        }
    }

    @Override
    public ResponseEntity<String> getAppRoute(URI routeId) {
        AppRoute appRoute = appRouteService.getAppRoute(routeId);

        if (appRoute != null) {
            try {
                return ResponseEntity.ok(serializer.serialize(appRoute));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not serialize app route to jsonld");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not get app route with id: " + routeId);
        }

    }

    @Override
    public ResponseEntity<String> getAppRoutes() {
        List<AppRoute> appRouteList = appRouteService.getAppRoutes();
        if (appRouteList != null) {
            try {
                return ResponseEntity.ok(serializer.serialize(appRouteList));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not serialize list of app routes");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not get list of app routes");
        }
    }

    @Override
    public ResponseEntity<String> deleteAppRoute(URI routeId) {
        var route = configModelService.getConfigModel().getAppRoute()
                .stream()
                .filter(appRoute -> appRoute.getId().equals(routeId))
                .findAny().orElse(null);

        if (route != null) {
            var routeEmpty = route.getAppRouteOutput().isEmpty()
                    && route.getAppRouteBroker().isEmpty()
                    && route.getAppRouteStart().isEmpty()
                    && route.getAppRouteEnd().isEmpty();
            if (routeEmpty) {
                configModelService.getConfigModel().getAppRoute().remove(route);
                var success = configModelService.saveState();

                if (success) {
                    var jsonObject = new JSONObject();
                    jsonObject.put("message", "Deleted app route with the id: " + routeId.toString());
                    return ResponseEntity.ok(jsonObject.toJSONString());
                } else {
                    return ResponseEntity.status(500).body("New Config not accepted by Connector!");
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not delete Route, not empty!");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Could not find AppRoute with given ID!");
        }
    }
}
