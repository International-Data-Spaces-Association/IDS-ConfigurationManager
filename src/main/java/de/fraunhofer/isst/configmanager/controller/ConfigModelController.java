package de.fraunhofer.isst.configmanager.controller;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.ConfigurationModelImpl;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConfigModelService;
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
import java.util.ArrayList;

/**
 * The controller class implements the ConfigModelApi and offers the possibilities to manage
 * the configuration model in the configuration manager.
 */
@RestController
@RequestMapping("/api/ui")
@Slf4j
@Tag(name = "ConfigModel Management", description = "Endpoints for managing the configuration model")
public class ConfigModelController implements ConfigModelApi {
    private final Serializer serializer;
    private final ConfigModelService configModelService;
    private final DefaultConnectorClient client;

    @Autowired
    public ConfigModelController(Serializer serializer, ConfigModelService configModelService,
                                 DefaultConnectorClient client) {
        this.serializer = serializer;
        this.configModelService = configModelService;
        this.client = client;
    }

    /**
     * This method updates the configuration model with the given parameters.
     *
     * @param loglevel            logging level of the configuration model
     * @param connectorDeployMode connector deploy mode of the configuration model
     * @param trustStore          trust store of the configuration model
     * @param trustStorePassword  password of the trust store
     * @param keyStore            key store of the configuration model
     * @param keyStorePassword    password of the key store
     * @param proxyUri            the uri of the proxy
     * @param noProxyUriList      list of no proxy uri's
     * @param username            username for the authentication
     * @param password            password for the authentication
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateConfigModel(String loglevel, String connectorDeployMode,
                                                    String trustStore, String trustStorePassword, String keyStore,
                                                    String keyStorePassword, String proxyUri, ArrayList<URI> noProxyUriList,
                                                    String username, String password) {

        log.info(">> PUT /configmodel loglevel: " + loglevel + " connectorDeployMode: " + connectorDeployMode + " trustStore: " + trustStore
        + " trustStorePassword: " + trustStorePassword + " keyStore: " + keyStore + " keyStorePassword: " + keyStorePassword + " proxyUri: " + proxyUri
        + " username: " + username + " password: " + password);

        var result = configModelService.updateConfigurationModel(loglevel, connectorDeployMode, trustStore,
                trustStorePassword, keyStore, keyStorePassword, proxyUri, noProxyUriList, username, password);
        if (result) {
            var jsonObject = new JSONObject();
            jsonObject.put("message", "Successfully updated the configuration model in the configuration manager");
            try {
                // The configuration model is sent to the client without the app routes at this point,
                // because of the different infomodels.
                ConfigurationModelImpl configurationModel = (ConfigurationModelImpl) configModelService.getConfigModel();
                configurationModel.setAppRoute(Util.asList());
                var valid = client.sendConfiguration(serializer.serialize(configurationModel));
                if (valid) {
                    jsonObject.put("connectorResponse", "Successfully updated the configuration model at the client");
                    return ResponseEntity.ok(jsonObject.toJSONString());
                } else {
                    jsonObject.put("connectorResponse", "Failed to update the configuration model at the client");
                    return ResponseEntity.badRequest().body(jsonObject.toJSONString());
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems while sending configuration" +
                        " to the client");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update the configuration model");
        }
    }

    /**
     * This method returns the current configuration model.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getConfigModel() {
        log.info(">> GET /configmodel");
        try {
            return ResponseEntity.ok(serializer.serialize(configModelService.getConfigModel()));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body("Could not determine the configuration model");
        }
    }

    /**
     * This method returns the current configuration model as JSON string
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getConfigModelJson() {
        log.info(">> GET /configmodel/json");

        ConfigurationModel configurationModel = configModelService.getConfigModel();

        JSONObject configModelJson = new JSONObject();
        configModelJson.put("loglevel", configurationModel.getConfigurationModelLogLevel());
        configModelJson.put("connectorStatus", configurationModel.getConnectorStatus());
        configModelJson.put("connectorDeployMode", configurationModel.getConnectorDeployMode());
        configModelJson.put("trustStore", configurationModel.getTrustStore().toString());
        configModelJson.put("trustStorePassword", configurationModel.getTrustStorePassword());
        configModelJson.put("keyStore", configurationModel.getKeyStore().toString());
        configModelJson.put("keyStorePassword", configurationModel.getKeyStorePassword());

        return ResponseEntity.ok(configModelJson.toJSONString());
    }
}
