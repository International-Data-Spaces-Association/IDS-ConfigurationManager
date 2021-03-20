package de.fraunhofer.isst.configmanager.controller;

import de.fraunhofer.iais.eis.BaseConnector;
import de.fraunhofer.iais.eis.ConfigurationModelImpl;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConnectorService;
import de.fraunhofer.isst.configmanager.util.Utility;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * The controller class implements the ConnectorUIApi and offers the possibilities to manage
 * the connectors in the configuration manager.
 */
@RestController
@RequestMapping("/api/ui")
@Slf4j
@Tag(name = "Connector Management", description = "Endpoints for managing the connectors in the configuration manager")
public class ConnectorUIController implements ConnectorUIApi {
    private final ConnectorService connectorService;
    private final ConfigModelService configModelService;
    private final Serializer serializer;
    private final DefaultConnectorClient client;

    @Autowired
    public ConnectorUIController(ConnectorService connectorService,
                                 ConfigModelService configModelService,
                                 Serializer serializer,
                                 DefaultConnectorClient client) {
        this.configModelService = configModelService;
        this.connectorService = connectorService;
        this.serializer = serializer;
        this.client = client;
    }

    /**
     * This methods returns the connector description from the configuration model.
     *
     * @return the connector description
     */
    @Override
    public ResponseEntity<String> getConnector() {
        log.info(">> GET /connector");

        Connector connector = configModelService.getConfigModel().getConnectorDescription();
        if (connector != null) {
            try {
                return new ResponseEntity<>(serializer.serialize(connector), HttpStatus.OK);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * This method returns as response the base connector in JSON format.
     *
     * @return as response the connector in JSON format
     */
    @Override
    public ResponseEntity<String> getConnectorJson() {
        log.info(">> GET /connector/json");

        BaseConnector baseConnector = (BaseConnector) configModelService.getConfigModel().getConnectorDescription();

        JSONObject baseConnectorJson = new JSONObject();
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
     * This method creates a connector description for the configuration model with the given parameters.
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
    public ResponseEntity<String> createConnector(String title, String description, String endpointAccessURL,
                                                  String version, String curator, String maintainer,
                                                  String inboundModelVersion, String outboundModelVersion) {
        log.info(">> POST /connector");

        BaseConnector baseConnector = connectorService.createConnector(title, description, endpointAccessURL, version,
                curator, maintainer, inboundModelVersion, outboundModelVersion);

        var configModelImpl = (ConfigurationModelImpl) configModelService.getConfigModel();
        configModelImpl.setConnectorDescription(baseConnector);
        configModelService.saveState();

        return ResponseEntity.ok(Utility.jsonMessage("message", "Successfully created a new connector with the id: " +
                baseConnector.getId().toString()));
    }

    /**
     * This method updates the connector description from the configuration model with the given parameters.
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
    public ResponseEntity<String> updateConnector(String title, String description, String endpointAccessURL,
                                                  String version, String curator, String maintainer,
                                                  String inboundModelVersion, String outboundModelVersion) {
        log.info(">> PUT /connector title: " + title + " description: " + " endpointAccessURL: " + endpointAccessURL
                + " version: " + version + " curator: " + curator + " maintainer: " + maintainer + " inboundModelVersion: "
                + inboundModelVersion + " outboundModelVersion: " + outboundModelVersion);

        boolean updated = connectorService.updateConnector(title, description, endpointAccessURL, version,
                curator, maintainer, inboundModelVersion, outboundModelVersion);
        var jsonObject = new JSONObject();
        if (updated) {
            jsonObject.put("message", "Successfully updated the connector");
            ConfigurationModelImpl configurationModel = (ConfigurationModelImpl) configModelService.getConfigModel();
            if (configurationModel.getAppRoute() != null) {
                configurationModel.setAppRoute(Util.asList());
            }
            try {
                var valid = client.sendConfiguration(serializer.serialize(configurationModel));
                if (valid) {
                    jsonObject.put("connectorResponse", "Successfully updated the connector " +
                            "description of the configuration model");
                    return ResponseEntity.ok(jsonObject.toJSONString());
                } else {
                    jsonObject.put("connectorResponse", "Failed to update the connector. " +
                            "The configuration model is not valid");
                    return ResponseEntity.badRequest().body(jsonObject.toJSONString());
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                jsonObject.put("connectorResponse", "Failed to send the new configuration to the client");
                return ResponseEntity.badRequest().body(jsonObject.toJSONString());
            }
        } else {
            return ResponseEntity.badRequest().body("Could not update the connector");
        }
    }

    /**
     * This method deletes a connector from the configuration model
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> deleteConnector() {
        log.info(">> DELETE /connector");

        if (configModelService.getConfigModel().getConnectorDescription() != null) {
            var configModelImpl = (ConfigurationModelImpl) configModelService.getConfigModel();
            configModelImpl.setConnectorDescription(null);
            configModelService.saveState();

            return ResponseEntity.ok(Utility.jsonMessage("message", "Successfully deleted the connector"));
        } else {
            return ResponseEntity.badRequest().body("Could not delete the connector");
        }
    }
}
