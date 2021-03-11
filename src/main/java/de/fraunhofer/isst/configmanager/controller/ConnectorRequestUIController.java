package de.fraunhofer.isst.configmanager.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConnectorRequestService;
import net.minidev.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;

/**
 * The controller class implements the ConnectorRequestApi and offers the possibilities to manage
 * the request to external connectors.
 */
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
     * @param recipientId   uri of the requested IDS connector
     * @param reqResourceId uri of the requested resource
     * @return if reqResourceId is set, then the resource will be returned otherwise the IDS connector
     */
    @Override
    public ResponseEntity<String> requestMetadata(URI recipientId, URI reqResourceId) {

        if (reqResourceId != null) {
            Resource resource = connectorRequestService.requestResource(recipientId, reqResourceId);
            if (resource != null) {
                JSONArray resourceContent = connectorRequestService.getResourceContent(resource);
                if (resourceContent != null) {
                    try {
                        return ResponseEntity.ok(objectMapper.writeValueAsString(resourceContent));
                    } catch (JsonProcessingException e) {
                        LOGGER.error(e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems while parsing " +
                                "the list to JSON");
                    }
                } else {
                    return ResponseEntity.badRequest().body("Could not get resource content from the requested connector");

                }
            } else {
                return ResponseEntity.badRequest().body("Could not get resource from the requested connector");
            }
        } else {
            List<Resource> resources = connectorRequestService.requestResourcesFromConnector(recipientId);
            if (resources != null && resources.size() > 0) {
                JSONArray customResourceList = connectorRequestService.createResourceList(resources);
                try {
                    return ResponseEntity.ok(objectMapper.writeValueAsString(customResourceList));
                } catch (JsonProcessingException e) {
                    LOGGER.error(e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems while parsing " +
                            "the list to JSON");
                }
            } else {
                return ResponseEntity.badRequest().body("Could not get resources from the requested connector");
            }
        }
    }
}
