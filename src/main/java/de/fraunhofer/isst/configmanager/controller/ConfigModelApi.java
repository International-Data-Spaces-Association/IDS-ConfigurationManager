package de.fraunhofer.isst.configmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

public interface ConfigModelApi {

    @PostMapping(value = "/configmodel", produces = "application/ld+json")
    @Operation(summary = "Creates a new configuration model")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully created a new configuration model")})
    ResponseEntity<String> createConfigModel(@RequestParam("loglevel") String loglevel,
                                             @RequestParam("connectorStatus") String connectorStatus,
                                             @RequestParam("connectorDeployMode") String connectorDeployMode,
                                             @RequestParam("trustStore") String trustStore,
                                             @RequestParam("trustStorePassword") String trustStorePassword,
                                             @RequestParam("keyStore") String keyStore,
                                             @RequestParam("keyStorePassword") String keyStorePassword);

    @PutMapping(value = "/configmodel")
    @Operation(summary = "Updates the configuration model")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully updated the configuration model")})
    ResponseEntity<String> updateConfigModel(@RequestParam(value = "loglevel", required = false) String loglevel,
                                             @RequestParam(value = "connectorStatus", required = false) String connectorStatus,
                                             @RequestParam(value = "connectorDeployMode", required = false) String connectorDeployMode,
                                             @RequestParam(value = "trustStore", required = false) String trustStore,
                                             @RequestParam(value = "trustStorePassword", required = false) String trustStorePassword,
                                             @RequestParam(value = "keyStore", required = false) String keyStore,
                                             @RequestParam(value = "keyStorePassword", required = false) String keyStorePassword,
                                             @RequestParam(value = "proxyUri", required = false) String proxyUri,
                                             @RequestParam(value = "noProxyUri", required = false) ArrayList<URI> noProxyUriList,
                                             @RequestParam(value = "username", required = false) String username,
                                             @RequestParam(value = "password", required = false) String password);


    @GetMapping(value = "/configmodel", produces = "application/ld+json")
    @Operation(summary = "Get the  configuration model")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Succesfully get the configuration model")})
    ResponseEntity<String> getConfigModel();

    @GetMapping(value = "/configmodel/json", produces = "application/ld+json")
    @Operation(summary = "Get the configuration model in json")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Succesfully get the configuration model in json")})
    ResponseEntity<String> getConfigModelJson();

    @DeleteMapping(value = "/configmodel")
    @Operation(summary = "Deletes the configuration model")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully deleted the configuration model")})
    ResponseEntity<String> deleteConfigModel(@RequestParam("configmodelId") URI configmodelId);
}
