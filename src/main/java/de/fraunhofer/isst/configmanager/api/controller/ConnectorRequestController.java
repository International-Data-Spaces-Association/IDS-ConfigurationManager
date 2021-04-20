package de.fraunhofer.isst.configmanager.api.controller;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.api.ConnectorRequestApi;
import de.fraunhofer.isst.configmanager.api.service.ConnectorRequestService;
import de.fraunhofer.isst.configmanager.model.config.QueryInput;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;

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
        log.info(">> POST /request/description recipientId: {} and requestedResource: {}", recipientId, requestedResourceId);
        ResponseEntity<String> response;

        if (requestedResourceId != null) {
            // JSON object contains key and the resource itself
            final var validKeyAndResource = connectorRequestService.requestResource(recipientId, requestedResourceId);
            if (validKeyAndResource != null) {
                response = ResponseEntity.ok(validKeyAndResource);
            } else {
                response = ResponseEntity.badRequest().body("Could not get key and resource from the requested connector");
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

    /**
     * @param recipientId         id of the recipient
     * @param requestedArtifactId id of the requested artifact
     * @param contractOffer       the contract offer
     * @return contract agreement id
     */
    @Override
    public ResponseEntity<String> requestContract(final URI recipientId,
                                                  final URI requestedArtifactId,
                                                  final String contractOffer) {
        log.info(">> POST /request/contract with recipient: {}, artifact: {}, contract: {}",
                recipientId, requestedArtifactId, contractOffer);


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

    /**
     * @param recipientId         the target connector uri
     * @param requestedArtifactId the requested artifact uri
     * @param contractId          the URI of the contract agreement
     * @param key                 a {@link java.util.UUID} object
     * @param queryInput          the query to fetch data from backend systems
     * @return requested data from an external connector
     */
    @Override
    public ResponseEntity<String> requestData(final URI recipientId,
                                              final URI requestedArtifactId,
                                              final URI contractId,
                                              final UUID key,
                                              final QueryInput queryInput) {

        log.info(">> POST /request/artifact with recipient: {}, artifact: {}, contract: {}, key: {} and queryInput: {} ",
                recipientId, requestedArtifactId, contractId, key, queryInput);

        ResponseEntity<String> response = null;
        try {
            final var requestDataResponse = connectorRequestService.requestData(recipientId, requestedArtifactId,
                    contractId, key, queryInput);
            final var clientResponseString = Objects.requireNonNull(requestDataResponse.body()).string();
            final var jsonObject = new JSONObject();

            if (requestDataResponse.isSuccessful() && !clientResponseString.contains("REJECTION")) {
                final var splitBody = clientResponseString.split("\n", 2);
                jsonObject.put("message", "Saved at: " + key);
                jsonObject.put("data", splitBody[1].substring(10));
                response = ResponseEntity.ok(jsonObject.toJSONString());
            } else {
                jsonObject.put("message", clientResponseString);
                response = ResponseEntity.badRequest().body(clientResponseString);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return response;
    }
}
