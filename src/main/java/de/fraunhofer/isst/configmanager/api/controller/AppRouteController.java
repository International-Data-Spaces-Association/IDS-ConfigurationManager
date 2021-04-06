package de.fraunhofer.isst.configmanager.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.AppRouteImpl;
import de.fraunhofer.iais.eis.RouteStepImpl;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.api.AppRouteApi;
import de.fraunhofer.isst.configmanager.api.service.AppRouteService;
import de.fraunhofer.isst.configmanager.api.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.model.configlists.RouteDeployMethodRepository;
import de.fraunhofer.isst.configmanager.model.routedeploymethod.DeployMethod;
import de.fraunhofer.isst.configmanager.model.routedeploymethod.RouteDeployMethod;
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

/**
 * The api class implements the AppRouteApi and offers the possibilities to manage
 * the app routes in the configuration manager.
 */
@Slf4j
@RestController
@RequestMapping("/api/ui")
@Tag(name = "App Route Management", description = "Endpoints for managing the app routes in the configuration manager")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AppRouteController implements AppRouteApi {

    transient ConfigModelService configModelService;
    transient AppRouteService appRouteService;
    transient Serializer serializer;
    transient RouteDeployMethodRepository routeDeployMethodRepository;
    transient ObjectMapper objectMapper;

    @Autowired
    public AppRouteController(final ConfigModelService configModelService,
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
        ResponseEntity<String> response;

        final var appRoute = appRouteService.createAppRoute(description);

        if (appRoute != null) {
            final var jsonObject = new JSONObject();
            jsonObject.put("id", appRoute.getId().toString());
            jsonObject.put("message", "Created a new app route successfully");
            log.info("---- [AppRouteController createAppRoute] Created app route successfully");
            response = ResponseEntity.ok(jsonObject.toJSONString());
        } else {
            log.info("---- [AppRouteController createAppRoute] Could not create app route");
            response = ResponseEntity.badRequest().body("Could not create an app route");
        }

        return response;
    }

    /**
     * This method updates an app route.
     *
     * @param routeId     id of the app route
     * @param description description of the app route
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateAppRoute(final URI routeId, final String description) {
        log.info(">> PUT /approute routeId: " + routeId + " description: " + description);
        ResponseEntity<String> response;

        final boolean updated = appRouteService.updateAppRoute(routeId, description);

        if (updated) {
            log.info("---- [AppRouteController updateAppRoute] App route with id: " + routeId + " is updated.");
            response = ResponseEntity.ok(Utility.jsonMessage("message", "App route with id: " + routeId + " is updated."));
        } else {
            log.info("---- [AppRouteController updateAppRoute] Could not update app route with id: " + routeId);
            response = ResponseEntity.badRequest().body("Could not update app route with id: " + routeId);
        }

        return response;
    }

    /**
     * This method deletes an app route.
     *
     * @param routeId id of the app route
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> deleteAppRoute(final URI routeId) {
        log.info(">> DELETE /approute routeId: " + routeId);
        ResponseEntity<String> response;

        final boolean deleted = appRouteService.deleteAppRoute(routeId);

        if (deleted) {
            log.info("---- [AppRouteController deleteAppRoute] App route with id: " + routeId + " is deleted.");
            response = ResponseEntity.ok(Utility.jsonMessage("message", "App route with id: " + routeId + " is deleted."));
        } else {
            log.info("---- [AppRouteController deleteAppRoute] Could not delete app route with id: " + routeId);
            response = ResponseEntity.badRequest().body("Could not delete app route with id: " + routeId);
        }

        return response;
    }

    /**
     * This method returns a specific app route.
     *
     * @param routeId id of the app route
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getAppRoute(final URI routeId) {
        log.info(">> GET /approute routeId: " + routeId);
        ResponseEntity<String> response;

        final var appRoute = appRouteService.getAppRoute(routeId);

        if (appRoute != null) {
            try {
                final var appRouteString = serializer.serialize(appRoute);
                log.info("---- [AppRouteController getAppRoute] Returning app route");
                response = ResponseEntity.ok(appRouteString);
            } catch (IOException e) {
                log.error("---- [AppRouteController getAppRoute] Problem while serializing app route!");
                log.error(e.getMessage(), e);
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not serialize app route to jsonld");
            }
        } else {
            log.info("---- [AppRouteController getAppRoute] Could not get app route with id: " + routeId);
            response = ResponseEntity.badRequest().body("Could not get app route with id: " + routeId);
        }

        return response;
    }

    /**
     * This method returns a list of app routes.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getAppRoutes() {
        log.info(">> GET /approutes");
        ResponseEntity<String> response;

        final var appRouteList = appRouteService.getAppRoutes();

        try {
            if (appRouteList == null) {
                log.info("---- [AppRouteController getAppRoutes] Returning empty list since no app routes are present");
                response = ResponseEntity.ok(objectMapper.writeValueAsString(new JSONArray()));
            } else {
                log.info("---- [AppRouteController getAppRoutes] Returning list of app routes");
                response = ResponseEntity.ok(serializer.serialize(appRouteList));
            }
        } catch (IOException e) {
            log.error("---- [AppRouteController getAppRoutes] Problem while serializing app routes list!");
            log.error(e.getMessage(), e);
            response = ResponseEntity.badRequest().body("Problems while serializing");
        }

        return response;
    }

    /**
     * This method creates a route step for an app route with the given parameters.
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
        log.info(">> POST /approute/step routeId: " + routeId + " startId: " + startId
                + " startCoordinateX: " + startCoordinateX
                + " startCoordinateY: " + startCoordinateY + " endID: " + endID
                + " endCoordinateX: " + endCoordinateX
                + " endCoordinateY: " + endCoordinateY + " resourceId: " + resourceId);
        ResponseEntity<String> response;

        final var routeStep = appRouteService.createAppRouteStep(routeId, startId,
                startCoordinateX, startCoordinateY,
                endID, endCoordinateX, endCoordinateY, resourceId);

        if (routeStep != null) {
            final var jsonObject = new JSONObject();
            jsonObject.put("routeStepId", routeStep.getId().toString());
            jsonObject.put("message", "Successfully created the route step");

            log.info("---- [AppRouteController createAppRouteStep] Successfully created the route step");
            response = ResponseEntity.ok(jsonObject.toJSONString());
        } else {
            log.warn("---- [AppRouteController createAppRouteStep] Could not create the route step");
            response = ResponseEntity.badRequest().body("Could not create the route step");
        }

        return response;
    }

    /**
     * This method deletes a route step.
     *
     * @param routeId     id of the app route
     * @param routeStepId id of the route step
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> deleteAppRouteStep(final URI routeId, final URI routeStepId) {
        log.info(">> DELETE /approute/step routeId: " + routeId + " routeStepId: " + routeStepId);
        ResponseEntity<String> response;

        final boolean deleted = appRouteService.deleteAppRouteStep(routeId, routeStepId);

        if (deleted) {
            log.info("---- [AppRouteController deleteAppRouteStep] Successfully deleted the route step with id:" + routeStepId);
            response = ResponseEntity.ok("Successfully deleted the route step with id: " + routeStepId);
        } else {
            log.warn("---- [AppRouteController deleteAppRouteStep] Could not delete the route step");
            response = ResponseEntity.badRequest().body("---- Could not delete the route step");
        }

        return response;
    }

    /**
     * This method returns a specific route step.
     *
     * @param routeId     id of the app route
     * @param routeStepId id of the route step
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getAppRouteStep(final URI routeId, final URI routeStepId) {
        log.info(">> GET /approute/step routeId: " + routeId + " routeStepId: " + routeStepId);
        ResponseEntity<String> response;

        final var routeStep = appRouteService.getSubroute(routeId, routeStepId);

        if (routeStep != null) {
            try {
                final var routeStepString = serializer.serialize(routeStep);
                log.info("---- [AppRouteController getAppRouteStep] Route step found");
                response = ResponseEntity.ok(routeStepString);
            } catch (IOException e) {
                log.error("---- [AppRouteController getAppRouteStep] Problem while serializing route step!");
                log.error(e.getMessage(), e);
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not serialize the route step");
            }
        } else {
            log.warn("---- [AppRouteController getAppRouteStep] Route step is null");
            response = ResponseEntity.badRequest().body("Could not get the route step");
        }

        return response;
    }

    /**
     * This method returns information regarding the endpoint.
     *
     * @param routeId    id of the app route
     * @param endpointId id of the endpoint
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getEndpointInformation(final URI routeId, final URI endpointId) {
        log.info(">> GET /approute/step/endpoint/info routeId: " + routeId + " endpointId: " + endpointId);
        ResponseEntity<String> response;

        final var endpointInformation = appRouteService.getEndpointInformation(routeId, endpointId);

        if (endpointInformation != null) {
            try {
                final var endpointInfo = objectMapper.writeValueAsString(endpointInformation);
                log.info("---- [AppRouteController getEndpointInformation Returning endpoint information");
                response = ResponseEntity.ok(endpointInfo);
            } catch (JsonProcessingException e) {
                log.error("---- [AppRouteController getEndpointInformation Could not parse endpoint Information to JSON!");
                log.error(e.getMessage(), e);
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not parse endpoint information to JSON");
            }
        } else {
            log.warn("---- [AppRouteController getEndpointInformation] Endpoint Information is null");
            response = ResponseEntity.badRequest().body("Could not get endpoint information");
        }

        return response;
    }

    /**
     * @return list of endpoint information
     */
    @Override
    public ResponseEntity<String> getAllEndpointInfo() {
        log.info(">> GET /approute/step/endpoints/info");
        ResponseEntity<String> response;

        final var endpointInformations = appRouteService.getAllEndpointInfo();

        if (endpointInformations != null) {
            try {
                final var endpointInfos = objectMapper.writeValueAsString(endpointInformations);
                log.info("---- [AppRouteController getAllEndpointInfo] Returning all endpoint information");
                response = ResponseEntity.ok(endpointInfos);
            } catch (JsonProcessingException e) {
                log.error("---- [AppRouteController getAllEndpointInfo] Could not parse endpoint informations to JSON!");
                log.error(e.getMessage(), e);
                response = ResponseEntity.badRequest().body("Could not parse endpoint information to JSON");
            }
        } else {
            log.info("---- [AppRouteController getAllEndpointInfo] Endpoint information list is null");
            response = ResponseEntity.badRequest().body("Could not get endpoint information");
        }

        return response;
    }

    /**
     * This method updates the route deploy method of all app route and route steps.
     *
     * @param deployMethod route deploy method
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateRouteDeployMethod(final DeployMethod deployMethod) {
        log.info(">> PUT /route/deploymethod deployMethod: " + deployMethod);
        ResponseEntity<String> response;

        if (routeDeployMethodRepository.count() != 0) {
            final var existingDeployMethod = routeDeployMethodRepository.findAll().get(0);
            existingDeployMethod.setDeployMethod(deployMethod);

            routeDeployMethodRepository.save(existingDeployMethod);

            // Updates the deploy method from the app routes and route steps
            updateDeployMethodFromRoutes(deployMethod);

            log.info("---- [AppRouteController updateRouteDeployMethod] Updated successfully the route deploy method");

            response = ResponseEntity.ok("Updated successfully the route deploy method");
        } else {
            log.warn("---- [AppRouteController updateRouteDeployMethod] Could not update the route deploy method");
            response = ResponseEntity.badRequest().body("Could not update the route deploy method");
        }

        return response;
    }

    /**
     * This method returns the route deploy method.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getRouteDeployMethod() {
        log.info(">> GET /route/deploymethod");
        ResponseEntity<String> response;

        final var routeDeployMethods = routeDeployMethodRepository.findAll();

        try {
            log.info("---- [AppRouteController getRouteDeployMethod] Returning the deploy route method");
            response = ResponseEntity.ok(objectMapper.writeValueAsString(routeDeployMethods));
        } catch (JsonProcessingException e) {
            log.error("---- [AppRouteController getRouteDeployMethod] Could not get deploy method from the app routes!");
            log.error(e.getMessage(), e);
            response = ResponseEntity.badRequest().body("Could not get deploy method from the app routes");
        }

        return response;
    }

    /**
     * This method updates the deploy method from every app route and route step.
     *
     * @param deployMethod deploy method of the route
     */
    private void updateDeployMethodFromRoutes(final DeployMethod deployMethod) {
        log.info("---- [AppRouteController updateDeployMethodFromRoutes] Updating deploymethod for every app route and route step...");

        final var appRouteList = configModelService.getConfigModel().getAppRoute();
        if (appRouteList != null) {
            // Update deploy method from app routes
            for (final var appRoute : appRouteList) {
                if (appRoute != null) {
                    final var appRouteImpl = (AppRouteImpl) appRoute;
                    appRouteImpl.setRouteDeployMethod(deployMethod.toString());

                    // Update deploy method from route steps
                    if (appRoute.getHasSubRoute() != null) {
                        for (final var routeStep : appRoute.getHasSubRoute()) {
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
