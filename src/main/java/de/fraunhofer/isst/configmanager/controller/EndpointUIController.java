package de.fraunhofer.isst.configmanager.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.BaseConnectorImpl;
import de.fraunhofer.iais.eis.ConfigurationModelImpl;
import de.fraunhofer.iais.eis.ConnectorEndpoint;
import de.fraunhofer.iais.eis.ConnectorEndpointBuilder;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.configmanagement.service.EndpointService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

/**
 * The controller class implements the EndpointUIApi and offers the possibilities to manage
 * the endpoints in the configuration manager.
 */
@RestController
@RequestMapping("/api/ui")
@Slf4j
@Tag(name = "Endpoints Management", description = "Different endpoint types can be managed here")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EndpointUIController implements EndpointUIApi {
    transient Serializer serializer;
    transient ObjectMapper objectMapper;
    transient ConfigModelService configModelService;
    transient EndpointService endpointService;
    transient DefaultConnectorClient client;

    @Autowired
    public EndpointUIController(final Serializer serializer,
                                final ObjectMapper objectMapper,
                                final ConfigModelService configModelService,
                                final EndpointService endpointService,
                                final DefaultConnectorClient client) {
        this.serializer = serializer;
        this.objectMapper = objectMapper;
        this.configModelService = configModelService;
        this.endpointService = endpointService;
        this.client = client;
    }

    /**
     * This method creates a generic endpoint with the given parameters.
     *
     * @param accessURL access url of the parameter
     * @param username  username for the authentication
     * @param password  password for the authentication
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> createGenericEndpoint(final String accessURL,
                                                        final String username,
                                                        final String password) {
        log.info(">> POST /generic/endpoint accessURL: " + accessURL + " username: " + username + " password: " + password);

        final var genericEndpoint =
                endpointService.createGenericEndpoint(accessURL, username, password);
        if (genericEndpoint != null) {
            final var jsonObject = new JSONObject();
            jsonObject.put("id", genericEndpoint.getId().toString());
            jsonObject.put("message", "Created a new generic endpoint");
            return ResponseEntity.ok(jsonObject.toJSONString());
        } else {
            return ResponseEntity.badRequest().body("Could not create a generic endpoint");
        }
    }

    /**
     * This method returns a list of generic endpoints.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getGenericEndpoints() {
        log.info(">> GET /generic/endpoints");

        final var endpoints = endpointService.getGenericEndpoints();
        try {
            return ResponseEntity.ok(serializer.serialize(endpoints));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems while " +
                    "serializing");
        }
    }

    /**
     * This method returns a generic endpoint.
     *
     * @param endpointId id of the generic endpoint
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getGenericEndpoint(final URI endpointId) {
        log.info(">> GET /generic/endpoint endpointId: " + endpointId);

        final var genericEndpoint = endpointService.getGenericEndpoint(endpointId);
        if (genericEndpoint != null) {
            try {
                return ResponseEntity.ok(serializer.serialize(genericEndpoint));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems " +
                        "while serializing");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not get the generic endpoint");
        }
    }

    /**
     * This method deletes a generic endpoint.
     *
     * @param endpointId id of the generic endpoint
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> deleteGenericEndpoint(final URI endpointId) {
        log.info(">> DELETE /generic/endpoint endpointId: " + endpointId);

        final boolean deleted = endpointService.deleteGenericEndpoint(endpointId);
        if (deleted) {
            return ResponseEntity.ok("Deleted the generic endpoint with id: " + endpointId);
        } else {
            return ResponseEntity.badRequest().body("Could not delete the generic endpoint with " +
                    "id: " + endpointId);
        }
    }

    /**
     * This method updates a generic endpoint with the given parameters.
     *
     * @param endpointId id of the generic endpoint
     * @param accessURL  access url of the endpoint
     * @param username   username for authentication
     * @param password   password for authentication
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateGenericEndpoint(final URI endpointId,
                                                        final String accessURL,
                                                        final String username, final String
            password) {
        log.info(">> PUT /generic/endpoint endpointId: " + endpointId + " accessURL: " + accessURL + " username: " + password);

        final boolean updated = endpointService.updateGenericEndpoint(endpointId, accessURL, username,
                password);
        if (updated) {
            return ResponseEntity.ok("Updated the generic endpoint with id: " + endpointId);
        } else {
            return ResponseEntity.badRequest().body("Could not update the generic endpoint with " +
                    "id: " + endpointId);
        }
    }

    /**
     * This method returns all connector endpoints.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getConnectorEndpoints() {
        log.info(">> GET /connector/endpoints");

        if (configModelService.getConfigModel().getConnectorDescription() == null) {
            return ResponseEntity.badRequest().body("Could not get the connector");
        }
        final var connector = configModelService.getConfigModel().getConnectorDescription();
        if (connector.getHasEndpoint() == null) {
            return ResponseEntity.badRequest().body("Could not find any connector endpoints");
        } else {
            try {
                return ResponseEntity.ok(serializer.serialize(connector.getHasEndpoint()));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems " +
                        "while serializing");
            }
        }

    }

    /**
     * This method returns a specific connector endpoint.
     *
     * @param connectorEndpointId id of the connector endpoint
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getConnectorEndpoint(final URI connectorEndpointId) {
        if (configModelService.getConfigModel().getConnectorDescription() == null) {
            return ResponseEntity.badRequest().body("Could not get the connector");
        }
        final var connector = configModelService.getConfigModel().getConnectorDescription();
        if (connector.getHasEndpoint() == null) {
            return ResponseEntity.badRequest().body("Could not find any connector endpoints");
        } else {
            final var connectorEndpoint = connector.getHasEndpoint()
                    .stream()
                    .filter(connectorEndpoint1 -> connectorEndpoint1.getId().equals(connectorEndpointId))
                    .findAny().orElse(null);
            if (connectorEndpoint != null) {
                try {
                    return ResponseEntity.ok(serializer.serialize(connectorEndpoint));
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems" +
                            " while serializing");
                }
            }
        }
        return ResponseEntity.badRequest().body("Could not find any connector endpoint with id: " + connectorEndpointId);
    }

    /**
     * This method identifies the connector by access url and resource id and then returns a list
     * of connector endpoints.
     *
     * @param accessUrl  access url of the connector
     * @param resourceId id of the resource
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getConnectorEndpointsFromClient(final String accessUrl,
                                                                  final String resourceId) {

        try {
            final var baseConnector = client.getBaseConnector(accessUrl, resourceId);
            if (baseConnector == null) {
                return ResponseEntity.badRequest().body("Could not determine the connector with " +
                        "the access url: "
                        + accessUrl);
            } else {
                if (baseConnector.getHasEndpoint() == null) {
                    return ResponseEntity.ok(objectMapper.writeValueAsString(new JSONArray()));
                } else {
                    return ResponseEntity.ok(serializer.serialize(baseConnector.getHasEndpoint()));
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body("Could not determine connector endpoints from" +
                    " client");
        }
    }

    /**
     * This method creates a connector endpoint with given parameters.
     *
     * @param accessUrl access url of the endpoint
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> createConnectorEndpoint(final String accessUrl) {

        final var configModelImpl = (ConfigurationModelImpl) configModelService.getConfigModel();
        final var baseConnector = (BaseConnectorImpl) configModelImpl.getConnectorDescription();
        if (baseConnector.getHasEndpoint() == null) {
            baseConnector.setHasEndpoint(new ArrayList<>());
        }
        final var connectorEndpoints =
                (ArrayList<ConnectorEndpoint>) baseConnector.getHasEndpoint();
        // Create Connector Endpoint
        final var connectorEndpoint =
                new ConnectorEndpointBuilder()._accessURL_(URI.create(accessUrl)).build();
        // Add Connector Endpoint in Connector
        connectorEndpoints.add(connectorEndpoint);
        configModelService.saveState();
        final var jsonObject = new JSONObject();
        jsonObject.put("connectorEndpointId", connectorEndpoint.getId().toString());
        jsonObject.put("message", "Created a new connector endpoint for the connector");
        return ResponseEntity.ok(jsonObject.toJSONString());
    }
}
