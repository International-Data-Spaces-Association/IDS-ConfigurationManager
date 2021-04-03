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

public interface ResourceRepresentationApi {
    @PostMapping(value = "/resource/representation", produces = "application/ld+json")
    @Operation(summary = "Creates a representation for a resource")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully created a representation for a resource")})
    ResponseEntity<String> createResourceRepresentation(@RequestParam("resourceId") URI resourceId,
                                                        @RequestParam("endpointId") URI endpointId,
                                                        @RequestParam("language") String language,
                                                        @RequestParam("filenameExtension") String filenameExtension,
                                                        @RequestParam("bytesize") Long bytesize,
                                                        @RequestParam("sourceType") String sourceType);

    @PutMapping(value = "/resource/representation", produces = "application/ld+json")
    @Operation(summary = "Updates the representation for a resource")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully updated the representation for a resource")})
    ResponseEntity<String> updateResourceRepresentation(@RequestParam("resourceId") URI resourceId,
                                                        @RequestParam(value = "representationId") URI representationId,
                                                        @RequestParam(value = "endpointId") URI endpointId,
                                                        @RequestParam(value = "language", required = false) String language,
                                                        @RequestParam(value = "filenameExtension", required = false) String filenameExtension,
                                                        @RequestParam(value = "bytesize", required = false) Long bytesize,
                                                        @RequestParam(value = "sourceType", required = false) String sourceType);

    @GetMapping(value = "/resource/representation", produces = "application/ld+json")
    @Operation(summary = "Get the representation for a resource")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully get the representation for a resource")})
    ResponseEntity<String> getResourceRepresentation(@RequestParam("representationId") URI representationId);

    @GetMapping(value = "/resource/representation/json", produces = "application/ld+json")
    @Operation(summary = "Get the representation for a resource in JSON format")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully get the representation for a resource in JSON format")})
    ResponseEntity<String> getResourceRepresentationInJson(@RequestParam("resourceId") URI resourceId,
                                                           @RequestParam("representationId") URI representationId);

    @DeleteMapping(value = "/resource/representation", produces = "application/ld+json")
    @Operation(summary = "Deletes the representation for a resource")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully deleted the representation for a resource")})
    ResponseEntity<String> deleteResourceRepresentation(@RequestParam("resourceId") URI resourceId,
                                                        @RequestParam("representationId") URI representationId);
}
