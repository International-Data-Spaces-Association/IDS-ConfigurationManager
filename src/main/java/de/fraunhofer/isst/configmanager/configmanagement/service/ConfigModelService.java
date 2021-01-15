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
import java.util.stream.Collectors;

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
                        "http://k"
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
                                                String trustStore, String keyStore) {

        ConfigurationModel configurationModel = new ConfigurationModelBuilder()
                ._configurationModelLogLevel_(LogLevel.valueOf(loglevel))
                ._connectorStatus_(ConnectorStatus.valueOf(connectorStatus))
                ._connectorDeployMode_(ConnectorDeployMode.valueOf(connectorDeployMode))
                ._trustStore_(URI.create(trustStore))
                ._keyStore_(URI.create(keyStore))
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
        configModelList = configModelRepository.saveAndFlush(configModelList);
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
     * This method set the logging level in the configuration manager.
     *
     * @param loglevel is set in the configuration model
     */
    public void createConfigModelLogLevel(String loglevel) {
        var configModel = (ConfigurationModelImpl) getConfigModel();
        LogLevel logLevel = LogLevel.valueOf(loglevel);
        configModel.setConfigurationModelLogLevel(logLevel);
        saveState();
    }

    /**
     * This method set the connector status in the configuration model.
     *
     * @param connectorstatus is set in the configuration model
     */
    public void createConfigModelConnectorStatus(String connectorstatus) {
        var configModel = (ConfigurationModelImpl) getConfigModel();
        ConnectorStatus connectorStatus = ConnectorStatus.valueOf(connectorstatus);
        configModel.setConnectorStatus(connectorStatus);
        saveState();
    }

    /**
     * This method set the connector deploymode in the configuration model.
     *
     * @param connectordeploymode ist set in the configuration model
     */
    public void createConfigModelConnectorDeployMode(String connectordeploymode) {
        var configModel = (ConfigurationModelImpl) getConfigModel();
        ConnectorDeployMode connectorDeployMode = ConnectorDeployMode.valueOf(connectordeploymode);
        configModel.setConnectorDeployMode(connectorDeployMode);
        saveState();
    }

    /**
     * The key store is set in the configuration model.
     *
     * @param keystore is set in te configuration model
     */
    public void createConfigModelKeystore(URI keystore) {
        var configModel = (ConfigurationModelImpl) getConfigModel();
        configModel.setKeyStore(keystore);
        saveState();
    }

    /**
     * This method deletes the key store from the configuration manager.
     * <p>
     * The method deletes the set key store
     */
    public void deleteConfigModelKeystore() {
        var configModel = (ConfigurationModelImpl) getConfigModel();
        configModel.setKeyStore(null);
        saveState();
    }

    /**
     * This method creates the trust store in the configuration model.
     *
     * @param truststore is set in the configuration model
     */
    public void createConfigModelTruststore(URI truststore) {
        var configModel = (ConfigurationModelImpl) getConfigModel();
        configModel.setTrustStore(truststore);
        saveState();
    }

    /**
     * This method deletes the trust store from the configuration manager.
     */
    public void deleteConfigModelTrustStore() {
        var configModel = (ConfigurationModelImpl) getConfigModel();
        configModel.setTrustStore(null);
        saveState();
    }

    /**
     * This method updates the given proxy.
     *
     * @param proxy which is updated
     */
    public void updateConfigModelProxy(Proxy proxy) {

        var proxies = configModelList.getCurrentConfigurationModel().getConnectorProxy()
                .stream().map(proxy1 -> proxy1.getId().equals(proxy.getId()) ? proxy : proxy1)
                .collect(Collectors.toCollection(ArrayList::new));

        var configModelImpl = (ConfigurationModelImpl) getConfigModel();
        configModelImpl.setConnectorProxy(proxies);
        saveState();
    }

    /**
     * This method creates a proxy in the configuration model
     *
     * @param proxy which is crea
     */
    public void createConfigModelProxy(Proxy proxy) {

        var configModelImpl = (ConfigurationModelImpl) getConfigModel();

        if (configModelImpl.getConnectorProxy() == null) {
            configModelImpl.setConnectorProxy(new ArrayList<>());
        }
        ArrayList<Proxy> proxies = (ArrayList<Proxy>) configModelImpl.getConnectorProxy();
        proxies.add(proxy);
        configModelImpl.setConnectorProxy(proxies);
        saveState();
    }

    /**
     * This method deletes the proxy in configuration model.
     *
     * @param proxyId id of the proxy
     * @return true, if proxy is deleted from the configuration model
     */
    public boolean deleteConfigModelProxy(URI proxyId) {

        if (getConfigModel().getConnectorProxy().removeIf(proxy -> proxy.getId().equals(proxyId))) {
            saveState();
            return true;
        }
        return false;
    }

    /**
     * This method updates the user authentication in the configuration manager.
     *
     * @param userAuthentication which is updated in the configuration model
     */
    public void updateConfigModelUserAuth(UserAuthentication userAuthentication) {

        var userAuthentications = configModelList.getCurrentConfigurationModel().getUserAuthentication()
                .stream().map(userAuthentication1 -> userAuthentication1.getId()
                        .equals(userAuthentication.getId()) ? userAuthentication : userAuthentication1)
                .collect(Collectors.toCollection(ArrayList::new));

        var configModelImpl = (ConfigurationModelImpl) getConfigModel();
        configModelImpl.setUserAuthentication(userAuthentications);
        saveState();
    }

    /**
     * This method creates a user authentication in the configuration model.
     *
     * @param userAuthentication which is created
     */
    public void createConfigModelUserAuth(UserAuthentication userAuthentication) {

        var configModelImpl = (ConfigurationModelImpl) getConfigModel();

        if (configModelImpl.getUserAuthentication() == null) {
            configModelImpl.setUserAuthentication(new ArrayList<>());
        }

        ArrayList<UserAuthentication> userAuths = (ArrayList<UserAuthentication>) configModelImpl.getUserAuthentication();
        userAuths.add(userAuthentication);
        configModelImpl.setUserAuthentication(userAuths);
        saveState();
    }

    /**
     * This method deletes the user authentication from the configuration model with the given id.
     *
     * @param authenticationId id of the user authentication
     * @return true, if user authentication is deleted
     */
    public boolean deleteConfigModelUserAuth(URI authenticationId) {

        if (getConfigModel().getUserAuthentication().removeIf(userAuthentication -> userAuthentication.getId()
                .equals(authenticationId))) {
            saveState();
            return true;
        }
        return false;
    }

    /**
     * This method updates the configuration model with the given parameters.
     *
     * @param loglevel            logging level of the configuration model
     * @param connectorStatus     connector status of the configuration model
     * @param connectorDeployMode connector deploy mode of the configuration model
     * @param trustStore          trust store of the configuration model
     * @param keyStore            key store of the configuration model
     * @return true, if configuration model is updated
     */
    public boolean updateConfigurationModel(String loglevel, String connectorStatus, String connectorDeployMode,
                                            String trustStore, String keyStore) {

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
        if (keyStore != null) {
            configModelImpl.setKeyStore(URI.create(keyStore));
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
        if (proxyUri != null) {
            proxyImpl.setProxyURI(URI.create(proxyUri));
        }
        if (noProxyUriList != null) {
            proxyImpl.setNoProxy(noProxyUriList);
        }
        if (username != null) {
            proxyImpl.setProxyAuthentication(new BasicAuthenticationBuilder(proxyImpl.getProxyAuthentication().getId())
                    ._authUsername_(username)._authPassword_(proxyImpl.getProxyAuthentication().getAuthPassword()).build());
        }
        if (password != null) {
            proxyImpl.setProxyAuthentication(new BasicAuthenticationBuilder(proxyImpl.getProxyAuthentication().getId())
                    ._authUsername_(proxyImpl.getProxyAuthentication().getAuthUsername())._authPassword_(password).build());
        }
        saveState();
    }

    /**
     * Listener method, which notify about changes in the configuration model
     */
    public void notifyListeners() {
        for (ConfigModelListener listener : listeners) {
            listener.notifyConfig(getConfigModel());
        }
    }
}
