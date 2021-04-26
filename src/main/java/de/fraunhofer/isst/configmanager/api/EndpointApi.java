package de.fraunhofer.isst.configmanager.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @ApiResponse(responseCode = "200", description = "Created a generic endpoint")
    @ApiResponse(responseCode = "400", description = "Can not create the generic endpoint")
    ResponseEntity<String> createGenericEndpoint(@RequestParam(value = "accessURL") URI accessURL,
                                                 @RequestParam(value = "sourceType") String sourceType,
                                                 @RequestParam(value = "username", required = false) String username,
                                                 @RequestParam(value = "password", required = false) String password);

    @GetMapping(value = "/generic/endpoints", produces = "application/ld+json")
    @Operation(summary = "Returns a list of generic endpoints")
    @ApiResponse(responseCode = "200", description = "Returned a list of generic endpoints")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    ResponseEntity<String> getGenericEndpoints();

    @DeleteMapping(value = "/generic/endpoint", produces = "application/ld+json")
    @Operation(summary = "Deletes a generic endpoint")
    @ApiResponse(responseCode = "200", description = "Deleted a generic endpoint")
    @ApiResponse(responseCode = "400", description = "Can not delete the generic endpoint")
    ResponseEntity<String> deleteGenericEndpoint(@RequestParam(value = "endpointId") URI endpointId);

    @PutMapping(value = "/generic/endpoint", produces = "application/ld+json")
    @Operation(summary = "Updates a generic endpoint")
    @ApiResponse(responseCode = "200", description = "Updated a generic endpoint")
    @ApiResponse(responseCode = "400", description = "Can not update the generic endpoint")
    ResponseEntity<String> updateGenericEndpoint(@RequestParam(value = "id") URI id,
                                                 @RequestParam(value = "accessURL", required = false) URI accessURL,
                                                 @RequestParam(value = "sourceType", required = false) String sourceType,
                                                 @RequestParam(value = "username", required = false) String username,
                                                 @RequestParam(value = "password", required = false) String password);

    @PostMapping(value = "/connector/endpoint", produces = "application/ld+json")
    @Operation(summary = "Creates a new connector endpoint for the connector")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully created the connector endpoint for the connector")})
    ResponseEntity<String> createConnectorEndpoint(@RequestParam("accessUrl") URI accessUrl, @RequestParam(value = "sourceType", required = false) String sourceType);
}
