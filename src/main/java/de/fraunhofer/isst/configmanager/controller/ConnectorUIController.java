package de.fraunhofer.isst.configmanager.controller;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConnectorService;
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
 * The controller class implements the ConnectorUIApi and offers the possibilities to manage
 * the connectors in the configurationmanager.
 */
@RestController
@RequestMapping("/api/ui")
@Tag(name = "Connector Management", description = "Endpoints for managing the connectors in the configuration manager")
public class ConnectorUIController implements ConnectorUIApi {

    private final static Logger logger = LoggerFactory.getLogger(ConnectorUIController.class);

    private final ConnectorService connectorService;
    private final ConfigModelService configModelService;
    private final Serializer serializer;

    @Autowired
    public ConnectorUIController(ConnectorService connectorService, ConfigModelService configModelService,
                                 Serializer serializer) {
        this.configModelService = configModelService;
        this.connectorService = connectorService;
        this.serializer = serializer;
    }

    /**
     * This methods returns the connector description from the configuration model.
     *
     * @return the connector description
     */
    @Override
    public ResponseEntity<String> getConnector() {
        Connector connector = configModelService.getConfigModel().getConnectorDescription();
        if (connector != null) {
            try {
                return new ResponseEntity<>(serializer.serialize(connector), HttpStatus.OK);
            } catch (IOException e) {
                logger.error(e.getMessage());
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

        BaseConnector baseConnector = connectorService.createConnector(title, description, endpointAccessURL, version,
                curator, maintainer, inboundModelVersion, outboundModelVersion);

        var configModelImpl = (ConfigurationModelImpl) configModelService.getConfigModel();
        configModelImpl.setConnectorDescription(baseConnector);
        configModelService.saveState();

        var jsonObject = new JSONObject();
        jsonObject.put("message", "Successfully created a new connector with the id: " +
                baseConnector.getId().toString());
        return ResponseEntity.ok(jsonObject.toJSONString());
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


        var connector = (BaseConnectorImpl) configModelService.getConfigModel()
                .getConnectorDescription();
        if (title != null) {
            connector.setTitle(Util.asList(new TypedLiteral(title)));
        }
        if (description != null) {
            connector.setDescription(Util.asList(new TypedLiteral(description)));
        }
        if (endpointAccessURL != null) {
            connector.setHasEndpoint(Util.asList(new ConnectorEndpointBuilder()._accessURL_(URI.create(endpointAccessURL)).build()));
        }
        if (version != null) {
            connector.setVersion(version);
        }
        if (curator != null) {
            connector.setCurator(URI.create(curator));
        }
        if (maintainer != null) {
            connector.setMaintainer(URI.create(maintainer));
        }
        if (inboundModelVersion != null) {
            connector.setInboundModelVersion(Util.asList(inboundModelVersion));
        }
        if (outboundModelVersion != null) {
            connector.setOutboundModelVersion(outboundModelVersion);
        }
        connector.setSecurityProfile(SecurityProfile.BASE_SECURITY_PROFILE);

        var configModelImpl = (ConfigurationModelImpl) configModelService.getConfigModel();
        configModelImpl.setConnectorDescription(connector);
        configModelService.saveState();

        var jsonObject = new JSONObject();
        jsonObject.put("message", "Successfully updated the connector with the id: " +
                connector.getId().toString());
        return ResponseEntity.ok(jsonObject.toJSONString());
    }

    /**
     * This method deletes a connector from the configuration model
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> deleteConnector() {

        if (configModelService.getConfigModel().getConnectorDescription() != null) {
            var configModelImpl = (ConfigurationModelImpl) configModelService.getConfigModel();
            configModelImpl.setConnectorDescription(null);
            configModelService.saveState();

            var jsonObject = new JSONObject();
            jsonObject.put("message", "Successfully deleted the connector");
            return ResponseEntity.ok(jsonObject.toJSONString());
        } else {
            return ResponseEntity.badRequest().body("Could not delete the connector");
        }
    }
}
