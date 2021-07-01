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

public interface BrokerApi {
    // APIs to manage custom broker
    @PostMapping(value = "/broker", produces = "application/ld+json")
    @Operation(summary = "Creates a new broker")
    @ApiResponse(responseCode = "200", description = "Created a new broker")
    ResponseEntity<String> createBroker(@RequestParam(value = "brokerUri") URI brokerUri,
                                        @RequestParam(value = "title", required = false) String title);

    @PutMapping(value = "/broker", produces = "application/ld+json")
    @Operation(summary = "Updates a broker")
    @ApiResponse(responseCode = "200", description = "Updated the broker")
    @ApiResponse(responseCode = "400", description = "Can not update the broker")
    ResponseEntity<String> updateBroker(@RequestParam(value = "brokerUri") URI brokerUri,
                                        @RequestParam(value = "title", required = false) String title);

    @DeleteMapping(value = "/broker", produces = "application/ld+json")
    @Operation(summary = "Deletes a broker")
    @ApiResponse(responseCode = "200", description = "Deleted the broker")
    @ApiResponse(responseCode = "400", description = "Can not delete the broker")
    ResponseEntity<String> deleteBroker(@RequestParam(value = "brokerUri") URI brokerUri);

    @GetMapping(value = "/brokers", produces = "application/ld+json")
    @Operation(summary = "Returns the list of all brokers")
    @ApiResponse(responseCode = "200", description = "Successfully returned the list of all brokers")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    ResponseEntity<String> getAllBrokers();

    @PostMapping(value = "/broker/register", produces = "application/ld+json")
    @Operation(summary = "Registers the connector with the broker")
    @ApiResponse(responseCode = "200", description = "Successfully registered the connector with the broker")
    @ApiResponse(responseCode = "400", description = "Can not find the broker to register the connector")
    ResponseEntity<String> registerConnector(@RequestParam(value = "brokerUri") URI brokerUri);

    @PostMapping(value = "/broker/unregister", produces = "application/ld+json")
    @Operation(summary = "Unregisters the connector with the broker")
    @ApiResponse(responseCode = "200", description = "Successfully unregistered the connector with the broker")
    @ApiResponse(responseCode = "400", description = "Can not find the broker to unregister the connector")
    ResponseEntity<String> unregisterConnector(@RequestParam(value = "brokerUri") URI brokerUri);

    @PostMapping(value = "/broker/update", produces = "application/ld+json")
    @Operation(summary = "Updates the self description at the broker")
    @ApiResponse(responseCode = "200", description = "Successfully updated the self description at the broker")
    @ApiResponse(responseCode = "400", description = "Can not find the broker to update the connector")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    ResponseEntity<String> updateConnector(@RequestParam(value = "brokerUri") URI brokerUri);

    // APIs to manage the resources at broker
    @PostMapping(value = "/broker/update/resource", produces = "application/ld+json")
    @Operation(summary = "Updates a resource at the broker")
    @ApiResponse(responseCode = "200", description = "Successfully updated the resource at the broker")
    @ApiResponse(responseCode = "400", description = "Can not find the broker to update the connector")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    ResponseEntity<String> updateResourceAtBroker(@RequestParam(value = "brokerUri") URI brokerUri,
                                                  @RequestParam("resourceId") URI resourceId);

    @PostMapping(value = "/broker/delete/resource", produces = "application/ld+json")
    @Operation(summary = "Deletes a resource at the broker")
    @ApiResponse(responseCode = "200", description = "Successfully deleted the resource at the broker")
    @ApiResponse(responseCode = "400", description = "Can not find the broker to update the connector")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    ResponseEntity<String> deleteResourceAtBroker(@RequestParam(value = "brokerUri") URI brokerUri,
                                                  @RequestParam("resourceId") URI resourceId);

    @GetMapping(value = "/broker/resource/information", produces = "application/ld+json")
    @Operation(summary = "Returns information about registration status for resources")
    @ApiResponse(responseCode = "200", description = "Successfully returned information about registration status for resources")
    ResponseEntity<String> getRegisterStatusForResource(@RequestParam("resourceId") URI resourceId);

}
