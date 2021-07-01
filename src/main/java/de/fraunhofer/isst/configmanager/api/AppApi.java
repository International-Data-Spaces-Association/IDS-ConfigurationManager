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
import org.springframework.web.bind.annotation.GetMapping;

public interface AppApi {
    @GetMapping(value = "/apps", produces = "application/ld+json")
    @Operation(summary = "Returns a list of all apps")
    @ApiResponse(responseCode = "200", description = "Returns a list of custom apps")
    @ApiResponse(responseCode = "400", description = "Could not find customs apps")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    ResponseEntity<String> getApps();
}
