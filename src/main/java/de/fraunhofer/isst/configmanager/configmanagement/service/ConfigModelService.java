package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.BaseConnectorBuilder;
import de.fraunhofer.iais.eis.BasicAuthenticationBuilder;
import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.ConfigurationModelBuilder;
import de.fraunhofer.iais.eis.ConfigurationModelImpl;
import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.iais.eis.ConnectorStatus;
import de.fraunhofer.iais.eis.LogLevel;
import de.fraunhofer.iais.eis.ProxyBuilder;
import de.fraunhofer.iais.eis.ProxyImpl;
import de.fraunhofer.iais.eis.SecurityProfile;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.configmanagement.entities.config.ConfigModelObject;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configlists.ConfigModelRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for the configuration model.
 */
@Service
@Slf4j
public class ConfigModelService {

    private transient final ConfigModelRepository configModelRepository;
    @Getter
    private ConfigModelObject configModelObject;

    @Autowired
    public ConfigModelService(final ConfigModelRepository configModelRepository,
                              final DefaultConnectorClient client) {
        this.configModelRepository = configModelRepository;
        log.warn("---- Initial StartUp! Trying to get current Configuration from Connector!");
        try {
            updateConfigModel(client.getConfiguration());
            log.info("---- Received configuration from running Connector!");
        } catch (IOException e) {
            log.warn("---- Could not get Configmodel from Connector! Using old Config if " +
                    "available! " +
                    "Error establishing connection to connector: " + e.getMessage());

            if (configModelRepository.findAll().size() > 0) {
                configModelObject = configModelRepository.findAll().get(0);
            } else {
                log.warn("---- Connector Config not reachable and no old config available! Using " +
                        "new placeholder Config.");
                createConfigModel(
                        "NO_LOGGING",
                        "TEST_DEPLOYMENT",
                        "http://truststore",
                        "password",
                        "http://keystore",
                        "password"
                );
            }
        }
    }

//    private void getOfferedResources() {
//
//        BaseConnector baseConnector = null;
//        try {
//            baseConnector = client.getSelfDeclaration();
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//        }
//        if (baseConnector != null && baseConnector.getResourceCatalog() != null) {
//            for (ResourceCatalog resourceCatalog : baseConnector.getResourceCatalog()) {
//                if (resourceCatalog.getOfferedResource() != null) {
//                    resources.addAll(resourceCatalog.getOfferedResource());
//                }
//            }
//        }
//    }

    /**
     * The method creates a configuration model with the given parameters.
     *
     * @param loglevel            loglevel of the configuration model
     * @param connectorDeployMode deploy mode of the connector
     * @param trustStore          the certificate
     * @param trustStorePassword  password for the trust store
     * @param keyStore            repository for certificates
     * @param keyStorePassword    password for the key store
     * @return configurationmodel
     */
    public ConfigurationModel createConfigModel(final String loglevel,
                                                final String connectorDeployMode,
                                                final String trustStore,
                                                final String trustStorePassword,
                                                final String keyStore,
                                                final String keyStorePassword) {

        final var connector = new BaseConnectorBuilder()
                ._inboundModelVersion_(new ArrayList<>(List.of("3.1.0")))
                ._outboundModelVersion_("3.1.0")
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._maintainer_(URI.create("https://example.com"))
                ._curator_(URI.create("https://example.com"))
                .build();

        final var configurationModel = new ConfigurationModelBuilder()
                ._configurationModelLogLevel_(LogLevel.valueOf(loglevel))
                ._connectorDescription_(connector)
                ._connectorStatus_(ConnectorStatus.CONNECTOR_ONLINE)
                ._connectorDeployMode_(ConnectorDeployMode.valueOf(connectorDeployMode))
                ._trustStore_(URI.create(trustStore))
                ._trustStorePassword_(trustStorePassword)
                ._keyStore_(URI.create(keyStore))
                ._keyStorePassword_(keyStorePassword)
                .build();

        // The configuration model is added to the list of configuration models and then stored
        // in the database.
        configModelRepository.saveAndFlush(new ConfigModelObject(configurationModel));
        return configurationModel;
    }

    /**
     * The boolean method tries to update the given configuration model.
     *
     * @param configurationModel which is updated
     */
    public void updateConfigModel(final ConfigurationModel configurationModel) {

        configModelRepository.deleteAll();
        configModelObject =
                configModelRepository.saveAndFlush(new ConfigModelObject(configurationModel));
    }

    /**
     * @return true, if the state is saved
     */
    public boolean saveState() {
        configModelRepository.deleteAll();
        configModelObject = configModelRepository.saveAndFlush(configModelObject);
        return true;
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
     * @return true, if configuration model is updated
     */
    public boolean updateConfigurationModel(final String loglevel, final String connectorDeployMode,
                                            final String trustStore,
                                            final String trustStorePassword, final String keyStore,
                                            final String keyStorePassword, final String proxyUri,
                                            final ArrayList<URI> noProxyUriList,
                                            final String username, final String password) {

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
        return saveState();
    }

    /**
     * This method updates the proxy settings
     *
     * @param proxyUri        the uri of the proxy
     * @param noProxyUriList  list of no proxy uri's
     * @param username        username for the authentication
     * @param password        password for the authentication
     * @param configmodelImpl configuration model implementation
     */
    public void updateProxySettings(final String proxyUri, final ArrayList<URI> noProxyUriList,
                                    final String username, final String password,
                                    final ConfigurationModelImpl configmodelImpl) {
        if (proxyUri.equals("null")) {
            configmodelImpl.setConnectorProxy(null);
        } else {
            if (getConfigModelObject().getConfigurationModel().getConnectorProxy() == null) {
                final var proxy = new ProxyBuilder()
                        ._proxyURI_(URI.create(proxyUri))
                        ._noProxy_(noProxyUriList)
                        ._proxyAuthentication_(new BasicAuthenticationBuilder()
                                ._authUsername_(username)._authPassword_(password).build())
                        .build();
                configmodelImpl.setConnectorProxy(Util.asList(proxy));
            } else {
                final var proxyImpl =
                        (ProxyImpl) getConfigModelObject().getConfigurationModel().getConnectorProxy().get(0);

                proxyImpl.setProxyURI(URI.create(proxyUri));
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
