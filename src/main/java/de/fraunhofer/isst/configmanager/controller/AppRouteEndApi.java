//package de.fraunhofer.isst.configmanager.controller;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.net.URI;
//
//public interface AppRouteEndApi {
//
//    @PostMapping(value = "/approute/end", produces = "application/ld+json")
//    @Operation(summary = "Creates a new route start")
//    @ApiResponses({@ApiResponse(responseCode = "200", description = "Created an endpoint for the app route")})
//    ResponseEntity<String> createAppRouteEnd(@RequestParam(value = "routeId") URI routeId,
//                                             @RequestParam(value = "accessUrl") String accessUrl);
//
//    @PostMapping(value = "/approute/subroute/end", produces = "application/ld+json")
//    @Operation(summary = "Creates a new route end for the subroute")
//    @ApiResponses({@ApiResponse(responseCode = "200", description = "Created a new route end for the subroute")})
//    ResponseEntity<String> createSubrouteEnd(@RequestParam(value = "routeId") URI routeId,
//                                             @RequestParam(value = "routeStepId", required = false) URI routeStepId,
//                                             @RequestParam(value = "accessUrl") String accessUrl);
//
//    @GetMapping(value = "/approute/end", produces = "application/ld+json")
//    @Operation(summary = "Returns the endpoint from the app route")
//    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returned the endpoint from the app route")})
//    ResponseEntity<String> getAppRouteEnd(@RequestParam(value = "routeId") URI routeId,
//                                          @RequestParam("endpointId") URI endpointId);
//
//    @GetMapping(value = "/approute/subroute/end", produces = "application/ld+json")
//    @Operation(summary = "Returns the endpoint from the subroute")
//    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returned the endpoint from the subroute")})
//    ResponseEntity<String> getSubrouteEnd(@RequestParam(value = "routeId") URI routeId,
//                                          @RequestParam(value = "routeStepId") URI routeStepId,
//                                          @RequestParam("endpointId") URI endpointId);
//
//    @DeleteMapping(value = "/approute/end", produces = "application/ld+json")
//    @Operation(summary = "Deletes the endpoint from the app route")
//    @ApiResponses({@ApiResponse(responseCode = "200", description = "Deleted the endpoint from the app route")})
//    ResponseEntity<String> deleteAppRouteEnd(@RequestParam(value = "routeId") URI routeId,
//                                             @RequestParam("endpointId") URI endpointId);
//
//    @DeleteMapping(value = "/approute/subroute/end", produces = "application/ld+json")
//    @Operation(summary = "Deletes the endpoint from the subroute")
//    @ApiResponses({@ApiResponse(responseCode = "200", description = "Deleted the endpoint from the subroute")})
//    ResponseEntity<String> deleteSubrouteEnd(@RequestParam(value = "routeId") URI routeId,
//                                             @RequestParam(value = "routeStepId") URI routeStepId,
//                                             @RequestParam("endpointId") URI endpointId);
//
//    @PutMapping(value = "/approute/end", produces = "application/ld+json")
//    @Operation(summary = "Updates the endpoint in the app route")
//    @ApiResponses({@ApiResponse(responseCode = "200", description = "Updated the endpoint in the app route")})
//    ResponseEntity<String> updateAppRouteEnd(@RequestParam(value = "routeId") URI routeId,
//                                             @RequestParam(value = "endpointId") URI endpointId,
//                                             @RequestParam(value = "accessUrl") String accessUrl);
//
//    @PutMapping(value = "/approute/subroute/end", produces = "application/ld+json")
//    @Operation(summary = "Updates the endpoint in the subroute")
//    @ApiResponses({@ApiResponse(responseCode = "200", description = "Updated the endpoint in the subroute")})
//    ResponseEntity<String> updateSubrouteEnd(@RequestParam(value = "routeId") URI routeId,
//                                             @RequestParam(value = "routeStepId") URI routeStepId,
//                                             @RequestParam(value = "endpointId") URI endpointId,
//                                             @RequestParam(value = "accessUrl") String accessUrl);
//}
