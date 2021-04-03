package de.fraunhofer.isst.configmanager.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

public interface ResourceContractApi {
    @GetMapping(value = "/resource/contract", produces = "application/ld+json")
    @Operation(summary = "Returns the contract from a resource")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully returned the contract from the resource")})
    ResponseEntity<String> getResourceContract(@RequestParam("resourceId") URI resourceId);

    @PutMapping(value = "/resource/contract", produces = "application/ld+json")
    @Operation(summary = "Updates the contract in a resource")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully updated the contract in the resource")})
    ResponseEntity<String> updateResourceContract(@RequestParam("resourceId") URI resourceId,
                                                  @RequestBody String contractJson);
}
