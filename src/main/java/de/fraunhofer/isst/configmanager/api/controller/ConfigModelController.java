package de.fraunhofer.isst.configmanager.api.controller;

import de.fraunhofer.iais.eis.ConfigurationModelImpl;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.api.ConfigModelApi;
import de.fraunhofer.isst.configmanager.api.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultConnectorClient;
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
import java.net.URI;
import java.util.ArrayList;

/**
 * The api class implements the ConfigModelApi and offers the possibilities to manage
 * the configuration model in the configuration manager.
 */
@Slf4j
@RestController
@RequestMapping("/api/ui")
@Tag(name = "Configmodel Management", description = "Endpoints for managing the configuration model")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ConfigModelController implements ConfigModelApi {

    transient Serializer serializer;
    transient ConfigModelService configModelService;
    transient DefaultConnectorClient client;

    @Autowired
    public ConfigModelController(final Serializer serializer,
                                 final ConfigModelService configModelService,
                                 final DefaultConnectorClient client) {
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
    public ResponseEntity<String> updateConfigModel(final String loglevel,
                                                    final String connectorDeployMode,
                                                    final String trustStore,
                                                    final String trustStorePassword,
                                                    final String keyStore,
                                                    final String keyStorePassword,
                                                    final URI proxyUri,
                                                    final ArrayList<URI> noProxyUriList,
                                                    final String username,
                                                    final String password) {

        log.info(">> PUT /configmodel loglevel: " + loglevel + " connectorDeployMode: " + connectorDeployMode + " trustStore: " + trustStore
                + " keyStore: " + keyStore + " proxyUri: " + proxyUri + " username: " + username);

        ResponseEntity<String> response;

        final var result = configModelService.updateConfigurationModel(loglevel, connectorDeployMode,
                trustStore,
                trustStorePassword, keyStore, keyStorePassword, proxyUri, noProxyUriList,
                username, password);

        if (result) {
            final var jsonObject = new JSONObject();
            jsonObject.put("message", "Successfully updated the configuration model in the "
                    + "configuration manager");
            try {
                // The configuration model is sent to the client without the app routes at this
                // point, because of the different infomodels.
                final var configurationModel = (ConfigurationModelImpl) configModelService.getConfigModel();
                configurationModel.setAppRoute(Util.asList());

                final var valid = client.sendConfiguration(serializer.serialize(configurationModel));

                if (valid) {
                    jsonObject.put("connectorResponse", "Successfully updated the configuration model at the client");
                    response = ResponseEntity.ok(jsonObject.toJSONString());
                } else {
                    jsonObject.put("connectorResponse", "Failed to update the configuration model at the client");
                    response = ResponseEntity.badRequest().body(jsonObject.toJSONString());
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update the configuration model");
        }

        return response;
    }

    /**
     * This method returns the current configuration model.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getConfigModel() {
        log.info(">> GET /configmodel");
        ResponseEntity<String> response;

        try {
            response = ResponseEntity.ok(serializer.serialize(configModelService.getConfigModel()));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            response = ResponseEntity.badRequest().body("Could not determine the configuration model");
        }

        return response;
    }
}
