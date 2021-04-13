package de.fraunhofer.isst.configmanager.api.controller;

import de.fraunhofer.iais.eis.BaseConnector;
import de.fraunhofer.iais.eis.ConfigurationModelImpl;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.api.ConnectorApi;
import de.fraunhofer.isst.configmanager.api.service.BrokerService;
import de.fraunhofer.isst.configmanager.api.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.api.service.ConnectorService;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultBrokerClient;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.util.Utility;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * The api class implements the ConnectorApi and offers the possibilities to manage
 * the connectors in the configuration manager.
 */
@Slf4j
@RestController
@RequestMapping("/api/ui")
@Tag(name = "Connector Management", description = "Endpoints for managing the connectors in the configuration manager")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ConnectorController implements ConnectorApi {

    transient ConnectorService connectorService;
    transient ConfigModelService configModelService;
    transient BrokerService brokerService;
    transient Serializer serializer;
    transient DefaultConnectorClient client;
    transient DefaultBrokerClient brokerClient;

    @Autowired
    public ConnectorController(final ConnectorService connectorService,
                               final ConfigModelService configModelService,
                               final BrokerService brokerService,
                               final Serializer serializer,
                               final DefaultConnectorClient client,
                               final DefaultBrokerClient brokerClient) {
        this.configModelService = configModelService;
        this.connectorService = connectorService;
        this.brokerService = brokerService;
        this.serializer = serializer;
        this.client = client;
        this.brokerClient = brokerClient;
    }

    /**
     * This methods returns the connector description from the configuration model.
     *
     * @return the connector description
     */
    @Override
    public ResponseEntity<String> getConnector() {
        log.info(">> GET /connector");
        ResponseEntity<String> response;

        final var connector = configModelService.getConfigModel().getConnectorDescription();

        if (connector != null) {
            try {
                response = new ResponseEntity<>(serializer.serialize(connector), HttpStatus.OK);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            response = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return response;
    }

    /**
     * This methods tries to connect to the  public connector endpoint.
     *
     * @return accessibility status
     */
    @Override
    public ResponseEntity<String> getConnectorStatus() {
        log.info(">> GET /connector/status");
        ResponseEntity<String> response;

        final var json = new JSONObject();

        try {
            client.getConnectorStatus();

            log.info("---- [ConnectorController getConnectorStatus] Could connect to the Connector!");

            json.put("status", "Public connector endpoint reachable.");
            response = new ResponseEntity<>(json.toString(), HttpStatus.OK);
        } catch (Exception e) {
            //Error case does not need to be processed further here
            log.warn("---- [ConnectorController getConnectorStatus] Could not connect to the Connector!");

            json.put("status", "Public connector endpoint not reachable.");
            response = new ResponseEntity<>(json.toString(), HttpStatus.SERVICE_UNAVAILABLE);
        }

        return response;
    }

    /**
     * This method returns as response the base connector in JSON format.
     *
     * @return as response the connector in JSON format
     */
    @Override
    public ResponseEntity<String> getConnectorJson() {
        log.info(">> GET /connector/json");

        final var baseConnector = (BaseConnector) configModelService.getConfigModel().getConnectorDescription();

        final var baseConnectorJson = new JSONObject();
        baseConnectorJson.put("title", baseConnector.getTitle().get(0).getValue());
        baseConnectorJson.put("description", baseConnector.getDescription().get(0).getValue());
        baseConnectorJson.put("endpointAccessURL", baseConnector.getHasDefaultEndpoint().getAccessURL().toString());
        baseConnectorJson.put("version", baseConnector.getVersion());
        baseConnectorJson.put("curator", baseConnector.getCurator().toString());
        baseConnectorJson.put("maintainer", baseConnector.getMaintainer().toString());
        baseConnectorJson.put("inboundModelVersion", baseConnector.getInboundModelVersion().get(0));
        baseConnectorJson.put("outboundModelVersion", baseConnector.getOutboundModelVersion());
        baseConnectorJson.put("securityProfile", baseConnector.getSecurityProfile());

        return ResponseEntity.ok(baseConnectorJson.toJSONString());
    }

    /**
     * This method creates a connector description for the configuration model with the given
     * parameters.
     *
     * @param title                title of the connector
     * @param description          description of the connector
     * @param endpointAccessURL    access url of the connector endpoint
     * @param version              version of the connector
     * @param curator              curator of the connector
     * @param maintainer           maintainer of the connector
     * @param inboundModelVersion  the inbound model version of the connector
     * @param outboundModelVersion the outbound model version of the connector
     * @return http response message with the id of the created connector
     */
    @Override
    public ResponseEntity<String> createConnector(final String title, final String description,
                                                  final String endpointAccessURL,
                                                  final String version, final String curator,
                                                  final String maintainer,
                                                  final String inboundModelVersion,
                                                  final String outboundModelVersion) {
        log.info(">> POST /connector");

        final var baseConnector = connectorService.createConnector(title, description,
                endpointAccessURL, version,
                curator, maintainer, inboundModelVersion,
                outboundModelVersion);

        final var configModelImpl = (ConfigurationModelImpl) configModelService.getConfigModel();
        configModelImpl.setConnectorDescription(baseConnector);
        configModelService.saveState();

        return ResponseEntity.ok(Utility
                .jsonMessage("message", "Successfully created a new connector with the id: " + baseConnector.getId().toString()));
    }

    /**
     * This method updates the connector description from the configuration model with the given
     * parameters.
     *
     * @param title                title of the connector
     * @param description          description of the connector
     * @param endpointAccessURL    access url of the connector endpoint
     * @param version              version of the connector
     * @param curator              curator of the connector
     * @param maintainer           maintainer of the connector
     * @param inboundModelVersion  the inbound model version of the connector
     * @param outboundModelVersion the outbound model version of the connector
     * @return http response message with the id of the created connector
     */
    @Override
    public ResponseEntity<String> updateConnector(final String title, final String description,
                                                  final String endpointAccessURL,
                                                  final String version, final String curator,
                                                  final String maintainer,
                                                  final String inboundModelVersion,
                                                  final String outboundModelVersion) {
        log.info(">> PUT /connector title: " + title + " description: " + " endpointAccessURL: " + endpointAccessURL
                + " version: " + version + " curator: " + curator + " maintainer: " + maintainer + " inboundModelVersion: "
                + inboundModelVersion + " outboundModelVersion: " + outboundModelVersion);

        ResponseEntity<String> response;

        final boolean updated = connectorService.updateConnector(title, description, endpointAccessURL,
                version,
                curator, maintainer, inboundModelVersion, outboundModelVersion);

        final var jsonObject = new JSONObject();

        if (updated) {
            jsonObject.put("message", "Successfully updated the connector");
            final var configurationModel = (ConfigurationModelImpl) configModelService.getConfigModel();

            if (configurationModel.getAppRoute() != null) {
                configurationModel.setAppRoute(Util.asList());
            }

            try {
                final var valid = client.sendConfiguration(serializer.serialize(configurationModel));

                if (valid) {
                    final var registered = brokerService.getRegisteredBroker();
                    registered.iterator().forEachRemaining(elem -> {
                        final var asJsonObject = (JSONObject) elem;
                        final var brokerId = asJsonObject.getAsString("brokerId");

                        CompletableFuture.runAsync(() -> {
                            try {
                                brokerClient.updateAtBroker(brokerId);
                            } catch (IOException e) {
                                log.warn(String.format("Error while updating at broker: %s", e.getMessage()), e);
                            }
                        });
                    });

                    jsonObject.put("connectorResponse", "Successfully updated the connector "
                            + "description of the configuration model");
                    response = ResponseEntity.ok(jsonObject.toJSONString());
                } else {
                    jsonObject.put("connectorResponse", "Failed to update the connector. "
                            + "The configuration model is not valid");
                    response = ResponseEntity.badRequest().body(jsonObject.toJSONString());
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                jsonObject.put("connectorResponse", "Failed to send the new configuration to the client");
                response = ResponseEntity.badRequest().body(jsonObject.toJSONString());
            }
        } else {
            response = ResponseEntity.badRequest().body("Could not update the connector");
        }

        return response;
    }

    /**
     * This method deletes a connector from the configuration model.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> deleteConnector() {
        log.info(">> DELETE /connector");
        ResponseEntity<String> response;

        if (configModelService.getConfigModel().getConnectorDescription() != null) {
            final var configModelImpl = (ConfigurationModelImpl) configModelService.getConfigModel();
            configModelImpl.setConnectorDescription(null);
            configModelService.saveState();

            response = ResponseEntity.ok(Utility.jsonMessage("message", "Successfully deleted the connector"));
        } else {
            response = ResponseEntity.badRequest().body("Could not delete the connector");
        }

        return response;
    }
}
