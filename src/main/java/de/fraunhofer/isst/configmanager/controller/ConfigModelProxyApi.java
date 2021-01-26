package de.fraunhofer.isst.configmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.util.ArrayList;

public interface ConfigModelProxyApi {

    @PutMapping(value = "/configmodel/proxy", produces = "application/ld+json")
    @Operation(summary = "Updates a the proxy at the configuration model")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully updated the proxy at the " +
            "configuration model")})
    ResponseEntity<String> updateConfigModelProxy(@RequestParam(value = "proxyUri", required = false) String proxyUri,
                                                  @RequestParam(value = "noProxyUri", required = false) ArrayList<URI> noProxyUriList,
                                                  @RequestParam(value = "username", required = false) String username,
                                                  @RequestParam(value = "password", required = false) String password);

    @GetMapping(value = "/configmodel/proxy", produces = "application/ld+json")
    @Operation(summary = "Returns the proxy from the configuration model")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully returned the proxy from the " +
            "configuration model")})
    ResponseEntity<String> getConfigModelProxy();

    @GetMapping(value = "/configmodel/proxy/json", produces = "application/ld+json")
    @Operation(summary = "Returns the proxy from the configuration model in JSON format")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully returned the proxy from the " +
            "configuration model in JSON format")})
    ResponseEntity<String> getConfigModelProxyJson();

    @DeleteMapping(value = "/configmodel/proxy", produces = "application/ld+json")
    @Operation(summary = "Deletes the proxy from the configuration model")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully deleted the proxy from the" +
            " configuration model")})
    ResponseEntity<String> deleteConfigModelProxy(@RequestParam("proxyId") URI proxyId);

}
