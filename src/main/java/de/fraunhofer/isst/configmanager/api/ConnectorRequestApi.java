package de.fraunhofer.isst.configmanager.api;

import de.fraunhofer.isst.configmanager.model.config.QueryInput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.util.UUID;

public interface ConnectorRequestApi {
    @PostMapping(value = "/request/description")
    @Operation(summary = "Request metadata from another IDS connector.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully requested  metadata from IDS connector"),
            @ApiResponse(responseCode = "400", description = "Can not request metadata from IDS connector"),
            @ApiResponse(responseCode = "500", description = "Internal server error")})
    ResponseEntity<String> requestMetadata(@RequestParam("recipientId") URI recipientId,
                                           @RequestParam(value = "requestedResourceId", required = false) URI requestedResourceId);

    @PostMapping(value = "/request/contract", produces = "application/ld+json")
    @Operation(summary = "Send a contract request to another IDS connector.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully send a contract request to another IDS connector"),
            @ApiResponse(responseCode = "400", description = "Can not return the contract agreement id")})
    ResponseEntity<String> requestContract(@RequestParam("recipientId") URI recipientId,
                                           @RequestParam(value = "requestedArtifactId ") URI requestedArtifactId,
                                           @RequestBody(required = false) String contractOffer);

    @PostMapping(value = "/request/artifact")
    @Operation(summary = "Request data from another IDS connector.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully requested data from another IDS connector.")})
    ResponseEntity<String> requestData(@RequestParam("recipientId") URI recipientId,
                                       @RequestParam(value = "requestedArtifactId ") URI requestedArtifactId,
                                       @RequestParam(value = "contractId", required = false) URI contractId,
                                       @RequestParam(value = "key") UUID key,
                                       @RequestBody(required = false) QueryInput queryInput);
}
