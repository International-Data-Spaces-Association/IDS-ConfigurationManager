package de.fraunhofer.isst.configmanager.controller;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConfigModelService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Resource contracts Management", description = "Endpoints for managing the contracts of a resource")
public class ResourceContractUIController implements ResourceContractApi {

    private final static Logger logger = LoggerFactory.getLogger(ResourceContractUIController.class);

    private final ConfigModelService configModelService;
    private final Serializer serializer;
    private final DefaultConnectorClient client;

    @Autowired
    public ResourceContractUIController(ConfigModelService configModelService, Serializer serializer,
                                        DefaultConnectorClient client) {
        this.configModelService = configModelService;
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

        if (configModelService.getConfigModel() == null || configModelService.getConfigModel().getAppRoute() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Could not find any resources!\"}");
        }

        for (AppRoute appRoute : configModelService.getConfigModel().getAppRoute()) {
            for (Resource resource : appRoute.getAppRouteOutput()) {
                if (resourceId.equals(resource.getId())) {
                    if (resource.getContractOffer().get(0) != null) {
                        try {
                            return ResponseEntity.ok(serializer.serialize(resource.getContractOffer().get(0)));
                        } catch (IOException e) {
                            logger.error(e.getMessage());
                        }
                    }
                }
            }
        }
        return ResponseEntity.badRequest().body("Could not find the contract");
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

        if (configModelService.getConfigModel() == null || configModelService.getConfigModel().getAppRoute() == null
                || configModelService.getConfigModel().getConnectorDescription().getResourceCatalog() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Could not find any resources!\"}");
        }

        // Create the updated contract offer
        ContractOffer contractOffer = null;
        if (contractJson != null) {
            try {
                contractOffer = serializer.deserialize(contractJson, ContractOffer.class);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

        // Update resource contract in app route
        for (AppRoute appRoute : configModelService.getConfigModel().getAppRoute()) {
            if (appRoute.getAppRouteOutput() != null) {
                for (Resource resource : appRoute.getAppRouteOutput()) {
                    if (resourceId.equals(resource.getId())) {
                        var resourceImpl = (ResourceImpl) resource;
                        resourceImpl.setContractOffer(Util.asList(contractOffer));
                        break;
                    }
                }
            }
        }

        // Update resource representation in resource catalog
        for (ResourceCatalog resourceCatalog : configModelService.getConfigModel()
                .getConnectorDescription().getResourceCatalog()) {
            if (resourceCatalog.getOfferedResource() != null) {
                for (Resource resource : resourceCatalog.getOfferedResource()) {
                    if (resourceId.equals(resource.getId())) {
                        var resourceImpl = (ResourceImpl) resource;
                        resourceImpl.setContractOffer(Util.asList(contractOffer));
                        break;
                    }
                }
            }
        }
        try {
            configModelService.saveState();
            var response = client.updateResourceContract(resourceId.toString(), contractOffer);
            var jsonObject = new JSONObject();
            jsonObject.put("connectorResponse", response);
            jsonObject.put("resourceID", resourceId.toString());
            return ResponseEntity.ok(jsonObject.toJSONString());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return ResponseEntity.badRequest().body("Could not update the representation of the resource");

    }
}
