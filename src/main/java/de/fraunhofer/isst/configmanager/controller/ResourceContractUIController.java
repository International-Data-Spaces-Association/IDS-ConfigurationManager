package de.fraunhofer.isst.configmanager.controller;

import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.configmanagement.service.ResourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;

/**
 * The controller class implements the ResourceContractApi and offers the possibilities to manage
 * the contracts in a resource.
 */
@RestController
@RequestMapping("/api/ui")
@Slf4j
@Tag(name = "Resource contracts Management", description = "Endpoints for managing the contracts of a resource")
public class ResourceContractUIController implements ResourceContractApi {

    private final static Logger logger = LoggerFactory.getLogger(ResourceContractUIController.class);

    private final ConfigModelService configModelService;
    private final ResourceService resourceService;
    private final Serializer serializer;
    private final DefaultConnectorClient client;

    @Autowired
    public ResourceContractUIController(ConfigModelService configModelService,
                                        ResourceService resourceService,
                                        Serializer serializer,
                                        DefaultConnectorClient client) {
        this.configModelService = configModelService;
        this.resourceService = resourceService;
        this.serializer = serializer;
        this.client = client;
    }

    /**
     * This method returns the contract from a specific resource.
     *
     * @param resourceId id of the resource
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getResourceContract(URI resourceId) {
        log.info(">> GET /resource/contract resourceId: " + resourceId);

        if (configModelService.getConfigModel() == null ||
                configModelService.getConfigModel().getConnectorDescription().getResourceCatalog() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Could not find any resources!\"}");
        }

        ContractOffer contractOffer = resourceService.getResourceContract(resourceId);
        if (contractOffer != null) {
            try {
                return ResponseEntity.ok(serializer.serialize(contractOffer));
            } catch (IOException e) {
                logger.error(e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems while parsing serializing " +
                        "the contract offer");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not get the resource contract");
        }
    }

    /**
     * This method updates the contract of a resource.
     *
     * @param resourceId   id of the resource
     * @param contractJson id of the contract
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateResourceContract(URI resourceId, String contractJson) {
        log.info(">> PUT /resource/contract resourceId: " + resourceId + " contractJson: " + contractJson);
        // Create the updated contract offer
        ContractOffer contractOffer = null;
        if (contractJson != null) {
            try {
                contractOffer = serializer.deserialize(contractJson, ContractOffer.class);
            } catch (IOException e) {
                logger.error(e.getMessage());
                return ResponseEntity.badRequest().body("Problems while deserializing the contract");
            }
        }

        // Update the resource contract
        if (contractOffer != null) {
            var jsonObject = new JSONObject();
            try {
                jsonObject.put("resourceID", resourceId.toString());
                jsonObject.put("contractID", contractOffer.getId().toString());
                var response = client.updateResourceContract(resourceId.toString(), contractJson);
                resourceService.updateResourceContractInAppRoute(resourceId, contractOffer);
                jsonObject.put("connectorResponse", response);
                return ResponseEntity.ok(jsonObject.toJSONString());
            } catch (IOException e) {
                logger.error(e.getMessage());
                jsonObject.put("message", "Problems while updating the contract at the connector");
                return ResponseEntity.badRequest().body(jsonObject.toJSONString());
            }
        } else {
            return ResponseEntity.badRequest().body("Could not update the resource representation");
        }
    }
}
