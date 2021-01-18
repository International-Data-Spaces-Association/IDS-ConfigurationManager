package de.fraunhofer.isst.configmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

public interface AppRouteSubrouteApi {

    @PostMapping(value = "/approute/subroute", produces = "application/ld+json")
    @Operation(summary = "Creates a new subroute")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Created a new route subroute")})
    ResponseEntity<String> createSubroute(@RequestParam(value = "routeId") URI routeId,
                                          @RequestParam(value = "routeDeployMethod") String routeDeployMethod);

    @PutMapping(value = "/approute/subroute", produces = "application/ld+json")
    @Operation(summary = "Updates the given subroute")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Updated the given subroute")})
    ResponseEntity<String> updateSubroute(@RequestParam(value = "routeId") URI routeId,
                                          @RequestParam(value = "routeStepId") URI routeStepId,
                                          @RequestParam(value = "routeDeployMethod", required = false) String routeDeployMethod);

    @GetMapping(value = "/approute/subroute", produces = "application/ld+json")
    @Operation(summary = "Returns the specific subroute")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returns the specific subroute")})
    ResponseEntity<String> getSubroute(@RequestParam(value = "routeId") URI routeId,
                                       @RequestParam(value = "routeStepId") URI routeStepId);

    @DeleteMapping(value = "/approute/subroute", produces = "application/ld+json")
    @Operation(summary = "Updates the given subroute")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Updated the given subroute")})
    ResponseEntity<String> deleteSubroute(@RequestParam(value = "routeId") URI routeId,
                                          @RequestParam(value = "routeStepId") URI routeStepId);
}
