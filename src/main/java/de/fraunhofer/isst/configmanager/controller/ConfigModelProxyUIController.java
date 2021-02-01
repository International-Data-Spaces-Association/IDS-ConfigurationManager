package de.fraunhofer.isst.configmanager.controller;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.util.Utility;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.minidev.json.JSONObject;
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

    private final ConfigModelService configModelService;
    private final Serializer serializer;

    @Autowired
    public ConfigModelProxyUIController(ConfigModelService configModelService, Serializer serializer) {
        this.configModelService = configModelService;
        this.serializer = serializer;

        if (configModelService.getConfigModel().getConnectorProxy() == null) {
            var configModelImpl = (ConfigurationModelImpl) configModelService.getConfigModel();
            Proxy proxy = new ProxyBuilder()
                    ._proxyURI_(URI.create(""))
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

        if (configModelService.getConfigModel().getConnectorProxy() == null) {
            return ResponseEntity.badRequest().body("Could not find any proxy setting to update.");
        } else {
            var proxyImpl = (ProxyImpl) configModelService.getConfigModel().getConnectorProxy().get(0);
            configModelService.updateConfigurationModelProxy(proxyUri, noProxyUriList, username, password, proxyImpl);
            return ResponseEntity.ok(Utility.jsonMessage("message", "Successfully updated proxy for the" +
                    " configuration model with the id: " + proxyImpl.getId().toString()));
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
            e.printStackTrace();
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
