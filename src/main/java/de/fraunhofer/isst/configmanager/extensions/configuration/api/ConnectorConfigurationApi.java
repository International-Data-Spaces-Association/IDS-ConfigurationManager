/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.isst.configmanager.extensions.configuration.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.util.ArrayList;

public interface ConnectorConfigurationApi {
    @PutMapping(value = "/configmodel")
    @Operation(summary = "Updates the configuration model")
    @ApiResponse(responseCode = "200", description = "Successfully updated the configuration model at the client")
    @ApiResponse(responseCode = "400", description = "Can not update the configuration model at the client")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    ResponseEntity<String> updateConfigModel(@RequestParam(value = "loglevel", required = false) String loglevel,
                                             @RequestParam(value = "connectorDeployMode", required = false) String connectorDeployMode,
                                             @RequestParam(value = "trustStore", required = false) String trustStore,
                                             @RequestParam(value = "trustStorePassword", required = false) String trustStorePassword,
                                             @RequestParam(value = "keyStore", required = false) String keyStore,
                                             @RequestParam(value = "keyStorePassword", required = false) String keyStorePassword,
                                             @RequestParam(value = "proxyUri", required = false) URI proxyUri,
                                             @RequestParam(value = "noProxyUri", required = false) ArrayList<URI> noProxyUriList,
                                             @RequestParam(value = "username", required = false) String username,
                                             @RequestParam(value = "password", required = false) String password);


    @GetMapping(value = "/configmodel", produces = "application/ld+json")
    @Operation(summary = "Get the  configuration model")
    @ApiResponse(responseCode = "200", description = "Succesfully get the configuration model")
    @ApiResponse(responseCode = "400", description = "Can not find the configuration model")
    ResponseEntity<String> getConfigModel();

    @PutMapping(value = "/connector", produces = "application/ld+json")
    @Operation(summary = "Update a connector")
    @ApiResponse(responseCode = "200", description = "Successfully updated the connector description of the configuration model")
    @ApiResponse(responseCode = "400", description = "Failed to update the connector. The configuration model is not valid")
    ResponseEntity<String> updateConnector(@RequestParam(value = "title", required = false) String title,
                                           @RequestParam(value = "description", required = false) String description,
                                           @RequestParam(value = "endpoint", required = false) URI endpoint,
                                           @RequestParam(value = "version", required = false) String version,
                                           @RequestParam(value = "curator", required = false) URI curator,
                                           @RequestParam(value = "maintainer", required = false) URI maintainer,
                                           @RequestParam(value = "inboundModelVersion", required = false) String inboundModelVersion,
                                           @RequestParam(value = "outboundModelVersion", required = false) String outboundModelVersion);
}
