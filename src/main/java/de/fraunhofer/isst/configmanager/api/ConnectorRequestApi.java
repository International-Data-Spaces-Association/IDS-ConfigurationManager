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

import de.fraunhofer.isst.configmanager.data.util.QueryInput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.util.UUID;

public interface ConnectorRequestApi {
    @PostMapping(value = "/request/description")
    @Operation(summary = "Request metadata from another IDS connector.")
    @ApiResponse(responseCode = "200", description = "Successfully requested  metadata from IDS connector")
    @ApiResponse(responseCode = "400", description = "Can not request metadata from IDS connector")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    ResponseEntity<String> requestMetadata(@RequestParam("recipientId") URI recipientId,
                                           @RequestParam(value = "requestedResourceId", required = false) URI requestedResourceId);

    @PostMapping(value = "/request/contract", produces = "application/ld+json")
    @Operation(summary = "Send a contract request to another IDS connector.")
    @ApiResponse(responseCode = "200", description = "Successfully send a contract request to another IDS connector")
    @ApiResponse(responseCode = "400", description = "Can not return the contract agreement id")
    ResponseEntity<String> requestContract(@RequestParam("recipientId") URI recipientId,
                                           @RequestParam(value = "requestedArtifactId ") URI requestedArtifactId,
                                           @RequestBody(required = false) String contractOffer);

    @PostMapping(value = "/request/artifact")
    @Operation(summary = "Request data from another IDS connector.")
    @ApiResponse(responseCode = "200", description = "Successfully requested data from another IDS connector")
    @ApiResponse(responseCode = "400", description = "Can not request data from IDS connector")
    ResponseEntity<String> requestData(@RequestParam("recipientId") URI recipientId,
                                       @RequestParam(value = "requestedArtifactId ") URI requestedArtifactId,
                                       @RequestParam(value = "contractId", required = false) URI contractId,
                                       @RequestParam(value = "key") UUID key,
                                       @RequestBody(required = false) QueryInput queryInput);
}
