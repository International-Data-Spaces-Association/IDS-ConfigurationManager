package de.fraunhofer.isst.configmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

public interface AppUIApi {

    @PostMapping(value = "/app", produces = "application/ld+json")
    @Operation(summary = "Create a new app")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Created a new app")})
    ResponseEntity<String> createApp(@RequestParam(value = "appUri") URI appUri,
                                     @RequestParam(value = "title") String title);

    @PutMapping(value = "/app", produces = "application/ld+json")
    @Operation(summary = "Update the app")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Updated the app")})
    ResponseEntity<String> updateApp(@RequestParam(value = "appUri") URI appUri,
                                     @RequestParam(value = "title", required = false) String title);

    @GetMapping(value = "/apps", produces = "application/ld+json")
    @Operation(summary = "Returns a list of all apps")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returned a list of all apps")})
    ResponseEntity<String> getApps();

    @GetMapping(value = "/app", produces = "application/ld+json")
    @Operation(summary = "Return an app")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returned an app")})
    ResponseEntity<String> getApp(@RequestParam(value = "appUri") URI appUri);

    @DeleteMapping(value = "/app", produces = "application/ld+json")
    @Operation(summary = "Delete an specific app")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Deleted the specific app")})
    ResponseEntity<String> deleteApp(@RequestParam(value = "appUri") URI appUri);

}
