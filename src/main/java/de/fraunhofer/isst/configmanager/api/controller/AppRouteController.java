package de.fraunhofer.isst.configmanager.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.AppRoute;
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
 * The api class implements the AppRouteApi and offers the possibilities to manage
 * the app routes in the configuration manager.
 */
@Slf4j
@RestController
@RequestMapping("/api/ui")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "App Route Management", description = "Endpoints for managing the app routes in the configuration manager")
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
        if (log.isInfoEnabled()) {
            log.info(">> POST /approute description: " + description);
        }

        ResponseEntity<String> response;

        final var appRoute = appRouteService.createAppRoute(description);

        if (appRoute != null) {
            final var jsonObject = new JSONObject();
            jsonObject.put("id", appRoute.getId().toString());
            jsonObject.put("message", "Created a new app route successfully");

            if (log.isInfoEnabled()) {
                log.info("---- [AppRouteController createAppRoute] Created app route successfully");
            }
            response = ResponseEntity.ok(jsonObject.toJSONString());
        } else {
            if (log.isInfoEnabled()) {
                log.info("---- [AppRouteController createAppRoute] Could not create app route");
            }
            response = ResponseEntity.badRequest().body("Can not create an app route");
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
        if (log.isInfoEnabled()) {
            log.info(">> DELETE /approute routeId: " + routeId);
        }

        ResponseEntity<String> response;

        final boolean deleted = appRouteService.deleteAppRoute(routeId);

        if (deleted) {
            if (log.isInfoEnabled()) {
                log.info("---- [AppRouteController deleteAppRoute] App route with id: " + routeId + " is deleted.");
            }

            response = ResponseEntity.ok(Utility.jsonMessage("message", "App route with id: " + routeId + " is deleted."));
        } else {
            if (log.isInfoEnabled()) {
                log.info("---- [AppRouteController deleteAppRoute] Could not delete app route with id: " + routeId);
            }

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
        if (log.isInfoEnabled()) {
            log.info(">> GET /approute routeId: " + routeId);
        }

        ResponseEntity<String> response;

        final var appRoute = appRouteService.getAppRoute(routeId);

        if (appRoute != null) {
            try {
                final var appRouteString = serializer.serialize(appRoute);
                if (log.isInfoEnabled()) {
                    log.info("---- [AppRouteController getAppRoute] Returning app route");
                }
                response = ResponseEntity.ok(appRouteString);
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("---- [AppRouteController getAppRoute] Problem while serializing app route!");
                    log.error(e.getMessage(), e);
                }
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not serialize app route to jsonld");
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info("---- [AppRouteController getAppRoute] Could not get app route with id: " + routeId);
            }
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
        if (log.isInfoEnabled()) {
            log.info(">> GET /approutes");
        }
        ResponseEntity<String> response;

        final var appRouteList = appRouteService.getAppRoutes();

        try {
            if (appRouteList == null) {
                if (log.isInfoEnabled()) {
                    log.info("---- [AppRouteController getAppRoutes] Returning empty list since no app routes are present");
                }
                response = ResponseEntity.ok(serializer.serialize(new ArrayList<AppRoute>()));
            } else {
                if (log.isInfoEnabled()) {
                    log.info("---- [AppRouteController getAppRoutes] Returning list of app routes");
                }
                response = ResponseEntity.ok(serializer.serialize(appRouteList));
            }
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("---- [AppRouteController getAppRoutes] Problem while serializing app routes list!");
                log.error(e.getMessage(), e);
            }
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
        if (log.isInfoEnabled()) {
            log.info(">> POST /approute/step routeId: " + routeId + " startId: " + startId
                    + " startCoordinateX: " + startCoordinateX
                    + " startCoordinateY: " + startCoordinateY + " endID: " + endID
                    + " endCoordinateX: " + endCoordinateX
                    + " endCoordinateY: " + endCoordinateY + " resourceId: " + resourceId);
        }
        ResponseEntity<String> response;

        final var routeStep = appRouteService.createAppRouteStep(routeId, startId,
                startCoordinateX, startCoordinateY,
                endID, endCoordinateX, endCoordinateY, resourceId);

        if (routeStep != null) {
            final var jsonObject = new JSONObject();
            jsonObject.put("routeStepId", routeStep.getId().toString());
            jsonObject.put("message", "Successfully created the route step");

            if (log.isInfoEnabled()) {
                log.info("---- [AppRouteController createAppRouteStep] Successfully created the route step");
            }
            response = ResponseEntity.ok(jsonObject.toJSONString());
        } else {
            if (log.isWarnEnabled()) {
                log.warn("---- [AppRouteController createAppRouteStep] Could not create the route step");
            }
            response = ResponseEntity.badRequest().body("Could not create the route step");
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
        if (log.isInfoEnabled()) {
            log.info(">> GET /approute/step/endpoint/info routeId: " + routeId + " endpointId: " + endpointId);
        }
        ResponseEntity<String> response;

        final var endpointInformation = appRouteService.getEndpointInformation(routeId, endpointId);

        if (endpointInformation != null) {
            try {
                final var endpointInfo = objectMapper.writeValueAsString(endpointInformation);
                if (log.isInfoEnabled()) {
                    log.info("---- [AppRouteController getEndpointInformation Returning endpoint information");
                }
                response = ResponseEntity.ok(endpointInfo);
            } catch (JsonProcessingException e) {
                if (log.isErrorEnabled()) {
                    log.error("---- [AppRouteController getEndpointInformation Could not parse endpoint Information to JSON!");
                    log.error(e.getMessage(), e);
                }
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not parse endpoint information to JSON");
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn("---- [AppRouteController getEndpointInformation] Endpoint Information is null");
            }
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
        if (log.isInfoEnabled()) {
            log.info(">> PUT /route/deploymethod deployMethod: " + deployMethod);
        }
        ResponseEntity<String> response;

        if (routeDeployMethodRepository.count() != 0) {
            final var existingDeployMethod = routeDeployMethodRepository.findAll().get(0);
            existingDeployMethod.setDeployMethod(deployMethod);

            routeDeployMethodRepository.save(existingDeployMethod);

            // Updates the deploy method from the app routes and route steps
            updateDeployMethodFromRoutes(deployMethod);

            if (log.isInfoEnabled()) {
                log.info("---- [AppRouteController updateRouteDeployMethod] Updated successfully the route deploy method");
            }

            response = ResponseEntity.ok("Updated successfully the route deploy method");
        } else {
            if (log.isWarnEnabled()) {
                log.warn("---- [AppRouteController updateRouteDeployMethod] Could not update the route deploy method");
            }
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
        if (log.isInfoEnabled()) {
            log.info(">> GET /route/deploymethod");
        }
        ResponseEntity<String> response;

        final var routeDeployMethods = routeDeployMethodRepository.findAll();

        try {
            if (log.isInfoEnabled()) {
                log.info("---- [AppRouteController getRouteDeployMethod] Returning the deploy route method");
            }
            response = ResponseEntity.ok(objectMapper.writeValueAsString(routeDeployMethods));
        } catch (JsonProcessingException e) {
            if (log.isErrorEnabled()) {
                log.error("---- [AppRouteController getRouteDeployMethod] Could not get deploy method from the app routes!");
                log.error(e.getMessage(), e);
            }
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
        if (log.isInfoEnabled()) {
            log.info("---- [AppRouteController updateDeployMethodFromRoutes] Updating deploymethod for every app route and route step...");
        }

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
