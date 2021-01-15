package de.fraunhofer.isst.configmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

public interface EndpointUIApi {

    @GetMapping(value = "/approute/endpoint", produces = "application/ld+json")
    @Operation(summary = "Returns the endpoints of the app route")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully returned the endpoint of the" +
            " app route")})
    ResponseEntity<String> getAppRouteEndpoint(@RequestParam("routeId") URI routeId,
                                               @RequestParam("endpointId") URI endpointId);

    @GetMapping(value = "/approute/endpoint/json", produces = "application/ld+json")
    @Operation(summary = "Returns the endpoints of the app route in json")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully returned the endpoint of the" +
            " app route in json")})
    ResponseEntity<String> getAppRouteEndpointInJson(@RequestParam("routeId") URI routeId,
                                                     @RequestParam("endpointId") URI endpointId);

    @PostMapping(value = "/approute/endpoint", produces = "application/ld+json")
    @Operation(summary = "Creates a new endpoint for the app route")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully created the endpoint for the" +
            " app route")})
    ResponseEntity<String> createAppRouteEndpoint(@RequestParam(value = "routeId", required = false) URI routeId,
                                                  @RequestParam("accessUrl") String accessUrl,
                                                  @RequestParam("username") String username,
                                                  @RequestParam("password") String password);

    @PutMapping(value = "/approute/endpoint", produces = "application/ld+json")
    @Operation(summary = "Updates the endpoint in the app route")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully updated the endpoint in the" +
            " app route")})
    ResponseEntity<String> updateAppRouteEndpoint(@RequestParam("routeId") URI routeId,
                                                  @RequestParam("endpointId") URI endpointId,
                                                  @RequestParam(value = "accessUrl", required = false) String accessUrl,
                                                  @RequestParam(value = "username", required = false) String username,
                                                  @RequestParam(value = "password", required = false) String password);

    @DeleteMapping(value = "/approute/endpoint", produces = "application/ld+json")
    @Operation(summary = "Deletes the endpoint in the app route")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully deleted the endpoint in the" +
            " app route")})
    ResponseEntity<String> deleteAppRouteEndpoint(@RequestParam("routeId") URI routeId,
                                                  @RequestParam(value = "appRouteEndId", required = false) URI appRouteEndId,
                                                  @RequestParam(value = "appRouteStartId", required = false) URI appRouteStartId,
                                                  @RequestParam(value = "appRouteOutputId", required = false) URI appRouteOutputId,
                                                  @RequestParam(value = "appRouteBrokerId", required = false) URI appRouteBrokerId);

    @DeleteMapping(value = "/approute", produces = "application/ld+json")
    @Operation(summary = "Deletes the given approute (if it is empty)")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully deleted the approute")})
    ResponseEntity<String> deleteAppRoute(@RequestParam("routeId") URI routeId);
}
