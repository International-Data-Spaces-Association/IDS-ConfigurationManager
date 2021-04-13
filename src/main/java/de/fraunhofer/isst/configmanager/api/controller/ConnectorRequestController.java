package de.fraunhofer.isst.configmanager.api.controller;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.api.ConnectorRequestApi;
import de.fraunhofer.isst.configmanager.api.service.ConnectorRequestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import net.minidev.json.JSONObject;

import java.io.IOException;
import java.net.URI;

/**
 * The api class implements the ConnectorRequestApi and offers the possibilities to manage
 * the request to external connectors.
 */
@Slf4j
@RestController
@RequestMapping("/api/ui")
@Tag(name = "Connector Request Management", description = "Endpoints for managing connector requests")
public class ConnectorRequestController implements ConnectorRequestApi {

    private final transient ConnectorRequestService connectorRequestService;
    private final transient Serializer serializer;

    @Autowired
    public ConnectorRequestController(final ConnectorRequestService connectorRequestService,
                                      final Serializer serializer) {
        this.connectorRequestService = connectorRequestService;
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
    public ResponseEntity<String> requestMetadata(final URI recipientId, final URI requestedResourceId) {
        log.info(">> POST /request/description recipientId: " + recipientId + " requestedResourceId: " + requestedResourceId);
        ResponseEntity<String> response;

        if (requestedResourceId != null) {
            final var resource = connectorRequestService.requestResource(recipientId, requestedResourceId);

            if (resource != null) {
                try {
                    response = ResponseEntity.ok(serializer.serialize(resource));
                } catch (IOException e) {
                    log.error(e.getMessage());
                    response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            } else {
                response = ResponseEntity.badRequest().body("Could not get resource from the requested connector");
            }
        } else {
            final var resources = connectorRequestService.requestResourcesFromConnector(recipientId);

            if (!resources.isEmpty()) {
                try {
                    response = ResponseEntity.ok(serializer.serialize(resources));
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            } else {
                response = ResponseEntity.badRequest().body("Could not get resources from the requested connector");
            }
        }
        return response;
    }

    @Override
    public ResponseEntity<String> requestContract(final URI recipientId,
                                                  final URI requestedArtifactId,
                                                  final String contractOffer) {
        log.info(">> POST /request/contract recipientId: " + recipientId + " requestedArtifactId: " + requestedArtifactId + " contractOffer: " + contractOffer);
        ResponseEntity<String> response;

        final var contractAgreementId = connectorRequestService
                .requestContractAgreement(recipientId.toString(), requestedArtifactId.toString(), contractOffer);

        if (contractAgreementId != null) {
            final var jsonObject = new JSONObject();

            if (contractAgreementId.contains("Failed")) {
                jsonObject.put("message", contractAgreementId);
                response = ResponseEntity.badRequest().body(jsonObject.toJSONString());
            } else {
                jsonObject.put("agreementId", contractAgreementId);
                response = ResponseEntity.ok(jsonObject.toJSONString());
            }
        } else {
            response = ResponseEntity.badRequest().body("Could not get agreement id for the contract");
        }

        return response;
    }
}
