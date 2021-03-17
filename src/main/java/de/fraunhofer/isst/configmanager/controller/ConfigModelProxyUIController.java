package de.fraunhofer.isst.configmanager.controller;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.util.Utility;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

/**
 * The controller class implements the ConfigModelProxyApi and offers the possibilities to manage
 * the proxy settings in the configuration manager.
 */
@RestController
@RequestMapping("/api/ui")
@Tag(name = "ConfigModel - Proxy Management", description = "Endpoints for managing the proxy from the configuration model")
public class ConfigModelProxyUIController implements ConfigModelProxyApi {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigModelProxyUIController.class);

    private final ConfigModelService configModelService;
    private final Serializer serializer;
    private final DefaultConnectorClient client;

    @Autowired
    public ConfigModelProxyUIController(ConfigModelService configModelService, Serializer serializer,
                                        DefaultConnectorClient client) {
        this.configModelService = configModelService;
        this.serializer = serializer;
        this.client = client;

        if (configModelService.getConfigModel().getConnectorProxy() == null) {
            var configModelImpl = (ConfigurationModelImpl) configModelService.getConfigModel();
            Proxy proxy = new ProxyBuilder()
                    ._proxyURI_(URI.create("http://test"))
                    ._noProxy_(Util.asList())
                    ._proxyAuthentication_(new BasicAuthenticationBuilder()._authPassword_("")._authUsername_("").build())
                    .build();
            configModelImpl.setConnectorProxy(Util.asList(proxy));
        }
    }

    /**
     * This method updates the connector proxy at the configuration model with the given parameters.
     *
     * @param proxyUri       uri of the proxy
     * @param noProxyUriList list of no proxy uri's
     * @param username       username for the authentication
     * @param password       password for the authentication
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateConfigModelProxy(String proxyUri, ArrayList<URI> noProxyUriList,
                                                         String username, String password) {
        var configmodelImpl = (ConfigurationModelImpl) configModelService.getConfigModel();
        var jsonObject = new JSONObject();
        if (proxyUri.equals("null")) {
            configmodelImpl.setConnectorProxy(null);
            configModelService.saveState();
            jsonObject.put("message", "Deleted the proxy settings of the configuration model");
        } else {
            if (configModelService.getConfigModel().getConnectorProxy() == null) {
                Proxy proxy = new ProxyBuilder()
                        ._proxyURI_(URI.create(proxyUri))
                        ._noProxy_(noProxyUriList)
                        ._proxyAuthentication_(new BasicAuthenticationBuilder()
                                ._authUsername_(username)._authPassword_(password).build())
                        .build();
                configmodelImpl.setConnectorProxy(Util.asList(proxy));
                configModelService.saveState();
                jsonObject.put("message", "Created a new proxy setting for the configuration model");
            } else {
                var proxyImpl = (ProxyImpl) configModelService.getConfigModel().getConnectorProxy().get(0);
                configModelService.updateConfigurationModelProxy(proxyUri, noProxyUriList, username, password, proxyImpl);
                configModelService.saveState();
                jsonObject.put("message", "Successfully updated proxy for the configuration model");
            }
        }
        // Send updated configuration to the client
        ConfigurationModelImpl configurationModel = (ConfigurationModelImpl) configModelService.getConfigModel();
        if (configurationModel.getAppRoute() != null) {
            configurationModel.setAppRoute(Util.asList());
        }
        try {
            // The configuration model is sent to the client without the app routes at this point,
            // because of the different infomodels.
            var valid = client.sendConfiguration(serializer.serialize(configurationModel));
            if (valid) {
                jsonObject.put("connectorResponse", "Updated the configuration model at the client");
                return ResponseEntity.ok(jsonObject.toJSONString());
            } else {
                jsonObject.put("connectorResponse", "Failed to update the configuration model at the client");
                return ResponseEntity.badRequest().body(jsonObject.toJSONString());
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body("Problems while sending new configuration to the client");
        }
    }

    /**
     * This method returns the connector proxy from the configuration model.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getConfigModelProxy() {
        try {
            return ResponseEntity.ok(serializer.serialize(configModelService.getConfigModel().getConnectorProxy()));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body("Could not get connector proxy from the configuration model");
        }
    }

    /**
     * This method returns the connector proxy from the configuration model in JSON format.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getConfigModelProxyJson() {

        Proxy proxy = configModelService.getConfigModel().getConnectorProxy().get(0);

        JSONObject proxyJson = new JSONObject();
        proxyJson.put("proxyUri", proxy.getProxyURI().toString());
        proxyJson.put("noProxy", proxy.getNoProxy());
        proxyJson.put("username", proxy.getProxyAuthentication().getAuthUsername());
        proxyJson.put("password", proxy.getProxyAuthentication().getAuthPassword());

        return ResponseEntity.ok(proxyJson.toJSONString());
    }

    /**
     * This method deletes the connector proxy from the configuration model with the given id.
     *
     * @param proxyId id of the proxy
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> deleteConfigModelProxy(URI proxyId) {

        var configModelImpl = (ConfigurationModelImpl) configModelService.getConfigModel();

        if (configModelImpl.getConnectorProxy().removeIf(proxy -> proxy.getId().equals(proxyId))) {
            configModelService.saveState();
            return ResponseEntity.ok(Utility.jsonMessage("message", "Successfully deleted connector proxy with the id: " +
                    proxyId.toString()));
        } else {
            return ResponseEntity.badRequest().body("Could not delete the connector proxy");
        }
    }
}
