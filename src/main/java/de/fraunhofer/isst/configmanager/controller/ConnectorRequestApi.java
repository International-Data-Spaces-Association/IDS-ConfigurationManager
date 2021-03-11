package de.fraunhofer.isst.configmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

public interface ConnectorRequestApi {

    // Endpoints for invoking external connector requests
    @PostMapping(value = "/request/description", produces = "application/ld+json")
    @Operation(summary = "Request metadata from another IDS connector.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully requested  metadata from IDS connector")})
    ResponseEntity<String> requestMetadata(@RequestParam("recipientId") URI recipientId,
                                           @RequestParam(value = "reqResourceId", required = false) URI reqResourceId);
}
