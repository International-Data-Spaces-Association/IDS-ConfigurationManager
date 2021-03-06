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
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.api.ResourceContractApi;
import de.fraunhofer.isst.configmanager.api.service.resources.ResourceContractService;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultResourceClient;
import de.fraunhofer.isst.configmanager.data.enums.UsagePolicyName;
import de.fraunhofer.isst.configmanager.util.ValidateApiInput;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;

/**
 * The api class implements the ResourceContractApi and offers the possibilities to manage
 * the contracts in a resource.
 */
@Slf4j
@RestController
@RequestMapping("/api/ui")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "Resource contracts Management", description = "Endpoints for managing the contracts of a resource")
public class ResourceContractController implements ResourceContractApi {

    transient ResourceContractService resourceContractService;
    transient Serializer serializer;
    transient DefaultResourceClient client;

    @Autowired
    public ResourceContractController(final ResourceContractService resourceContractService,
                                      final Serializer serializer,
                                      final DefaultResourceClient client) {
        this.resourceContractService = resourceContractService;
        this.serializer = serializer;
        this.client = client;
    }

    /**
     * This method updates the contract of a resource.
     *
     * @param resourceId   id of the resource
     * @param contractJson id of the contract
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateResourceContract(final URI resourceId, final String contractJson) {
        if (log.isInfoEnabled()) {
            log.info(">> PUT /resource/contract resourceId: " + resourceId + " contractJson: " + contractJson);
        }
        ResponseEntity<String> response;

        if ("{}".equals(contractJson) && ValidateApiInput.notValid(resourceId.toString())) {
            response = ResponseEntity.badRequest().body("All validated parameter have undefined as value!");
        } else {
            ContractOffer contractOffer;

            try {
                contractOffer = serializer.deserialize(contractJson, ContractOffer.class);

                if (contractOffer != null) {
                    final var jsonObject = new JSONObject();

                    response = updateResourceContract(
                            resourceId,
                            contractJson,
                            contractOffer,
                            jsonObject);
                } else {
                    response = ResponseEntity.badRequest().body("Could not update the resource contract");
                }
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        return response;
    }

    @NotNull
    private ResponseEntity<String> updateResourceContract(final URI resourceId,
                                                          final String contractJson,
                                                          final ContractOffer contractOffer,
                                                          final JSONObject jsonObject) {
        ResponseEntity<String> response;
        try {
            jsonObject.put("resourceID", resourceId.toString());
            jsonObject.put("contractID", contractOffer.getId().toString());

            final var clientResponse = client.updateResourceContract(resourceId.toString(), contractJson);

            resourceContractService.updateResourceContractInAppRoute(resourceId, contractOffer);

            jsonObject.put("connectorResponse", clientResponse);
            response = ResponseEntity.ok(jsonObject.toJSONString());
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            jsonObject.put("message", "Problems while updating the contract at the connector");
            response = ResponseEntity.badRequest().body(jsonObject.toJSONString());
        }
        return response;
    }

    /**
     * @param resourceId   id of the resource
     * @param usagePolicyName      the pattern of the contract
     * @param contractJson the created contract for the resource
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateContractForResource(final URI resourceId,
                                                            final UsagePolicyName usagePolicyName,
                                                            final String contractJson) {
        if (log.isInfoEnabled()) {
            log.info(">> PUT /resource/contract/update resourceId: " + resourceId + "pattern" + usagePolicyName.toString()
                    + " contractJson: " + contractJson);
        }

        ResponseEntity<String> response;

        if (ValidateApiInput.notValid(resourceId.toString())) {
            response = ResponseEntity.badRequest().body("All validated parameter have undefined as value!");
        } else {
            ContractOffer contractOffer = null;
            try {
                contractOffer = resourceContractService.getContractOffer(usagePolicyName, contractJson);
            } catch (JsonProcessingException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage());
                }
            }

            // Update the resource contract
            if (contractOffer != null) {
                final var jsonObject = new JSONObject();
                try {
                    final var contract = serializer.serialize(contractOffer);

                    jsonObject.put("resourceID", resourceId.toString());
                    jsonObject.put("contractID", contractOffer.getId().toString());

                    final var connectorResponse = client.updateResourceContract(resourceId.toString(), contract);

                    resourceContractService.updateResourceContractInAppRoute(resourceId, contractOffer);

                    jsonObject.put("connectorResponse", connectorResponse);
                    response = ResponseEntity.ok(jsonObject.toJSONString());
                } catch (IOException e) {
                    if (log.isErrorEnabled()) {
                        log.error(e.getMessage(), e);
                    }
                    jsonObject.put("message", "Problems while updating the contract at the connector");
                    response = ResponseEntity.badRequest().body(jsonObject.toJSONString());
                }
            } else {
                response = ResponseEntity.badRequest().body("Could not update the resource representation");
            }
        }
        return response;
    }
}
