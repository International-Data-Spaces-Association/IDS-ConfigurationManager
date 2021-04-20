package de.fraunhofer.isst.configmanager.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

public interface ConnectorApi {
    @GetMapping(value = "/connector", produces = "application/ld+json")
    @Operation(summary = "Get the Connector-Description")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the connector"),
            @ApiResponse(responseCode = "404", description = "Can not find a connector description"),
            @ApiResponse(responseCode = "500", description = "Internal server error")})
    ResponseEntity<String> getConnector();

    @GetMapping(value = "/connector/status", produces = "application/ld+json")
    @Operation(summary = "Get the accessibility-status of the Public Connector Endpoint (Connector Self-description)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Public connector endpoint reachable."),
            @ApiResponse(responseCode = "503", description = "Public connector endpoint not reachable.")})
    ResponseEntity<String> getConnectorStatus();

    @PutMapping(value = "/connector", produces = "application/ld+json")
    @Operation(summary = "Update a connector")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully updated the connector description of the configuration model"),
            @ApiResponse(responseCode = "400", description = "Failed to update the connector. The configuration model is not valid")})
    ResponseEntity<String> updateConnector(@RequestParam(value = "title", required = false) String title,
                                           @RequestParam(value = "description", required = false) String description,
                                           @RequestParam(value = "endpoint", required = false) URI endpoint,
                                           @RequestParam(value = "version", required = false) String version,
                                           @RequestParam(value = "curator", required = false) URI curator,
                                           @RequestParam(value = "maintainer", required = false) URI maintainer,
                                           @RequestParam(value = "inboundModelVersion", required = false) String inboundModelVersion,
                                           @RequestParam(value = "outboundModelVersion", required = false) String outboundModelVersion);
}
