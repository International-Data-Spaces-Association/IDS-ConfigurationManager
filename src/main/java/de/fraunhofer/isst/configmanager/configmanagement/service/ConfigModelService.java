package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.configmanagement.entities.config.ConfigModelObject;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.ConfigModelList;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.ConfigModelRepository;
import de.fraunhofer.isst.configmanager.configmanagement.service.listeners.ConfigModelListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ConfigModelService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigModelService.class);
    private static final Serializer SERIALIZER = new Serializer();

    private final ConfigModelRepository configModelRepository;
    private ConfigModelList configModelList;
    private final List<ConfigModelListener> listeners;
    private final DefaultConnectorClient client;

    @Autowired
    public ConfigModelService(ConfigModelRepository configModelRepository, List<ConfigModelListener> listeners,
                              DefaultConnectorClient client) {
        this.configModelRepository = configModelRepository;
        this.listeners = listeners;
        this.client = client;
        this.configModelList = new ConfigModelList();
        if (configModelRepository.findAll().isEmpty()) {
            LOGGER.warn("No configuration found! Trying to get current Configuration from Connector!");
            try {
                var configmodel = client.getConfiguration();
                this.configModelList.getConfigModelObjects().add(new ConfigModelObject(configmodel));
                configModelList = configModelRepository.saveAndFlush(configModelList);
                LOGGER.info("Received configuration from running Connector!");
            } catch (IOException e) {
                LOGGER.warn("Could not get Configmodel from Connector! Using placeholder!");
                createConfigModel(
                        "NO_LOGGING",
                        "CONNECTOR_OFFLINE",
                        "TEST_DEPLOYMENT",
                        "http://t",
                        "tPassword",
                        "http://k",
                        "kPassword"
                );
            }
        } else {
            LOGGER.info("Reloading old configuration");
            configModelList = configModelRepository.findAll().get(0);
            try {
                var valid = client.sendConfiguration(SERIALIZER.serialize(getConfigModel()));
                if (!valid) {
                    LOGGER.warn("Old configuration is invalid, using Connectors configuration!");
                    var configmodel = client.getConfiguration();
                    updateConfigModel(configmodel);
                }
            } catch (IOException e) {
                LOGGER.warn("Could not get a valid ConfigurationModel and/or Connector is not reachable!");
            }
        }
    }

    /**
     * The method creates a configuration model with the given parameters.
     *
     * @param loglevel            loglevel of the configuration model
     * @param connectorStatus     status of the connector
     * @param connectorDeployMode deploy mode of the connector
     * @param trustStore          the certificate
     * @param keyStore            repository for certificates
     * @return configurationmodel
     */
    public ConfigurationModel createConfigModel(String loglevel, String connectorStatus, String connectorDeployMode,
                                                String trustStore, String trustStorePassword, String keyStore,
                                                String keyStorePassword) {

        BaseConnector connector = new BaseConnectorBuilder()
                ._inboundModelVersion_(new ArrayList<>(List.of("3.1.0")))
                ._outboundModelVersion_("3.1.0")
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._maintainer_(URI.create("https://example.com"))
                ._curator_(URI.create("https://example.com"))
                .build();

        ConfigurationModel configurationModel = new ConfigurationModelBuilder()
                ._configurationModelLogLevel_(LogLevel.valueOf(loglevel))
                ._connectorDescription_(connector)
                ._connectorStatus_(ConnectorStatus.valueOf(connectorStatus))
                ._connectorDeployMode_(ConnectorDeployMode.valueOf(connectorDeployMode))
                ._trustStore_(URI.create(trustStore))
                ._trustStorePassword_(trustStorePassword)
                ._keyStore_(URI.create(keyStore))
                ._keyStorePassword_(keyStorePassword)
                .build();

        // The configuration model is added to the list of configuration models and then stored in the database.
        this.configModelList.getConfigModelObjects().add(new ConfigModelObject(configurationModel));
        configModelList = configModelRepository.saveAndFlush(configModelList);
        return configurationModel;
    }

    /**
     * @return the current configuration model
     */
    public ConfigurationModel getConfigModel() {
        return configModelList.getCurrentConfigurationModel();
    }

    /**
     * The boolean method tries to update the given configuration model.
     *
     * @param configurationModel which is updated
     * @return true, if configuration model is updated
     */
    public boolean updateConfigModel(ConfigurationModel configurationModel) {
        for (int i = 0; i < configModelList.getConfigModelObjects().size(); i++) {
            if (configModelList.getConfigModelObjects().get(i).getConfigurationModel().getId()
                    .equals(configurationModel.getId())) {
                configModelList.getConfigModelObjects().get(i).setConfigurationModel(configurationModel);
                configModelList = configModelRepository.saveAndFlush(configModelList);
                return true;
            }
        }
        return false;
    }

    /**
     * The method deletes the configuration model with the given id.
     *
     * @param configmodelId id of the configuration model
     * @return true, if configuration model is deleted
     */
    public boolean deleteConfigModel(URI configmodelId) {

        for (var configModelObject : configModelList.getConfigModelObjects()) {
            if (configModelObject.getConfigurationModel().getId().equals(configmodelId)) {

                boolean success = configModelList.getConfigModelObjects().remove(configModelObject);
                configModelList = configModelRepository.saveAndFlush(configModelList);
                return success;
            }
        }
        return false;
    }

    /**
     * @return true, if the state is saved
     */
    public boolean saveState() {
        LOGGER.info("ConfigList before: " + configModelList);
        configModelList = configModelRepository.saveAndFlush(configModelList);
        LOGGER.info("ConfigList after: " + configModelList);
        return true;
    }

    /**
     * The method saves the current state and sends the configuration to the target connector.
     *
     * @return true, if state is saved and send the configuration to the target connector
     */
    public boolean saveStateAndSend() {
        var accepted = false;
        try {
            var configmodel = getConfigModel();
            accepted = client.sendConfiguration(SERIALIZER.serialize(configmodel));
            if (!accepted) {
                LOGGER.warn("New configuration was not accepted! Trying to get valid config from connector!");
                if (!updateConfigModel(client.getConfiguration())) {
                    LOGGER.warn("Could not roll back update of configuration!");
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Could not send configuration to Client!");
        }
        configModelList = configModelRepository.saveAndFlush(configModelList);
        return accepted;
    }

    /**
     * This method updates the configuration model with the given parameters.
     *
     * @param loglevel            logging level of the configuration model
     * @param connectorStatus     connector status of the configuration model
     * @param connectorDeployMode connector deploy mode of the configuration model
     * @param trustStore          trust store of the configuration model
     * @param trustStorePassword  password of the trust store
     * @param keyStore            key store of the configuration model
     * @param keyStorePassword    password of the key store
     * @return true, if configuration model is updated
     */
    public boolean updateConfigurationModel(String loglevel, String connectorStatus, String connectorDeployMode,
                                            String trustStore, String trustStorePassword, String keyStore,
                                            String keyStorePassword) {

        ConfigurationModelImpl configModelImpl = (ConfigurationModelImpl) getConfigModel();
        if (loglevel != null) {
            configModelImpl.setConfigurationModelLogLevel(LogLevel.valueOf(loglevel));
        }
        if (connectorStatus != null) {
            configModelImpl.setConnectorStatus(ConnectorStatus.valueOf(connectorStatus));
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
        return saveState();
    }

    /**
     * This method updates the proxy settings from the configuration model with the given parameters.
     *
     * @param proxyUri       uri of the proxy
     * @param noProxyUriList list of no proxy uri's
     * @param username       username for the authentication
     * @param password       password for the authentication
     * @param proxyImpl      proxy implementation
     */
    public void updateConfigurationModelProxy(String proxyUri, ArrayList<URI> noProxyUriList,
                                              String username, String password, ProxyImpl proxyImpl) {
        if (proxyUri!=null && !proxyUri.isEmpty()) {
            proxyImpl.setProxyURI(URI.create(proxyUri));
        }
        if (noProxyUriList != null) {
            proxyImpl.setNoProxy(noProxyUriList);
        }
        if (username!=null && !username.isEmpty()) {
            proxyImpl.setProxyAuthentication(new BasicAuthenticationBuilder(proxyImpl.getProxyAuthentication().getId())
                    ._authUsername_(username)._authPassword_(proxyImpl.getProxyAuthentication().getAuthPassword()).build());
        }
        if (password!=null && !password.isEmpty()) {
            proxyImpl.setProxyAuthentication(new BasicAuthenticationBuilder(proxyImpl.getProxyAuthentication().getId())
                    ._authUsername_(proxyImpl.getProxyAuthentication().getAuthUsername())._authPassword_(password).build());
        }
        saveState();
    }
}
