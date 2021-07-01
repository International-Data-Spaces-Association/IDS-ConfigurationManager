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

import de.fraunhofer.isst.configmanager.data.enums.UsagePolicyName;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

public interface ResourceContractApi {
    @PutMapping(value = "/resource/contract", produces = "application/ld+json")
    @Operation(summary = "Updates the contract in a resource")
    @ApiResponse(responseCode = "200", description = "Successfully updated the contract in the resource")
    @ApiResponse(responseCode = "400", description = "Can not update the resource contract")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    ResponseEntity<String> updateResourceContract(@RequestParam("resourceId") URI resourceId,
                                                  @RequestBody String contractJson);

    @PutMapping(value = "/resource/contract/update", produces = "application/ld+json")
    @Operation(summary = "Updates the contract in a resource")
    @ApiResponse(responseCode = "200", description = "Successfully updated the contract in the resource")
    @ApiResponse(responseCode = "400", description = "Can not update the resource contract")
    ResponseEntity<String> updateContractForResource(@RequestParam("resourceId") URI resourceId,
                                                     @RequestParam("pattern") UsagePolicyName usagePolicyName,
                                                     @RequestBody(required = false) String contractJson);


}
