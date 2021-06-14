package de.fraunhofer.isst.configmanager.configuration.api.controller;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.configuration.api.ConfigModelApi;
import de.fraunhofer.isst.configmanager.configuration.api.service.ConfigModelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "Configmodel Management", description = "Endpoints for managing the configuration model")
public class ConfigModelController implements ConfigModelApi {

    transient Serializer serializer;
    transient ConfigModelService configModelService;

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
        if (log.isInfoEnabled()) {
            log.info(">> PUT /configmodel loglevel: " + loglevel + " connectorDeployMode: " + connectorDeployMode + " trustStore: " + trustStore
                    + " keyStore: " + keyStore + " proxyUri: " + proxyUri + " username: " + username);
        }

        ResponseEntity<String> response;

        configModelService.updateConfigurationModel(loglevel, connectorDeployMode,
                trustStore,
                trustStorePassword, keyStore, keyStorePassword, proxyUri, noProxyUriList,
                username, password);

        response = ResponseEntity.ok("Successfully updated the configuration model!");

        return response;
    }

    /**
     * This method returns the current configuration model.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getConfigModel() {
        if (log.isInfoEnabled()) {
            log.info(">> GET /configmodel");
        }

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
