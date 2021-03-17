package de.fraunhofer.isst.configmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConnectorRequestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * The controller class implements the ConnectorRequestApi and offers the possibilities to manage
 * the request to external connectors.
 */
@RestController
@RequestMapping("/api/ui")
@Tag(name = "Connector Request Management", description = "Endpoints for managing connector requests")
public class ConnectorRequestUIController implements ConnectorRequestApi {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConnectorRequestUIController.class);
    private final ConnectorRequestService connectorRequestService;

    private final ObjectMapper objectMapper;
    private final Serializer serializer;

    @Autowired
    public ConnectorRequestUIController(ConnectorRequestService connectorRequestService,
                                        ObjectMapper objectMapper,
                                        Serializer serializer) {
        this.connectorRequestService = connectorRequestService;
        this.objectMapper = objectMapper;
        this.serializer = serializer;
    }

    /**
     * This method request metadata from an IDS connector.
     *
     * @param recipientId         uri of the requested IDS connector
     * @param requestedResourceId uri of the requested resource
     * @return if reqResourceId is set, then the resource will be returned otherwise the IDS connector
     */
    @Override
    public ResponseEntity<String> requestMetadata(URI recipientId, URI requestedResourceId) {

        if (requestedResourceId != null) {
            Resource resource = connectorRequestService.requestResource(recipientId, requestedResourceId);
            if (resource != null) {
                try {
                    return ResponseEntity.ok(serializer.serialize(resource));
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                    return ResponseEntity.badRequest().body("Problems while serializing the resource");
                }
            } else {
                return ResponseEntity.badRequest().body("Could not get resource from the requested connector");
            }
        } else {
            List<Resource> resources = connectorRequestService.requestResourcesFromConnector(recipientId);
            if (resources != null && resources.size() > 0) {
                try {
                    return ResponseEntity.ok(serializer.serialize(resources));
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                    return ResponseEntity.badRequest().body("Problems while serializing the list of resources");
                }
            } else {
                return ResponseEntity.badRequest().body("Could not get resources from the requested connector");
            }
        }
    }

    @Override
    public ResponseEntity<String> requestContract(URI recipientId, URI requestedArtifactId, String contractOffer) {

        String contractAgreementId = connectorRequestService
                .requestContractAgreement(recipientId.toString(), requestedArtifactId.toString(), contractOffer);
        if (contractAgreementId != null) {
            var jsonObject = new JSONObject();
            jsonObject.put("agreementId", contractAgreementId);
            return ResponseEntity.ok(jsonObject.toJSONString());
        } else {
            return ResponseEntity.badRequest().body("Could not get agreement id for the contract");
        }
    }
}
