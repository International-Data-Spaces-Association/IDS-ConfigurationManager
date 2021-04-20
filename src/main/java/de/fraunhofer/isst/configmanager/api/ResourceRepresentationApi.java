package de.fraunhofer.isst.configmanager.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

public interface ResourceRepresentationApi {
    @PostMapping(value = "/resource/representation", produces = "application/ld+json")
    @Operation(summary = "Creates a representation for a resource")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully created a representation for a resource"),
            @ApiResponse(responseCode = "400", description = "Can not create the resource representation"),
            @ApiResponse(responseCode = "404", description = "Can not find the resource to create the representation")})
    ResponseEntity<String> createResourceRepresentation(@RequestParam("resourceId") URI resourceId,
                                                        @RequestParam("endpointId") URI endpointId,
                                                        @RequestParam("language") String language,
                                                        @RequestParam("filenameExtension") String filenameExtension,
                                                        @RequestParam("bytesize") Long bytesize);

    @PutMapping(value = "/resource/representation", produces = "application/ld+json")
    @Operation(summary = "Updates the representation for a resource")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully updated the representation for a resource"),
            @ApiResponse(responseCode = "400", description = "Can not update the resource representation"),
            @ApiResponse(responseCode = "404", description = "Can not find the resource to update the representation")})
    ResponseEntity<String> updateResourceRepresentation(@RequestParam("resourceId") URI resourceId,
                                                        @RequestParam(value = "representationId") URI representationId,
                                                        @RequestParam(value = "endpointId") URI endpointId,
                                                        @RequestParam(value = "language", required = false) String language,
                                                        @RequestParam(value = "filenameExtension", required = false) String filenameExtension,
                                                        @RequestParam(value = "bytesize", required = false) Long bytesize);
}
