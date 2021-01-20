package de.fraunhofer.isst.configmanager.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.AppRoute;
import de.fraunhofer.iais.eis.AppRouteImpl;
import de.fraunhofer.iais.eis.RouteStep;
import de.fraunhofer.iais.eis.RouteStepImpl;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.RouteDeployMethodRepository;
import de.fraunhofer.isst.configmanager.configmanagement.entities.routeDeployMethod.DeployMethod;
import de.fraunhofer.isst.configmanager.configmanagement.entities.routeDeployMethod.RouteDeployMethod;
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
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/ui")
@Tag(name = "App Route Management", description = "Endpoints for managing the app routes in the configuration manager")
public class AppRouteUIController implements AppRouteApi {

    private final ConfigModelService configModelService;
    private final AppRouteService appRouteService;
    private final Serializer serializer;
    private final RouteDeployMethodRepository routeDeployMethodRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public AppRouteUIController(ConfigModelService configModelService, AppRouteService appRouteService,
                                Serializer serializer, RouteDeployMethodRepository routeDeployMethodRepository,
                                ObjectMapper objectMapper) {
        this.configModelService = configModelService;
        this.appRouteService = appRouteService;
        this.serializer = serializer;
        this.routeDeployMethodRepository = routeDeployMethodRepository;
        this.objectMapper = objectMapper;
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

    @Override
    public ResponseEntity<String> createRouteDeployMethod(DeployMethod deployMethod) {

        RouteDeployMethod routeDeployMethod = new RouteDeployMethod();
        routeDeployMethod.setDeployMethod(deployMethod);

        if (routeDeployMethodRepository.count() == 0) {
            routeDeployMethodRepository.save(routeDeployMethod);
        } else {
            RouteDeployMethod existingDeployMethod = routeDeployMethodRepository.findAll().get(0);
            if (existingDeployMethod != null) {
                routeDeployMethodRepository.delete(existingDeployMethod);
                routeDeployMethodRepository.save(routeDeployMethod);
                // Updates the deploy method from the app routes and route steps
                updateDeployMethodFromRoutes(deployMethod);
            }
        }
        return ResponseEntity.ok("Successfully created the route deploy method");
    }

    @Override
    public ResponseEntity<String> updateRouteDeployMethod(DeployMethod deployMethod) {

        if (routeDeployMethodRepository.count() != 0) {
            RouteDeployMethod existingDeployMethod = routeDeployMethodRepository.findAll().get(0);
            existingDeployMethod.setDeployMethod(deployMethod);
            routeDeployMethodRepository.save(existingDeployMethod);
            // Updates the deploy method from the app routes and route steps
            updateDeployMethodFromRoutes(deployMethod);
            return ResponseEntity.ok("Updated successfully the route deploy method");
        } else {
            return ResponseEntity.badRequest().body("Could not update the route deploy method");
        }
    }

    @Override
    public ResponseEntity<String> getRouteDeployMethods() {
        List<RouteDeployMethod> routeDeployMethods = routeDeployMethodRepository.findAll();
        try {
            return ResponseEntity.ok(objectMapper.writeValueAsString(routeDeployMethods));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Could not get deploy method from the app routes");
        }
    }

    /**
     * This method updates the deploy method from every app route and the subroute
     *
     * @param deployMethod deploy method of the route
     */
    private void updateDeployMethodFromRoutes(DeployMethod deployMethod) {

        ArrayList<AppRoute> appRouteList = (ArrayList<AppRoute>) configModelService.getConfigModel().getAppRoute();
        if (appRouteList != null) {
            // Update deploy method from app routes
            for (AppRoute appRoute : appRouteList) {
                if (appRoute != null) {
                    var appRouteImpl = (AppRouteImpl) appRoute;
                    appRouteImpl.setRouteDeployMethod(deployMethod.toString());

                    // Update deploy method from route steps
                    if (appRoute.getHasSubRoute() != null) {
                        for (RouteStep routeStep : appRoute.getHasSubRoute()) {
                            if (routeStep != null) {
                                var routeStepImpl = (RouteStepImpl) routeStep;
                                routeStepImpl.setRouteDeployMethod(deployMethod.toString());
                            }
                        }
                    }
                }
            }
            configModelService.saveState();
        }
    }
}
