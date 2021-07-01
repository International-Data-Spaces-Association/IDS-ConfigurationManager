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
package de.fraunhofer.isst.configmanager.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.isst.configmanager.api.AppApi;
import de.fraunhofer.isst.configmanager.api.service.AppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/ui")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "App Management", description = "Endpoints for managing the app in the configuration manager")
public class AppController implements AppApi {

    transient AppService appService;
    transient ObjectMapper objectMapper;

    @Autowired
    public AppController(final AppService appService, final ObjectMapper objectMapper) {
        this.appService = appService;
        this.objectMapper = objectMapper;
    }

    /**
     * This method returns a list of custom apps.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getApps() {
        if (log.isInfoEnabled()) {
            log.info(">> GET /apps");
        }
        ResponseEntity<String> response;

        final var customAppList = appService.getApps();

        if (!customAppList.isEmpty()) {
            try {
                response = ResponseEntity.ok(objectMapper.writeValueAsString(customAppList));
            } catch (JsonProcessingException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            response = ResponseEntity.badRequest().body("Could not find custom apps");
        }

        return response;
    }
}
