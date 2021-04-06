package de.fraunhofer.isst.configmanager.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;

public interface ResourceApi {
    @GetMapping(value = "/resource", produces = "application/ld+json")
    @Operation(summary = "Returns the specific resource from the connector")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully returned the specifc resource from the connector")})
    ResponseEntity<String> getResource(@RequestParam(value = "resourceId") URI resourceId);

    @GetMapping(value = "/resources", produces = "application/ld+json")
    @Operation(summary = "Returns all resources from the connector")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully returned all resources from the connector")})
    ResponseEntity<String> getResources();

    @GetMapping(value = "/resources/requested", produces = "application/ld+json")
    @Operation(summary = "Returns all requested resources from the connector")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully returned all requested resources from the connector")})
    ResponseEntity<String> getRequestedResources();

    @GetMapping(value = "/resource/json", produces = "application/ld+json")
    @Operation(summary = "Returns the specific resource from the connector in JSON format")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully returned the specific resource "
            + "from the connector in JSON format")})
    ResponseEntity<String> getResourceInJson(@RequestParam(value = "resourceId") URI resourceId);

    @PostMapping(value = "/resource", produces = "application/ld+json")
    @Operation(summary = "Creates a resource for the connector")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully created a resource for the connector")})
    ResponseEntity<String> createResource(@RequestParam("title") String title,
                                          @RequestParam("description") String description,
                                          @RequestParam("language") String language,
                                          @RequestParam("keyword") ArrayList<String> keywords,
                                          @RequestParam("version") String version,
                                          @RequestParam("standardlicense") String standardlicense,
                                          @RequestParam("publisher") String publisher);

    @PutMapping(value = "/resource", produces = "application/ld+json")
    @Operation(summary = "Updates the specific resource at the connector")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully updated the specific resource at the connector")})
    ResponseEntity<String> updateResource(@RequestParam("resourceId") URI resourceId,
                                          @RequestParam(value = "title", required = false) String title,
                                          @RequestParam(value = "description", required = false) String description,
                                          @RequestParam(value = "language", required = false) String language,
                                          @RequestParam(value = "keyword", required = false) ArrayList<String> keywords,
                                          @RequestParam(value = "version", required = false) String version,
                                          @RequestParam(value = "standardlicense", required = false) String standardlicense,
                                          @RequestParam(value = "publisher", required = false) String publisher);

    @DeleteMapping(value = "/resource")
    @Operation(summary = "Deletes the specific resource from the connector")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully deleted the specific resource from the connector")})
    ResponseEntity<String> deleteResource(@RequestParam(value = "resourceId") URI resourceId);
}
