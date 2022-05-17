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
package de.fraunhofer.isst.configmanager.extensions.apps.api.controller;

import de.fraunhofer.isst.configmanager.extensions.apps.api.AppDeploymentApi;
import de.fraunhofer.isst.configmanager.extensions.apps.api.client.AppStoreRegistryClient;
import de.fraunhofer.isst.configmanager.extensions.apps.api.service.AppDeploymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/ui")
@AllArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "Extension: Apps", description = "Endpoints for managing deployments of apps")
public class AppDeploymentController implements AppDeploymentApi {

    transient AppStoreRegistryClient appStoreRegistryClient;
    transient AppDeploymentService appDeploymentService;

    @Override
    public ResponseEntity<String> getAllKnownApps() {
        if (log.isInfoEnabled()) {
            log.info(">> GET /apps");
        }

        //TODO: Read all App-Information about known apps from DB (!= actual currently deployed apps)
        return null;
    }

    /**
     * This method returns a list of custom apps.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getAllDeployments() {
        if (log.isInfoEnabled()) {
            log.info(">> GET /apps/deployments");
        }

        //TODO: get all currently deployed images

        return null;
    }

    @Override
    public ResponseEntity<String> getAppFromRegistry(final String imageName) {
        if (log.isInfoEnabled()) {
            log.info(">> POST /apps/deployments");
        }

        final var result = appStoreRegistryClient.pullImage(imageName);

        ResponseEntity<String> response;
        if (result) {
            response = ResponseEntity.ok("Pulled successfully docker image from registry");
        } else {
            response = ResponseEntity.badRequest().body("Failed to pull docker image from registry");
        }
        return response;
    }

    @Override
    public ResponseEntity<String> removeAppDeployment(final String containerID) {
        if (log.isInfoEnabled()) {
            log.info(">> DELETE /apps/deployments");
        }

        appDeploymentService.removeContainer(containerID);
        return ResponseEntity.ok("Removed container successfully");
    }

    @Override
    public ResponseEntity<String> startApp(final String containerID) {
        if (log.isInfoEnabled()) {
            log.info(">> POST /apps/deployments/start");
        }

        appDeploymentService.startContainer(containerID);
        return ResponseEntity.ok("Started container successfully");
    }

    @Override
    public ResponseEntity<String> stopApp(final String containerID) {
        if (log.isInfoEnabled()) {
            log.info(">> POST /apps/deployments/stop");
        }

        appDeploymentService.stopContainer(containerID);
        return ResponseEntity.ok("Stopped container successfully");
    }
}
