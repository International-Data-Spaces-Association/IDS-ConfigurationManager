/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.isst.configmanager.extensions.configuration.api.controller;

import de.fraunhofer.iais.eis.ConfigurationModelImpl;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.extensions.configuration.api.ConnectorConfigurationApi;
import de.fraunhofer.isst.configmanager.extensions.configuration.api.service.ConnectorConfigurationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
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
@Tag(name = "Extension: Connector Configuration")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ConnectorConfigurationController implements ConnectorConfigurationApi {

    transient Serializer serializer;
    transient ConnectorConfigurationService configModelService;


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
    public ResponseEntity<String> updateConnector(final String title,
                                                  final String description,
                                                  final URI endpointAccessURL,
                                                  final String version,
                                                  final URI curator,
                                                  final URI maintainer,
                                                  final String inboundModelVersion,
                                                  final String outboundModelVersion) {
        if (log.isInfoEnabled()) {
            log.info(">> PUT /connector title: " + title + " description: " + " endpointAccessURL: " + endpointAccessURL
                    + " version: " + version + " curator: " + curator + " maintainer: " + maintainer + " inboundModelVersion: "
                    + inboundModelVersion + " outboundModelVersion: " + outboundModelVersion);
        }

        ResponseEntity<String> response;

        final boolean updated = configModelService.updateConnector(title, description, endpointAccessURL,
                version,
                curator, maintainer, inboundModelVersion, outboundModelVersion);

        final var jsonObject = new JSONObject();

        if (updated) {
            jsonObject.put("message", "Successfully updated the connector");
            final var configurationModel = (ConfigurationModelImpl) configModelService.getConfigModel();

            if (configurationModel.getAppRoute() != null) {
                configurationModel.setAppRoute(Util.asList());
            }
        }
        response = ResponseEntity.ok(jsonObject.toJSONString());
        return response;
    }
}
