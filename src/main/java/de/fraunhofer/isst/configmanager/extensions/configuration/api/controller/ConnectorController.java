package de.fraunhofer.isst.configmanager.extensions.configuration.api.controller;

import de.fraunhofer.iais.eis.ConfigurationModelImpl;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.extensions.configuration.api.ConnectorApi;
import de.fraunhofer.isst.configmanager.extensions.configuration.api.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.extensions.configuration.api.service.ConnectorService;
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

import java.net.URI;

/**
 * The api class implements the ConnectorApi and offers the possibilities to manage
 * the connectors in the configuration manager.
 */
@Slf4j
@RestController
@RequestMapping("/api/ui")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "Extension: Configuration Connector")
public class ConnectorController implements ConnectorApi {

    transient ConnectorService connectorService;
    transient ConfigModelService configModelService;

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

        final boolean updated = connectorService.updateConnector(title, description, endpointAccessURL,
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
