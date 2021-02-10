package de.fraunhofer.isst.configmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

public interface BrokerUIApi {

    @PostMapping(value = "/broker", produces = "application/ld+json")
    @Operation(summary = "Creates a new broker")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Created a new broker")})
    ResponseEntity<String> createBroker(@RequestParam(value = "brokerUri") URI brokerUri,
                                        @RequestParam(value = "title", required = false) String title);

    @PutMapping(value = "/broker", produces = "application/ld+json")
    @Operation(summary = "Updates a broker")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Updated the broker")})
    ResponseEntity<String> updateBroker(@RequestParam(value = "brokerUri") URI brokerUri,
                                        @RequestParam(value = "title", required = false) String title);

    @DeleteMapping(value = "/broker", produces = "application/ld+json")
    @Operation(summary = "Deletes a broker")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Deleted the broker")})
    ResponseEntity<String> deleteBroker(@RequestParam(value = "brokerUri") URI brokerUri);

    @GetMapping(value = "/brokers", produces = "application/ld+json")
    @Operation(summary = "Returns the list of all brokers")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully returned the list of all brokers")})
    ResponseEntity<String> getAllBrokers();

    @GetMapping(value = "/broker/list", produces = "application/ld+json")
    @Operation(summary = "Returns a list of all broker uri's")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully returned a list of all broker uris's")})
    ResponseEntity<String> getAllBrokerUris();

    @GetMapping(value = "/broker", produces = "application/ld+json")
    @Operation(summary = "Returns the specific broker")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully returned the specific broker")})
    ResponseEntity<String> getBroker(@RequestParam(value = "brokerUri") URI brokerUri);

    @PostMapping(value = "/broker/register", produces = "application/ld+json")
    @Operation(summary = "Registers the connector with the broker")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully registered the connector with the broker")})
    ResponseEntity<String> registerConnector(@RequestParam(value = "brokerUri") URI brokerUri);

    @PostMapping(value = "/broker/unregister", produces = "application/ld+json")
    @Operation(summary = "Unregisters the connector with the broker")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully unregistered the connector with the broker")})
    ResponseEntity<String> unregisterConnector(@RequestParam(value = "brokerUri") URI brokerUri);

    @PostMapping(value = "/broker/update", produces = "application/ld+json")
    @Operation(summary = "Updates the self description at the broker")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully updated the self description at the broker")})
    ResponseEntity<String> updateConnector(@RequestParam(value = "brokerUri") URI brokerUri);

    @PostMapping(value = "/broker/update/resource", produces = "application/ld+json")
    @Operation(summary = "Updates a resource at the broker")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully updated the resource at the broker")})
    ResponseEntity<String> updateResourceAtBroker(@RequestParam(value = "brokerUri") URI brokerUri,
                                                  @RequestParam("resourceId") URI resourceId);

    @PostMapping(value = "/broker/delete/resource", produces = "application/ld+json")
    @Operation(summary = "Deletes a resource at the broker")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully deleted the resource at the broker")})
    ResponseEntity<String> deleteResourceAtBroker(@RequestParam(value = "brokerUri") URI brokerUri,
                                                  @RequestParam("resourceId") URI resourceId);

}
