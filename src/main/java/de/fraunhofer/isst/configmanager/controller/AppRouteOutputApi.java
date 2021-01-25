//package de.fraunhofer.isst.configmanager.controller;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import java.net.URI;
//
//public interface AppRouteOutputApi {
//
//    @GetMapping(value = "/approute/output", produces = "application/ld+json")
//    @Operation(summary = "Returns the resource from the app route output")
//    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returned the resource from the app route output")})
//    ResponseEntity<String> getResourceFromAppRouteOutput(@RequestParam(value = "routeId") URI routeId,
//                                                         @RequestParam("resourceId") URI resourceId);
//
//    @GetMapping(value = "/approute/subroute/output", produces = "application/ld+json")
//    @Operation(summary = "Returns the endpoint from the subroute")
//    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returned the endpoint from the subroute")})
//    ResponseEntity<String> getResourceFromSubrouteOutput(@RequestParam(value = "routeId") URI routeId,
//                                                         @RequestParam(value = "routeStepId") URI routeStepId,
//                                                         @RequestParam("resourceId") URI resourceId);
//
//    @DeleteMapping(value = "/approute/output", produces = "application/ld+json")
//    @Operation(summary = "Deletes the resource from the app route output")
//    @ApiResponses({@ApiResponse(responseCode = "200", description = "Deleted the resource from the app route output")})
//    ResponseEntity<String> deleteResourceFromAppRouteOutput(@RequestParam(value = "routeId") URI routeId,
//                                                            @RequestParam("resourceId") URI resourceId);
//
//    @DeleteMapping(value = "/approute/subroute/output", produces = "application/ld+json")
//    @Operation(summary = "Deletes the resource from the app route output")
//    @ApiResponses({@ApiResponse(responseCode = "200", description = "Deleted the resource from the app route output")})
//    ResponseEntity<String> deleteResourceFromSubrouteOutput(@RequestParam(value = "routeId") URI routeId,
//                                                            @RequestParam(value = "routeStepId") URI routeStepId,
//                                                            @RequestParam("resourceId") URI resourceId);
//}
