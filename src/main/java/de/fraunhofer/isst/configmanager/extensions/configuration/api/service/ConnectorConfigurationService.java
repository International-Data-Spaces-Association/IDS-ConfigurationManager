package de.fraunhofer.isst.configmanager.extensions.configuration.api.service;

import de.fraunhofer.iais.eis.BaseConnectorImpl;
import de.fraunhofer.iais.eis.BasicAuthenticationBuilder;
import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.ConfigurationModelImpl;
import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.iais.eis.ConnectorEndpointBuilder;
import de.fraunhofer.iais.eis.LogLevel;
import de.fraunhofer.iais.eis.ProxyBuilder;
import de.fraunhofer.iais.eis.ProxyImpl;
import de.fraunhofer.iais.eis.SecurityProfile;
import de.fraunhofer.iais.eis.util.TypedLiteral;
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
public class ConnectorConfigurationService {

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

        var updated = false;
        final var connector = (BaseConnectorImpl) getConfigModel().getConnectorDescription();

        if (connector != null) {
            if (title != null) {
                connector.setTitle(Util.asList(new TypedLiteral(title)));
            }
            if (description != null) {
                connector.setDescription(Util.asList(new TypedLiteral(description)));
            }
            if (endpointAccessURL != null) {
                connector.setHasEndpoint(Util.asList(new ConnectorEndpointBuilder()
                        ._accessURL_(endpointAccessURL).build()));
            }
            if (version != null) {
                connector.setVersion(version);
            }
            if (curator != null) {
                connector.setCurator(curator);
            }
            if (maintainer != null) {
                connector.setMaintainer(maintainer);
            }
            if (inboundModelVersion != null) {
                connector.setInboundModelVersion(Util.asList(inboundModelVersion));
            }
            if (outboundModelVersion != null) {
                connector.setOutboundModelVersion(outboundModelVersion);
            }
            connector.setSecurityProfile(SecurityProfile.BASE_SECURITY_PROFILE);
            updated = true;
        }
        final var configModelImpl = (ConfigurationModelImpl) getConfigModel();
        configModelImpl.setConnectorDescription(connector);
        saveState();

        return updated;
    }
}
