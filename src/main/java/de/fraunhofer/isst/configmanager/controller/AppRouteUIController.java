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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.info(">> POST /approute description: " + description);
        AppRoute appRoute = appRouteService.createAppRoute(description);

        if (appRoute != null) {
            var jsonObject = new JSONObject();
            jsonObject.put("id", appRoute.getId().toString());
            jsonObject.put("message", "Created a new app route successfully");
            log.info("---- Created app route successfully");
            return ResponseEntity.ok(jsonObject.toJSONString());
        } else {
            log.info("---- Could not create app route");
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
        log.info(">> PUT /approute routeId: " + routeId + " description: " + description);

        boolean updated = appRouteService.updateAppRoute(routeId, description);

        if (updated) {
            log.info("---- App route with id: " + routeId + " is updated.");
            return ResponseEntity.ok(Utility.jsonMessage("message", "App route with id: " + routeId + " is updated."));
        } else {
            log.info("---- Could not update app route with id: " + routeId);
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
        log.info(">> DELETE /approute routeId: " + routeId);

        boolean deleted = appRouteService.deleteAppRoute(routeId);
        if (deleted) {
            log.info("---- App route with id: " + routeId + " is deleted.");
            return ResponseEntity.ok(Utility.jsonMessage("message", "App route with id: " + routeId + " is deleted."));
        } else {
            log.info("---- Could not delete app route with id: " + routeId);
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
        log.info(">> GET /approute routeId: " + routeId);

        AppRoute appRoute = appRouteService.getAppRoute(routeId);

        if (appRoute != null) {
            try {
                String appRouteString = serializer.serialize(appRoute);
                log.info("---- Returning app route");
                return ResponseEntity.ok(appRouteString);
            } catch (IOException e) {
                log.error("Problem while serializing app route", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not serialize app route to jsonld");
            }
        } else {
            log.info("---- Could not get app route with id: " + routeId);
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
        log.info(">> GET /approutes");

        List<AppRoute> appRouteList = appRouteService.getAppRoutes();
        try {
            if (appRouteList == null) {
                log.info("Returning empty list since no app routes are present");
                return ResponseEntity.ok(objectMapper.writeValueAsString(new JSONArray()));
            } else {
                log.info("Returning list of app routes");
                return ResponseEntity.ok(serializer.serialize(appRouteList));
            }
        } catch (IOException e) {
            log.error("Problem while serializing app routes list", e);
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
        log.info(">> POST /approute/step routeId: " + routeId + " startId: " + startId + " startCoordinateX: " + startCoordinateX
        + " startCoordinateY: "+ startCoordinateY + " endID: " + endID + " endCoordinateX: " + endCoordinateX
        + " endCoordinateY: " + endCoordinateY + " resourceId: " + resourceId);

        RouteStep routeStep = appRouteService.createAppRouteStep(routeId, startId, startCoordinateX, startCoordinateY,
                endID, endCoordinateX, endCoordinateY, resourceId);

        if (routeStep != null) {
            var jsonObject = new JSONObject();
            jsonObject.put("routeStepId", routeStep.getId().toString());
            jsonObject.put("message", "Successfully created the route step");
            log.info("Successfully created the route step");
            return ResponseEntity.ok(jsonObject.toJSONString());
        } else {
            log.warn("---- Could not create the route step");
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
        log.info(">> DELETE /approute/step routeId: " + routeId + " routeStepId: " + routeStepId);

        boolean deleted = appRouteService.deleteAppRouteStep(routeId, routeStepId);
        if (deleted) {
            log.info("Successfully deleted the route step with id:" + routeStepId);
            return ResponseEntity.ok("Successfully deleted the route step with id: " + routeStepId);
        } else {
            log.warn("Could not delete the route step");
            return ResponseEntity.badRequest().body("---- Could not delete the route step");
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
        log.info(">> GET /approute/step routeId: " + routeId + " routeStepId: " + routeStepId);

        RouteStep routeStep = appRouteService.getSubroute(routeId, routeStepId);
        if (routeStep != null) {
            try {
                String routeStepString = serializer.serialize(routeStep);
                log.info("Route step found");
                return ResponseEntity.ok(routeStepString);
            } catch (IOException e) {
                log.error("Problem while serializing route step", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not serialize the route step");
            }
        } else {
            log.warn("---- Route step is null");
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
        log.info(">> GET /approute/step/endpoint/info routeId: " + routeId + " endpointId: " + endpointId);

        EndpointInformation endpointInformation = appRouteService.getEndpointInformation(routeId, endpointId);
        if (endpointInformation != null) {
            try {
                String endpointInfo = objectMapper.writeValueAsString(endpointInformation);
                log.info("Returning endpoint information");
                return ResponseEntity.ok(endpointInfo);
            } catch (JsonProcessingException e) {
                log.error("Could not parse endpoint Information to JSON", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not parse endpoint information to JSON");
            }
        } else {
            log.warn("---- Endpoint Information is null");
            return ResponseEntity.badRequest().body("Could not get endpoint information");
        }
    }

    /**
     * @return list of endpoint information
     */
    @Override
    public ResponseEntity<String> getAllEndpointInfo() {
        log.info(">> GET /approute/step/endpoints/info");

        List<EndpointInformation> endpointInformations = appRouteService.getAllEndpointInfo();
        if (endpointInformations != null) {
            try {
                String endpointInfos = objectMapper.writeValueAsString(endpointInformations);
                log.info("Returning all endpoint information");
                return ResponseEntity.ok(endpointInfos);
            } catch (JsonProcessingException e) {
                log.error("Could not parse endpoint informations to JSON", e);
                return ResponseEntity.badRequest().body("Could not parse endpoint information to JSON");
            }
        }
        log.info("Endpoint information list is null");
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
        log.info(">> PUT /route/deploymethod deployMethod: " + deployMethod);

        if (routeDeployMethodRepository.count() != 0) {
            RouteDeployMethod existingDeployMethod = routeDeployMethodRepository.findAll().get(0);
            existingDeployMethod.setDeployMethod(deployMethod);
            routeDeployMethodRepository.save(existingDeployMethod);
            // Updates the deploy method from the app routes and route steps
            updateDeployMethodFromRoutes(deployMethod);
            log.info("Updated successfully the route deploy method");
            return ResponseEntity.ok("Updated successfully the route deploy method");
        } else {
            log.warn("---- Could not update the route deploy method");
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
        log.info(">> GET /route/deploymethod");

        List<RouteDeployMethod> routeDeployMethods = routeDeployMethodRepository.findAll();
        try {
            log.info("Returning the deploy route method");
            return ResponseEntity.ok(objectMapper.writeValueAsString(routeDeployMethods));
        } catch (JsonProcessingException e) {
            log.error("Could not get deploy method from the app routes", e);
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
}
