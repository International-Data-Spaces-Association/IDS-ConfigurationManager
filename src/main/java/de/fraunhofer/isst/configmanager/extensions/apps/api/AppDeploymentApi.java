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
package de.fraunhofer.isst.configmanager.extensions.apps.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface AppDeploymentApi {
    @GetMapping(value = "/apps", produces = "application/ld+json")
    @Operation(summary = "Returns a list of all known apps from the database")
    @ApiResponse(responseCode = "200", description = "Returns a list of custom apps")
    @ApiResponse(responseCode = "400", description = "Could not find customs apps")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    ResponseEntity<String> getAllKnownApps();

    @GetMapping(value = "/apps/deployments", produces = "application/ld+json")
    @Operation(summary = "Returns a list of all deployed images (apps) in the Connector")
    @ApiResponse(responseCode = "200", description = "Returns a list of custom apps")
    @ApiResponse(responseCode = "400", description = "Could not find customs apps")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    ResponseEntity<String> getAllDeployments();

    @PostMapping(value = "/apps/deployments")
    @Operation(summary = "Pulls a docker image (app) from a registry")
    @ApiResponse(responseCode = "200", description = "Pulled successfully docker image from registry")
    @ApiResponse(responseCode = "400", description = "Failed to pull docker image from registry")
    ResponseEntity<String> getAppFromRegistry(@RequestParam("String imageName") String imageName);

    @DeleteMapping(value = "/apps/deployments")
    @Operation(summary = "Removes a docker image (app)")
    @ApiResponse(responseCode = "200", description = "Removed a docker container")
    ResponseEntity<String> removeAppDeployment(@RequestParam("String containerID") String containerID);

    @PostMapping(value = "/apps/deployments/start")
    @Operation(summary = "Starts a docker container (app)")
    @ApiResponse(responseCode = "200", description = "Started a docker container")
    ResponseEntity<String> startApp(@RequestParam("String containerID") String containerID);

    @PostMapping(value = "/apps/deployments/stop")
    @Operation(summary = "Stops a docker container (app)")
    @ApiResponse(responseCode = "200", description = "Stopped a docker container")
    ResponseEntity<String> stopApp(@RequestParam("String containerID") String containerID);
}
