package de.fraunhofer.isst.configmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

public interface AppRouteStartApi {

    @PostMapping(value = "/approute/start", produces = "application/ld+json")
    @Operation(summary = "Create a new route start")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Created a new route start")})
    ResponseEntity<String> createAppRouteStart(@RequestParam(value = "routeId") URI routeId,
                                               @RequestParam(value = "accessUrl") String accessUrl,
                                               @RequestParam(value = "username", required = false) String username,
                                               @RequestParam(value = "password", required = false) String password);

    @PostMapping(value = "/approute/subroute/start", produces = "application/ld+json")
    @Operation(summary = "Create a new route start for the subroute")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Created a new route start")})
    ResponseEntity<String> createSubrouteStart(@RequestParam(value = "routeId") URI routeId,
                                               @RequestParam(value = "routeStepId", required = false) URI routeStepId,
                                               @RequestParam(value = "accessUrl") String accessUrl,
                                               @RequestParam(value = "username", required = false) String username,
                                               @RequestParam(value = "password", required = false) String password);

    @GetMapping(value = "/approute/start", produces = "application/ld+json")
    @Operation(summary = "Returns the start endpoint from the app route")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returned the start endpoint from the app route")})
    ResponseEntity<String> getAppRouteStart(@RequestParam(value = "routeId") URI routeId,
                                            @RequestParam("endpointId") URI endpointId);

    @GetMapping(value = "/approute/subroute/start", produces = "application/ld+json")
    @Operation(summary = "Returns the start endpoint from the subroute")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returned the start endpoint from the subroute")})
    ResponseEntity<String> getSubrouteStart(@RequestParam(value = "routeId") URI routeId,
                                            @RequestParam(value = "routeStepId") URI routeStepId,
                                            @RequestParam("endpointId") URI endpointId);

    @DeleteMapping(value = "/approute/start", produces = "application/ld+json")
    @Operation(summary = "Deletes the start endpoint from the app route")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Deleted the start endpoint from the app route")})
    ResponseEntity<String> deleteAppRouteStart(@RequestParam(value = "routeId") URI routeId,
                                               @RequestParam("endpointId") URI endpointId);

    @DeleteMapping(value = "/approute/subroute/start", produces = "application/ld+json")
    @Operation(summary = "Deletes the start endpoint from the subroute")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Deleted the start endpoint from the subroute")})
    ResponseEntity<String> deleteSubrouteStart(@RequestParam(value = "routeId") URI routeId,
                                               @RequestParam(value = "routeStepId") URI routeStepId,
                                               @RequestParam("endpointId") URI endpointId);

    @PutMapping(value = "/approute/start", produces = "application/ld+json")
    @Operation(summary = "Updates the endpoint in the app route")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Updated the endpoint in the app route")})
    ResponseEntity<String> updateAppRouteStart(@RequestParam(value = "routeId") URI routeId,
                                               @RequestParam(value = "endpointId") URI endpointId,
                                               @RequestParam(value = "accessUrl", required = false) String accessUrl,
                                               @RequestParam(value = "username", required = false) String username,
                                               @RequestParam(value = "password", required = false) String password);

    @PutMapping(value = "/approute/subroute/start", produces = "application/ld+json")
    @Operation(summary = "Updates the endpoint in the subroute")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Updated the endpoint in the subroute")})
    ResponseEntity<String> updateSubrouteStart(@RequestParam(value = "routeId") URI routeId,
                                               @RequestParam(value = "routeStepId") URI routeStepId,
                                               @RequestParam(value = "endpointId") URI endpointId,
                                               @RequestParam(value = "accessUrl", required = false) String accessUrl,
                                               @RequestParam(value = "username", required = false) String username,
                                               @RequestParam(value = "password", required = false) String password);
}
