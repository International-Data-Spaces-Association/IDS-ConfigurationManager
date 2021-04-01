package de.fraunhofer.isst.configmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface ConnectorUIApi {

    @GetMapping(value = "/connector", produces = "application/ld+json")
    @Operation(summary = "Get the Connector-Description")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully retrieved the connector")})
    ResponseEntity<String> getConnector();

    @GetMapping(value = "/connector/status", produces = "application/ld+json")
    @Operation(summary = "Get the accessibility-status of the Public Connector Endpoint (Connector Self-description)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Public connector endpoint reachable."),
            @ApiResponse(responseCode = "503", description = "Public connector endpoint not reachable.")})
    ResponseEntity<String> getConnectorStatus();

    @GetMapping(value = "/connector/json", produces = "application/ld+json")
    @Operation(summary = "Get the connector in json")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully retrieved the connector in json")})
    ResponseEntity<String> getConnectorJson();

    @PostMapping(value = "/connector", produces = "application/ld+json")
    @Operation(summary = "Create a new connector")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully created a new connector")})
    ResponseEntity<String> createConnector(@RequestParam("title") String title,
                                           @RequestParam("description") String description,
                                           @RequestParam("endpointAccessURL") String endpointAccessURL,
                                           @RequestParam("version") String version,
                                           @RequestParam("curator") String curator,
                                           @RequestParam("maintainer") String maintainer,
                                           @RequestParam("inboundModelVersion") String inboundModelVersion,
                                           @RequestParam("outboundModelVersion") String outboundModelVersion);

    @PutMapping(value = "/connector", produces = "application/ld+json")
    @Operation(summary = "Update a connector")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully updated a new connector")})
    ResponseEntity<String> updateConnector(@RequestParam(value = "title", required = false) String title,
                                           @RequestParam(value = "description", required = false) String description,
                                           @RequestParam(value = "endpoint", required = false) String endpoint,
                                           @RequestParam(value = "version", required = false) String version,
                                           @RequestParam(value = "curator", required = false) String curator,
                                           @RequestParam(value = "maintainer", required = false) String maintainer,
                                           @RequestParam(value = "inboundModelVersion", required = false) String inboundModelVersion,
                                           @RequestParam(value = "outboundModelVersion", required = false) String outboundModelVersion);

    @DeleteMapping(value = "/connector")
    @Operation(summary = "Deletes the connector")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully deleted the connector")})
    ResponseEntity<String> deleteConnector();
}
