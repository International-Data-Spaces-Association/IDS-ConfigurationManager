package de.fraunhofer.isst.configmanager.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.AppRoute;
import de.fraunhofer.iais.eis.AppRouteImpl;
import de.fraunhofer.iais.eis.RouteStep;
import de.fraunhofer.iais.eis.RouteStepImpl;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.RouteDeployMethodRepository;
import de.fraunhofer.isst.configmanager.configmanagement.entities.endpointInfo.EndpointInformation;
import de.fraunhofer.isst.configmanager.configmanagement.entities.routeDeployMethod.DeployMethod;
import de.fraunhofer.isst.configmanager.configmanagement.entities.routeDeployMethod.RouteDeployMethod;
import de.fraunhofer.isst.configmanager.configmanagement.service.AppRouteService;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.util.Utility;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.minidev.json.JSONArray;
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

/**
 * The controller class implements the AppRouteApi and offers the possibilities to manage
 * the app routes in the configuration manager.
 */
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

        if (routeDeployMethodRepository.count() == 0) {
            RouteDeployMethod routeDeployMethod = new RouteDeployMethod();
            routeDeployMethod.setDeployMethod(DeployMethod.NONE);
            routeDeployMethodRepository.save(routeDeployMethod);
        }
    }

    /**
     * This method creates an app route.
     *
     * @param description description of the app route
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> createAppRoute(String description) {

        AppRoute appRoute = appRouteService.createAppRoute(description);

        if (appRoute != null) {
            var jsonObject = new JSONObject();
            jsonObject.put("id", appRoute.getId().toString());
            jsonObject.put("message", "Created a new app route successfully");
            return ResponseEntity.ok(jsonObject.toJSONString());
        } else {
            return ResponseEntity.badRequest().body("Could not create an app route");
        }
    }

    /**
     * This method updates an app route
     *
     * @param routeId     id of the app route
     * @param description description of the app route
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateAppRoute(URI routeId, String description) {
        boolean updated = appRouteService.updateAppRoute(routeId, description);

        if (updated) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "App route with id: " + routeId + " is updated."));
        } else {
            return ResponseEntity.badRequest().body("Could not update app route with id: " + routeId);
        }
    }

    /**
     * This method deletes an app route
     *
     * @param routeId id of the app route
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> deleteAppRoute(URI routeId) {
        boolean deleted = appRouteService.deleteAppRoute(routeId);
        if (deleted) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "App route with id: " + routeId + " is deleted."));
        } else {
            return ResponseEntity.badRequest().body("Could not delete app route with id: " + routeId);
        }
    }

    /**
     * This method returns a specific app route
     *
     * @param routeId id of the app route
     * @return a suitable http response depending on success
     */
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

    /**
     * This method returns a list of app routes
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getAppRoutes() {
        List<AppRoute> appRouteList = appRouteService.getAppRoutes();
        try {
            if (appRouteList == null) {
                return ResponseEntity.ok(objectMapper.writeValueAsString(new JSONArray()));
            } else {
                return ResponseEntity.ok(serializer.serialize(appRouteList));
            }
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Problems while serializing");
        }
    }

    /**
     * This method creates a route step for an app route with the given parameters
     *
     * @param routeId          id of the route
     * @param startId          id of the start endpoint
     * @param startCoordinateX x coordinate of the start endpoint
     * @param startCoordinateY y coordinate of the start endpoint
     * @param endID            id of the last endpoint
     * @param endCoordinateX   x coordinate of the last endpoint
     * @param endCoordinateY   y coordinate of the last endpoint
     * @param resourceId       id of the resource
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> createAppRouteStep(URI routeId, URI startId, int startCoordinateX,
                                                     int startCoordinateY, URI endID, int endCoordinateX,
                                                     int endCoordinateY, URI resourceId) {

        RouteStep routeStep = appRouteService.createAppRouteStep(routeId, startId, startCoordinateX, startCoordinateY,
                endID, endCoordinateX, endCoordinateY, resourceId);

        if (routeStep != null) {
            var jsonObject = new JSONObject();
            jsonObject.put("routeStepId", routeStep.getId().toString());
            jsonObject.put("message", "Successfully created the route step");
            return ResponseEntity.ok(jsonObject.toJSONString());
        } else {
            return ResponseEntity.badRequest().body("Could not create the route step");
        }
    }

    /**
     * This method deletes a route step
     *
     * @param routeId     id of the app route
     * @param routeStepId id of the route step
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> deleteAppRouteStep(URI routeId, URI routeStepId) {

        boolean deleted = appRouteService.deleteAppRouteStep(routeId, routeStepId);
        if (deleted) {
            return ResponseEntity.ok("Successfully deleted the route step with id: " + routeStepId);
        } else {
            return ResponseEntity.badRequest().body("Could not delete the route step");
        }
    }

    /**
     * This method returns a specific route step
     *
     * @param routeId     id of the app route
     * @param routeStepId id of the route step
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getAppRouteStep(URI routeId, URI routeStepId) {

        RouteStep routeStep = appRouteService.getSubroute(routeId, routeStepId);
        if (routeStep != null) {
            try {
                return ResponseEntity.ok(serializer.serialize(routeStep));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not serialize the route step");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not get the route step");
        }
    }

    /**
     * This method returns information regarding the endpoint
     *
     * @param routeId    id of the app route
     * @param endpointId id of the endpoint
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getEndpointInformation(URI routeId, URI endpointId) {

        EndpointInformation endpointInformation = appRouteService.getEndpointInformation(routeId, endpointId);
        if (endpointInformation != null) {
            try {
                return ResponseEntity.ok(objectMapper.writeValueAsString(endpointInformation));
            } catch (JsonProcessingException e) {
                return ResponseEntity.badRequest().body("Could not parse endpoint information to JSON");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not get endpoint information");
        }
    }

    /**
     * @return list of endpoint information
     */
    @Override
    public ResponseEntity<String> getAllEndpointInfo() {
        List<EndpointInformation> endpointInformations = appRouteService.getAllEndpointInfo();
        if (endpointInformations != null) {
            try {
                return ResponseEntity.ok(objectMapper.writeValueAsString(endpointInformations));
            } catch (JsonProcessingException e) {
                return ResponseEntity.badRequest().body("Could not parse endpoint information to JSON");
            }
        }
        return ResponseEntity.badRequest().body("Could not get endpoint information");
    }

    /**
     * This method updates the route deploy method of all app route and route steps
     *
     * @param deployMethod route deploy method
     * @return a suitable http response depending on success
     */
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

    /**
     * This method returns the route deploy method
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getRouteDeployMethod() {
        List<RouteDeployMethod> routeDeployMethods = routeDeployMethodRepository.findAll();
        try {
            return ResponseEntity.ok(objectMapper.writeValueAsString(routeDeployMethods));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Could not get deploy method from the app routes");
        }
    }

    /**
     * This method updates the deploy method from every app route and route step
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

    /**
     * This method validates the created app route for correctness and completeness
     *
     * @param routeId id of the app route
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> validateAppRoute(URI routeId) {

        String validationMessage = appRouteService.validateAppRoute(routeId);
        return ResponseEntity.ok(validationMessage);

    }
}
