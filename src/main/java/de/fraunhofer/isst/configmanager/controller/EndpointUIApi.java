package de.fraunhofer.isst.configmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

public interface EndpointUIApi {

    // APIs to manage the generic endpoints
    @GetMapping(value = "/generic/endpoint", produces = "application/ld+json")
    @Operation(summary = "Returns the generic endpoint of an app route")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully returned the generic endpoint of an " +
            "app route")})
    ResponseEntity<String> getGenericEndpoint(@RequestParam("routeId") URI routeId,
                                              @RequestParam("endpointId") URI endpointId);

    @GetMapping(value = "/generic/endpoint/json", produces = "application/ld+json")
    @Operation(summary = "Returns the generic endpoint of an app route in JSON")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully returned the generic endpoint of " +
            "an app route in JSON")})
    ResponseEntity<String> getGenericEndpointJson(@RequestParam("routeId") URI routeId,
                                                  @RequestParam("endpointId") URI endpointId);

    @PostMapping(value = "/generic/endpoint", produces = "application/ld+json")
    @Operation(summary = "Creates a new generic endpoint for the app route")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully created the generic endpoint for the" +
            " app route")})
    ResponseEntity<String> createGenericEndpoint(@RequestParam(value = "routeId", required = false) URI routeId,
                                                 @RequestParam("accessUrl") String accessUrl,
                                                 @RequestParam("username") String username,
                                                 @RequestParam("password") String password);

    @PutMapping(value = "/generic/endpoint", produces = "application/ld+json")
    @Operation(summary = "Updates the generic endpoint in the app route")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully updated the generic endpoint in the" +
            " app route")})
    ResponseEntity<String> updateGenericEndpoint(@RequestParam("routeId") URI routeId,
                                                 @RequestParam("endpointId") URI endpointId,
                                                 @RequestParam(value = "accessUrl", required = false) String accessUrl,
                                                 @RequestParam(value = "username", required = false) String username,
                                                 @RequestParam(value = "password", required = false) String password);

    // APIs to manage the connector endpoints
    @GetMapping(value = "/connector/endpoints", produces = "application/ld+json")
    @Operation(summary = "Returns the connector endpoints")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully returned the connector endpoints")})
    ResponseEntity<String> getConnectorEndpoints();

    @GetMapping(value = "/connector/endpoint", produces = "application/ld+json")
    @Operation(summary = "Returns the connector endpoint")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully returned the connector endpoint")})
    ResponseEntity<String> getConnectorEndpoint(@RequestParam("connectorEndpointId") URI connectorEndpointId);

    @PostMapping(value = "/connector/endpoint", produces = "application/ld+json")
    @Operation(summary = "Creates a new connector endpoint for the connector")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully created the connector endpoint " +
            "for the connector")})
    ResponseEntity<String> createConnectorEndpoint(@RequestParam("accessUrl") String accessUrl);

}
