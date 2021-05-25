package de.fraunhofer.isst.configmanager.api.service;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.data.entities.ConfigModelObject;
import de.fraunhofer.isst.configmanager.data.repositories.ConfigModelRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service class for the configuration model.
 */
@Slf4j
@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigModelService {

    final transient ConfigModelRepository configModelRepository;


    @Getter
    ConfigModelObject configModelObject;

    @Autowired
    public ConfigModelService(final ConfigModelRepository configModelRepository,
                              final DefaultConnectorClient client,
                              final  @Value("${dataspace.connector.connectionattemps}") Integer connectionAttemps) {
        this.configModelRepository = configModelRepository;
        if (log.isInfoEnabled()) {
            log.info("---- [ConfigModelService] ConfigManager StartUp! Trying to get current Configuration from Connector!");
        }
        try {
            getConnectorConfig(client, connectionAttemps);

            if (log.isInfoEnabled()) {
                log.info("---- [ConfigModelService] Received configuration from running Connector!");
            }
        } catch (InterruptedException e) {
            if (log.isInfoEnabled()) {
                log.info("---- [ConfigModelService] Could not get Configmodel from Connector! Using old Config if available!");
            }

            if (!configModelRepository.findAll().isEmpty()) {
                configModelObject = configModelRepository.findAll().get(0);
            } else {
                if (log.isInfoEnabled()) {
                    log.info("---- [ConfigModelService] Connector Config not reachable and no old config available! Using new placeholder Config.");
                }

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

    private void getConnectorConfig(final DefaultConnectorClient client,
                                    final Integer connectionAttemps) throws InterruptedException {
        ConfigurationModel connectorConfiguration = null;

        for (var i = 1; i <= connectionAttemps; i++) {
            try {
                if (log.isInfoEnabled()) {
                    log.info("---- [ConfigModelService] Try to reach the connector: " + i + "/" + connectionAttemps);
                }
                connectorConfiguration = client.getConfiguration();
                updateConfigModel(connectorConfiguration);
                break;
            } catch (IOException e) {
                if (i < connectionAttemps) {
                    if (log.isInfoEnabled()) {
                        log.info("---- [ConfigModelService] Could not reach the connector, starting next try in 5 seconds.");
                    }
                    TimeUnit.SECONDS.sleep(5);
                }
            }
        }

        if (connectorConfiguration == null) {
            throw new InterruptedException();
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
     */
    public void createConfigModel(final String loglevel,
                                  final String connectorDeployMode,
                                  final String trustStore,
                                  final String trustStorePassword,
                                  final String keyStore,
                                  final String keyStorePassword) {

        final var connectorEndpointBuilder = new ConnectorEndpointBuilder();
        connectorEndpointBuilder._accessURL_(URI.create("https://example.com"));

        final var connector = new BaseConnectorBuilder()
                ._inboundModelVersion_(new ArrayList<>(List.of("4.0.6")))
                ._outboundModelVersion_("4.0.6")
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._maintainer_(URI.create("https://example.com"))
                ._curator_(URI.create("https://example.com"))
                ._hasDefaultEndpoint_(connectorEndpointBuilder.build())
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

        configModelRepository.saveAndFlush(new ConfigModelObject(configurationModel));
    }

    /**
     * The boolean method tries to update the given configuration model.
     *
     * @param configurationModel which is updated
     */
    public void updateConfigModel(final ConfigurationModel configurationModel) {
        configModelRepository.deleteAll();
        configModelObject = configModelRepository.saveAndFlush(new ConfigModelObject(configurationModel));
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
    public boolean updateConfigurationModel(final String loglevel,
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
        return saveState();
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
