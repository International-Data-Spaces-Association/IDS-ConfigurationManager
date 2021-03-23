package de.fraunhofer.isst.configmanager.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.AppRoute;
import de.fraunhofer.iais.eis.AppRouteImpl;
import de.fraunhofer.iais.eis.RouteStepImpl;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configlists.RouteDeployMethodRepository;
import de.fraunhofer.isst.configmanager.configmanagement.entities.routedeploymethod.DeployMethod;
import de.fraunhofer.isst.configmanager.configmanagement.entities.routedeploymethod.RouteDeployMethod;
import de.fraunhofer.isst.configmanager.configmanagement.service.AppRouteService;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.util.Utility;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
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

/**
 * The controller class implements the AppRouteApi and offers the possibilities to manage
 * the app routes in the configuration manager.
 */
@RestController
@RequestMapping("/api/ui")
@Tag(name = "App Route Management", description = "Endpoints for managing the app routes in the " +
        "configuration manager")
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AppRouteUIController implements AppRouteApi {
    transient ConfigModelService configModelService;
    transient AppRouteService appRouteService;
    transient Serializer serializer;
    transient RouteDeployMethodRepository routeDeployMethodRepository;
    transient ObjectMapper objectMapper;

    @Autowired
    public AppRouteUIController(final ConfigModelService configModelService,
                                final AppRouteService appRouteService,
                                final Serializer serializer,
                                final RouteDeployMethodRepository routeDeployMethodRepository,
                                final ObjectMapper objectMapper) {
        this.configModelService = configModelService;
        this.appRouteService = appRouteService;
        this.serializer = serializer;
        this.routeDeployMethodRepository = routeDeployMethodRepository;
        this.objectMapper = objectMapper;

        if (routeDeployMethodRepository.count() == 0) {
            final var routeDeployMethod = new RouteDeployMethod();
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
    public ResponseEntity<String> createAppRoute(final String description) {
        log.info(">> POST /approute description: " + description);
        final var appRoute = appRouteService.createAppRoute(description);

        if (appRoute != null) {
            final var jsonObject = new JSONObject();
            jsonObject.put("id", appRoute.getId().toString());
            jsonObject.put("message", "Created a new app route successfully");
            log.info("---- [AppRouteUIController createAppRoute] Created app route successfully");
            return ResponseEntity.ok(jsonObject.toJSONString());
        } else {
            log.info("---- [AppRouteUIController createAppRoute] Could not create app route");
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
    public ResponseEntity<String> updateAppRoute(final URI routeId, final String description) {
        log.info(">> PUT /approute routeId: " + routeId + " description: " + description);

        final boolean updated = appRouteService.updateAppRoute(routeId, description);

        if (updated) {
            log.info("---- [AppRouteUIController updateAppRoute] App route with id: " + routeId + " is updated.");
            return ResponseEntity.ok(Utility.jsonMessage("message",
                    "App route with id: " + routeId + " is updated."));
        } else {
            log.info("---- [AppRouteUIController updateAppRoute] Could not update app route with id: " + routeId);
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
    public ResponseEntity<String> deleteAppRoute(final URI routeId) {
        log.info(">> DELETE /approute routeId: " + routeId);

        final boolean deleted = appRouteService.deleteAppRoute(routeId);
        if (deleted) {
            log.info("---- [AppRouteUIController deleteAppRoute] App route with id: " + routeId + " is deleted.");
            return ResponseEntity.ok(Utility.jsonMessage("message",
                    "App route with id: " + routeId + " is deleted."));
        } else {
            log.info("---- [AppRouteUIController deleteAppRoute] Could not delete app route with id: " + routeId);
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
    public ResponseEntity<String> getAppRoute(final URI routeId) {
        log.info(">> GET /approute routeId: " + routeId);

        final var appRoute = appRouteService.getAppRoute(routeId);

        if (appRoute != null) {
            try {
                final var appRouteString = serializer.serialize(appRoute);
                log.info("---- [AppRouteUIController getAppRoute] Returning app route");
                return ResponseEntity.ok(appRouteString);
            } catch (IOException e) {
                log.error("---- [AppRouteUIController getAppRoute] Problem while serializing app route!");
                log.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not " +
                        "serialize app route to jsonld");
            }
        } else {
            log.info("---- [AppRouteUIController getAppRoute] Could not get app route with id: " + routeId);
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

        final var appRouteList = appRouteService.getAppRoutes();
        try {
            if (appRouteList == null) {
                log.info("---- [AppRouteUIController getAppRoutes] Returning empty list since no app routes are present");
                return ResponseEntity.ok(objectMapper.writeValueAsString(new JSONArray()));
            } else {
                log.info("---- [AppRouteUIController getAppRoutes] Returning list of app routes");
                return ResponseEntity.ok(serializer.serialize(appRouteList));
            }
        } catch (IOException e) {
            log.error("---- [AppRouteUIController getAppRoutes] Problem while serializing app routes list!");
            log.error(e.getMessage(), e);
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
    public ResponseEntity<String> createAppRouteStep(final URI routeId, final URI startId,
                                                     final int startCoordinateX,
                                                     final int startCoordinateY, final URI endID,
                                                     final int endCoordinateX,
                                                     final int endCoordinateY,
                                                     final URI resourceId) {
        log.info(">> POST /approute/step routeId: " + routeId + " startId: " + startId + " " +
                "startCoordinateX: " + startCoordinateX
                + " startCoordinateY: " + startCoordinateY + " endID: " + endID + " " +
                "endCoordinateX: " + endCoordinateX
                + " endCoordinateY: " + endCoordinateY + " resourceId: " + resourceId);

        final var routeStep = appRouteService.createAppRouteStep(routeId, startId,
                startCoordinateX, startCoordinateY,
                endID, endCoordinateX, endCoordinateY, resourceId);

        if (routeStep != null) {
            final var jsonObject = new JSONObject();
            jsonObject.put("routeStepId", routeStep.getId().toString());
            jsonObject.put("message", "Successfully created the route step");
            log.info("---- [AppRouteUIController createAppRouteStep] Successfully created the route step");
            return ResponseEntity.ok(jsonObject.toJSONString());
        } else {
            log.warn("---- [AppRouteUIController createAppRouteStep] Could not create the route step");
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
    public ResponseEntity<String> deleteAppRouteStep(final URI routeId, final URI routeStepId) {
        log.info(">> DELETE /approute/step routeId: " + routeId + " routeStepId: " + routeStepId);

        final boolean deleted = appRouteService.deleteAppRouteStep(routeId, routeStepId);
        if (deleted) {
            log.info("---- [AppRouteUIController deleteAppRouteStep] Successfully deleted the route step with id:" + routeStepId);
            return ResponseEntity.ok("Successfully deleted the route step with id: " + routeStepId);
        } else {
            log.warn("---- [AppRouteUIController deleteAppRouteStep] Could not delete the route step");
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
    public ResponseEntity<String> getAppRouteStep(final URI routeId, final URI routeStepId) {
        log.info(">> GET /approute/step routeId: " + routeId + " routeStepId: " + routeStepId);

        final var routeStep = appRouteService.getSubroute(routeId, routeStepId);
        if (routeStep != null) {
            try {
                final var routeStepString = serializer.serialize(routeStep);
                log.info("---- [AppRouteUIController getAppRouteStep] Route step found");
                return ResponseEntity.ok(routeStepString);
            } catch (IOException e) {
                log.error("---- [AppRouteUIController getAppRouteStep] Problem while serializing route step!");
                log.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not " +
                        "serialize the route step");
            }
        } else {
            log.warn("---- [AppRouteUIController getAppRouteStep] Route step is null");
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
    public ResponseEntity<String> getEndpointInformation(final URI routeId, final URI endpointId) {
        log.info(">> GET /approute/step/endpoint/info routeId: " + routeId + " endpointId: " + endpointId);

        final var endpointInformation = appRouteService.getEndpointInformation(routeId,
                endpointId);
        if (endpointInformation != null) {
            try {
                final var endpointInfo = objectMapper.writeValueAsString(endpointInformation);
                log.info("---- [AppRouteUIController getEndpointInformation Returning endpoint information");
                return ResponseEntity.ok(endpointInfo);
            } catch (JsonProcessingException e) {
                log.error("---- [AppRouteUIController getEndpointInformation Could not parse endpoint Information to JSON!");
                log.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not " +
                        "parse endpoint information to JSON");
            }
        } else {
            log.warn("---- [AppRouteUIController getEndpointInformation] Endpoint Information is null");
            return ResponseEntity.badRequest().body("Could not get endpoint information");
        }
    }

    /**
     * @return list of endpoint information
     */
    @Override
    public ResponseEntity<String> getAllEndpointInfo() {
        log.info(">> GET /approute/step/endpoints/info");

        final var endpointInformations = appRouteService.getAllEndpointInfo();
        if (endpointInformations != null) {
            try {
                final var endpointInfos = objectMapper.writeValueAsString(endpointInformations);
                log.info("---- [AppRouteUIController getAllEndpointInfo] Returning all endpoint information");
                return ResponseEntity.ok(endpointInfos);
            } catch (JsonProcessingException e) {
                log.error("---- [AppRouteUIController getAllEndpointInfo] Could not parse endpoint informations to JSON!");
                log.error(e.getMessage(), e);
                return ResponseEntity.badRequest().body("Could not parse endpoint information to " +
                        "JSON");
            }
        }
        log.info("---- [AppRouteUIController getAllEndpointInfo] Endpoint information list is null");
        return ResponseEntity.badRequest().body("Could not get endpoint information");
    }

    /**
     * This method updates the route deploy method of all app route and route steps
     *
     * @param deployMethod route deploy method
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateRouteDeployMethod(final DeployMethod deployMethod) {
        log.info(">> PUT /route/deploymethod deployMethod: " + deployMethod);

        if (routeDeployMethodRepository.count() != 0) {
            final var existingDeployMethod = routeDeployMethodRepository.findAll().get(0);
            existingDeployMethod.setDeployMethod(deployMethod);
            routeDeployMethodRepository.save(existingDeployMethod);
            // Updates the deploy method from the app routes and route steps
            updateDeployMethodFromRoutes(deployMethod);
            log.info("---- [AppRouteUIController updateRouteDeployMethod] Updated successfully the route deploy method");
            return ResponseEntity.ok("Updated successfully the route deploy method");
        } else {
            log.warn("---- [AppRouteUIController updateRouteDeployMethod] Could not update the route deploy method");
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

        final var routeDeployMethods = routeDeployMethodRepository.findAll();
        try {
            log.info("Returning the deploy route method");
            return ResponseEntity.ok(objectMapper.writeValueAsString(routeDeployMethods));
        } catch (JsonProcessingException e) {
            log.error("Could not get deploy method from the app routes!");
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body("Could not get deploy method from the app " +
                    "routes");
        }
    }

    /**
     * This method updates the deploy method from every app route and route step
     *
     * @param deployMethod deploy method of the route
     */
    private void updateDeployMethodFromRoutes(final DeployMethod deployMethod) {
        final var appRouteList =
                (ArrayList<AppRoute>) configModelService.getConfigModel().getAppRoute();
        if (appRouteList != null) {
            // Update deploy method from app routes
            for (var appRoute : appRouteList) {
                if (appRoute != null) {
                    final var appRouteImpl = (AppRouteImpl) appRoute;
                    appRouteImpl.setRouteDeployMethod(deployMethod.toString());

                    // Update deploy method from route steps
                    if (appRoute.getHasSubRoute() != null) {
                        for (var routeStep : appRoute.getHasSubRoute()) {
                            if (routeStep != null) {
                                final var routeStepImpl = (RouteStepImpl) routeStep;
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
