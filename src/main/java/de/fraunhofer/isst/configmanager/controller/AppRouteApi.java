package de.fraunhofer.isst.configmanager.controller;

import de.fraunhofer.isst.configmanager.configmanagement.entities.routeDeployMethod.DeployMethod;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


public interface AppRouteApi {

    // Interfaces for managing app routes
    @PostMapping(value = "/approute", produces = "application/ld+json")
    @Operation(summary = "Creates a new app route")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Created a new app route")})
    ResponseEntity<String> createAppRoute();

    @PutMapping(value = "/approute", produces = "application/ld+json")
    @Operation(summary = "Updates the given app route")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Created a new app route")})
    ResponseEntity<String> updateAppRoute(@RequestParam("routeId") URI routeId);

    @DeleteMapping(value = "/approute", produces = "application/ld+json")
    @Operation(summary = "Deletes the given app route")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully deleted the app route")})
    ResponseEntity<String> deleteAppRoute(@RequestParam("routeId") URI routeId);

    @GetMapping(value = "/approute", produces = "application/ld+json")
    @Operation(summary = "Returns the given app route")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully get the app route")})
    ResponseEntity<String> getAppRoute(@RequestParam("routeId") URI routeId);

    @GetMapping(value = "/approutes", produces = "application/ld+json")
    @Operation(summary = "Returns all app routes")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully get all app routes")})
    ResponseEntity<String> getAppRoutes();

    @PostMapping(value = "/approute/step", produces = "application/ld+json")
    @Operation(summary = "Creates a new subroute for the app route")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully created a new subroute for the app route")})
    ResponseEntity<String> createAppRouteStep(@RequestParam(value = "routeId") URI routeId,
                                              @RequestParam(value = "startId") URI startId,
                                              @RequestParam(value = "startCoordinateX") int startCoordinateX,
                                              @RequestParam(value = "startCoordinateY") int startCoordinateY,
                                              @RequestParam(value = "endId") URI endID,
                                              @RequestParam(value = "endCoordinateX") int endCoordinateX,
                                              @RequestParam(value = "endCoordinateY") int endCoordinateY,
                                              @RequestParam(value = "resourceId", required = false) URI resourceId);

    @GetMapping(value = "/approute/validate", produces = "application/ld+json")
    @Operation(summary = "Validates the created routes for completeness and correctness")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Validated the created routes for completeness" +
            " and correctness")})
    ResponseEntity<String> validateAppRoute(@RequestParam(value = "routeId") URI routeId);

    // Interfaces for managing route steps
    @GetMapping(value = "/approute/step", produces = "application/ld+json")
    @Operation(summary = "Returns the specific route step")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully returned the specific route step")})
    ResponseEntity<String> getAppRouteStep(@RequestParam(value = "routeId") URI routeId,
                                           @RequestParam(value = "routeStepId") URI routeStepId);

    @GetMapping(value = "/approute/step/endpoint/info", produces = "application/ld+json")
    @Operation(summary = "Returns for a route step the specific endpoint information")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully returned for a route step " +
            "the specific endpoint information")})
    ResponseEntity<String> getEndpointInformation(@RequestParam(value = "routeId") URI routeId,
                                                  @RequestParam(value = "endpointId") URI endpointId);


    // Interfaces for managing deploy methods of the routes
    @PutMapping(value = "/route/deploymethod", produces = "application/ld+json")
    @Operation(summary = "Updates the route deploy method for all routes")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully updated the route deploy method")})
    ResponseEntity<String> updateRouteDeployMethod(@RequestParam("deployMethod") DeployMethod deployMethod);

    @GetMapping(value = "/route/deploymethod", produces = "application/ld+json")
    @Operation(summary = "Returns the route deploy method for all routes")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully returned the route deploy method")})
    ResponseEntity<String> getRouteDeployMethods();

}
