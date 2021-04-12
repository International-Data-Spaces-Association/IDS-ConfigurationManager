package de.fraunhofer.isst.configmanager.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

public interface EndpointApi {
    @PostMapping(value = "/generic/endpoint", produces = "application/ld+json")
    @Operation(summary = "Creates a generic endpoint")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Created a generic endpoint"),
            @ApiResponse(responseCode = "400", description = "Can not create the generic endpoint")})
    ResponseEntity<String> createGenericEndpoint(@RequestParam(value = "accessURL") String accessURL,
                                                 @RequestParam(value = "username", required = false) String username,
                                                 @RequestParam(value = "password", required = false) String password);

    @GetMapping(value = "/generic/endpoints", produces = "application/ld+json")
    @Operation(summary = "Returns a list of generic endpoints")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Returned a list of generic endpoints"),
            @ApiResponse(responseCode = "500", description = "Internal server error")})
    ResponseEntity<String> getGenericEndpoints();

    @GetMapping(value = "/generic/endpoint", produces = "application/ld+json")
    @Operation(summary = "Returns a specific generic endpoint")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Returned a specific backend connection"),
            @ApiResponse(responseCode = "400", description = "Can not find the generic endpoint"),
            @ApiResponse(responseCode = "500", description = "Internal server error")})
    ResponseEntity<String> getGenericEndpoint(@RequestParam(value = "endpointId") URI endpointId);

    @DeleteMapping(value = "/generic/endpoint", produces = "application/ld+json")
    @Operation(summary = "Deletes a generic endpoint")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deleted a generic endpoint"),
            @ApiResponse(responseCode = "400", description = "Can not delete the generic endpoint")})
    ResponseEntity<String> deleteGenericEndpoint(@RequestParam(value = "endpointId") URI endpointId);

    @PutMapping(value = "/generic/endpoint", produces = "application/ld+json")
    @Operation(summary = "Updates a generic endpoint")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated a generic endpoint"),
            @ApiResponse(responseCode = "400", description = "Can not update the generic endpoint")})
    ResponseEntity<String> updateGenericEndpoint(@RequestParam(value = "id") URI id,
                                                 @RequestParam(value = "accessURL", required = false) String accessURL,
                                                 @RequestParam(value = "username", required = false) String username,
                                                 @RequestParam(value = "password", required = false) String password);

    @GetMapping(value = "/connector/endpoints", produces = "application/ld+json")
    @Operation(summary = "Returns the connector endpoints")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully returned the connector endpoints"),
            @ApiResponse(responseCode = "400", description = "Can not find the connector endpoints"),
            @ApiResponse(responseCode = "500", description = "Internal server error")})
    ResponseEntity<String> getConnectorEndpoints();

    @GetMapping(value = "/connector/endpoints/client", produces = "application/ld+json")
    @Operation(summary = "Returns a list of connector endpoints")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully returned a list of connector endpoints"),
            @ApiResponse(responseCode = "400", description = "Can not find the connector endpoints from the client")})
    ResponseEntity<String> getConnectorEndpointsFromClient(@RequestParam("accessUrl") String accessUrl,
                                                           @RequestParam(value = "resourceId", required = false) String resourceId);

    @GetMapping(value = "/connector/endpoint", produces = "application/ld+json")
    @Operation(summary = "Returns the connector endpoint")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully returned the connector endpoint"),
            @ApiResponse(responseCode = "400", description = "Can not find the connector endpoint"),
            @ApiResponse(responseCode = "500", description = "Internal server error")})
    ResponseEntity<String> getConnectorEndpoint(@RequestParam("connectorEndpointId") URI connectorEndpointId);

    @PostMapping(value = "/connector/endpoint", produces = "application/ld+json")
    @Operation(summary = "Creates a new connector endpoint for the connector")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully created the connector endpoint for the connector")})
    ResponseEntity<String> createConnectorEndpoint(@RequestParam("accessUrl") String accessUrl);
}
