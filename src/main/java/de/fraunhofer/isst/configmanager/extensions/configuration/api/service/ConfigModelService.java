package de.fraunhofer.isst.configmanager.extensions.configuration.api.service;

import de.fraunhofer.iais.eis.BasicAuthenticationBuilder;
import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.ConfigurationModelImpl;
import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.iais.eis.LogLevel;
import de.fraunhofer.iais.eis.ProxyBuilder;
import de.fraunhofer.iais.eis.ProxyImpl;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.data.entities.ConfigModelObject;
import de.fraunhofer.isst.configmanager.data.repositories.ConfigModelRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;

/**
 * Service class for the configuration model.
 */
@Slf4j
@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigModelService {

    final transient ConfigModelRepository configModelRepository;

    @Getter
    ConfigModelObject configModelObject;

    public void saveState() {
        configModelRepository.deleteAll();
        configModelObject = configModelRepository.saveAndFlush(configModelObject);
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

        final var configModelImpl =
                (ConfigurationModelImpl) getConfigModelObject().getConfigurationModel();
        if (loglevel != null) {
            configModelImpl.setConfigurationModelLogLevel(LogLevel.valueOf(loglevel));
        }
        if (connectorDeployMode != null) {
            configModelImpl.setConnectorDeployMode(ConnectorDeployMode.valueOf(connectorDeployMode));
        }
        if (trustStore != null) {
            configModelImpl.setTrustStore(URI.create(trustStore));
        }
        if (trustStorePassword != null) {
            configModelImpl.setTrustStorePassword(trustStorePassword);
        }
        if (keyStore != null) {
            configModelImpl.setKeyStore(URI.create(keyStore));
        }
        if (keyStorePassword != null) {
            configModelImpl.setKeyStorePassword(keyStorePassword);
        }
        //Set Default CONNECTOR ONLINE
        configModelImpl.setConnectorStatus(configModelImpl.getConnectorStatus());

        // Update configuration model proxy
        if (proxyUri != null) {
            updateProxySettings(proxyUri, noProxyUriList, username, password, configModelImpl);
        }
        saveState();
    }

    /**
     * This method updates the proxy settings.
     *
     * @param proxyUri        the uri of the proxy
     * @param noProxyUriList  list of no proxy uri's
     * @param username        username for the authentication
     * @param password        password for the authentication
     * @param configmodelImpl configuration model implementation
     */
    public void updateProxySettings(final URI proxyUri, final ArrayList<URI> noProxyUriList,
                                    final String username, final String password,
                                    final ConfigurationModelImpl configmodelImpl) {
        if ("null".equals(proxyUri.toString())) {
            configmodelImpl.setConnectorProxy(null);
        } else {
            if (getConfigModelObject().getConfigurationModel().getConnectorProxy() == null) {
                final var proxy = new ProxyBuilder()
                        ._proxyURI_(proxyUri)
                        ._noProxy_(noProxyUriList)
                        ._proxyAuthentication_(new BasicAuthenticationBuilder()
                                ._authUsername_(username)._authPassword_(password).build())
                        .build();
                configmodelImpl.setConnectorProxy(Util.asList(proxy));
            } else {
                final var proxyImpl =
                        (ProxyImpl) getConfigModelObject().getConfigurationModel().getConnectorProxy().get(0);

                proxyImpl.setProxyURI(proxyUri);
                if (noProxyUriList != null) {
                    proxyImpl.setNoProxy(noProxyUriList);
                }
                if (username != null && !username.equals("null")) {

                    proxyImpl.setProxyAuthentication(new BasicAuthenticationBuilder()._authUsername_(username)
                            ._authPassword_(password).build());
                }
                if (username != null && username.equals("null")
                        && password != null && password.equals("null")) {
                    proxyImpl.setProxyAuthentication(null);
                }
                if (password != null && !password.equals("null")) {

                    proxyImpl.setProxyAuthentication(new BasicAuthenticationBuilder()._authUsername_(username)
                            ._authPassword_(password).build());
                }
            }
        }
    }

    /**
     * @return configuration model
     */
    public ConfigurationModel getConfigModel() {
        return getConfigModelObject().getConfigurationModel();
    }
}
