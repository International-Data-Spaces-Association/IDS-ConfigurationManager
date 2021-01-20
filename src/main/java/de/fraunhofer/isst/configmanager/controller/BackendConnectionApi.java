package de.fraunhofer.isst.configmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

public interface BackendConnectionApi {

    @PostMapping(value = "/backend/connection", produces = "application/ld+json")
    @Operation(summary = "Creates a backend connection")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Created a backend connection")})
    ResponseEntity<String> createBackendConnection(@RequestParam(value = "accessURL") String accessURL,
                                                   @RequestParam(value = "username", required = false) String username,
                                                   @RequestParam(value = "password", required = false) String password);

    @GetMapping(value = "/backend/connections", produces = "application/ld+json")
    @Operation(summary = "Returns a list of backend connection")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returned a list of backend connections")})
    ResponseEntity<String> getBackendConnections();

    @GetMapping(value = "/backend/connection", produces = "application/ld+json")
    @Operation(summary = "Returns a specific backend connection")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returned a specific backend connection")})
    ResponseEntity<String> getBackendConnection(@RequestParam(value = "id") URI id);

    @DeleteMapping(value = "/backend/connection", produces = "application/ld+json")
    @Operation(summary = "Deletes a backend connection")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Deleted a backend connection")})
    ResponseEntity<String> deleteBackendConnection(@RequestParam(value = "id") URI id);

    @PutMapping(value = "/backend/connection", produces = "application/ld+json")
    @Operation(summary = "Updates a backend connection")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Updated a backend connection")})
    ResponseEntity<String> updateBackendConnection(@RequestParam(value = "id") URI id,
                                                   @RequestParam(value = "accessURL", required = false) String accessURL,
                                                   @RequestParam(value = "username", required = false) String username,
                                                   @RequestParam(value = "password", required = false) String password);


}
