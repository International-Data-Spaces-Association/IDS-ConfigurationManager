package de.fraunhofer.isst.configmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


public interface AppRouteApi {

    @PostMapping(value = "/approute", produces = "application/ld+json")
    @Operation(summary = "Create a new app route")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Created a new app route")})
    ResponseEntity<String> createAppRoute(@RequestParam(value = "routeDeployMethod") String routeDeployMethod);

    @PutMapping(value = "/approute", produces = "application/ld+json")
    @Operation(summary = "Updates the given app route")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Created a new app route")})
    ResponseEntity<String> updateAppRoute(@RequestParam("routeId") URI routeId,
                                          @RequestParam(value = "routeDeployMethod", required = false) String routeDeployMethod);

    @GetMapping(value = "/approute", produces = "application/ld+json")
    @Operation(summary = "Returns the given app route")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully get the app route")})
    ResponseEntity<String> getAppRoute(@RequestParam("routeId") URI routeId);

    @GetMapping(value = "/approutes", produces = "application/ld+json")
    @Operation(summary = "Returns all app routes")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully get all app routes")})
    ResponseEntity<String> getAppRoutes();

    @DeleteMapping(value = "/approute", produces = "application/ld+json")
    @Operation(summary = "Deletes the given app route")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully deleted the app route")})
    ResponseEntity<String> deleteAppRoute(@RequestParam("routeId") URI routeId);
}
