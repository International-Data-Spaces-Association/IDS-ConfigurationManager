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
package de.fraunhofer.isst.configmanager.extensions.configuration.api.service;

import de.fraunhofer.iais.eis.ConfigurationModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;

/**
 * Service class for the configuration model.
 */
@Service
@Transactional
public class ConfigurationService {
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
     */
    public void updateConfigurationModel(final String loglevel,
                                            final String connectorDeployMode,
                                            final String trustStore,
                                            final String trustStorePassword,
                                            final String keyStore,
                                            final String keyStorePassword,
                                            final URI proxyUri,
                                            final ArrayList<URI> noProxyUriList,
                                            final String username,
                                            final String password) {
        //TODO: Save in DB
    }

    /**
     * @return configuration model
     */
    public ConfigurationModel getConfigModel() {
        //TODO: Get from DB
        return null;
    }

    /**
     * @param title                title of the connector
     * @param description          description of the connector
     * @param endpointAccessURL    access url of the endpoint
     * @param version              version of the connector
     * @param curator              curator of the connector
     * @param maintainer           maintainer of the connector
     * @param inboundModelVersion  inbound model version of the connector
     * @param outboundModelVersion outbound model version of the connector
     * @return true, if connector is updated
     */
    public boolean updateConnector(final String title,
                                   final String description,
                                   final URI endpointAccessURL,
                                   final String version,
                                   final URI curator,
                                   final URI maintainer,
                                   final String inboundModelVersion,
                                   final String outboundModelVersion) {

        //TODO: Save in DB
        return true;
    }
}
