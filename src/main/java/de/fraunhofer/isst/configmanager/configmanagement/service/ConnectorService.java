package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.ConfigModelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for the connector, which manages a list of configurations and a set of observers,
 * notified when the current configuration changes
 */
@Service
@Slf4j
public class ConnectorService {

    private final ConfigModelService configModelService;
    private ConfigModelRepository configModelRepository;

    @Autowired
    public ConnectorService(ConfigModelService configModelService, ConfigModelRepository configModelRepository) {
        this.configModelService = configModelService;
        this.configModelRepository = configModelRepository;

        // If no connector is found in the database, a default connector is created at this point.
        if (configModelRepository.findAll().get(0).getConfigurationModel().getConnectorDescription() == null) {
            log.info("---- No connector description is found in the configuration model! Creating default connector " +
                    "description");
            BaseConnector connector = new BaseConnectorBuilder()
                    ._inboundModelVersion_(new ArrayList<>(List.of("3.1.0")))
                    ._outboundModelVersion_("3.1.0")
                    ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                    ._maintainer_(URI.create("https://example.com"))
                    ._curator_(URI.create("https://example.com"))
                    .build();
            ConfigurationModelImpl configurationModel = (ConfigurationModelImpl) configModelService.getConfigModel();
            configurationModel.setConnectorDescription(connector);
        }
    }

    /**
     * @param title                  the title of the connector
     * @param description            the description of the connector
     * @param endpointAccessURL      the access url to the connector
     * @param version                the version of the connector
     * @param curator                the curator of the connector
     * @param maintainer             the maintainer of the connector
     * @param inboundedModelVersion  the inbounded model version of the connector
     * @param outboundedModelVersion the outbounded model version of the connector
     * @return base connector
     */
    public BaseConnector createConnector(String title, String description, String endpointAccessURL,
                                         String version, String curator, String maintainer,
                                         String inboundedModelVersion, String outboundedModelVersion) {

        return new BaseConnectorBuilder()
                ._title_(Util.asList(new TypedLiteral(title)))
                ._description_(Util.asList(new TypedLiteral(description)))
                ._hasEndpoint_(Util.asList(new ConnectorEndpointBuilder()._accessURL_(URI.create(endpointAccessURL)).build()))
                ._version_(version)
                ._curator_(URI.create(curator))
                ._maintainer_(URI.create(maintainer))
                ._inboundModelVersion_(Util.asList(inboundedModelVersion))
                ._outboundModelVersion_(outboundedModelVersion)
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE).build();
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
    public boolean updateConnector(String title, String description, String endpointAccessURL, String version,
                                   String curator, String maintainer, String inboundModelVersion, String outboundModelVersion) {

        boolean updated = false;
        var connector = (BaseConnectorImpl) configModelService.getConfigModel()
                .getConnectorDescription();
        if (connector != null) {
            if (title != null) {
                connector.setTitle(Util.asList(new TypedLiteral(title)));
            }
            if (description != null) {
                connector.setDescription(Util.asList(new TypedLiteral(description)));
            }
            if (endpointAccessURL != null) {
                connector.setHasEndpoint(Util.asList(new ConnectorEndpointBuilder()
                        ._accessURL_(URI.create(endpointAccessURL)).build()));
            }
            if (version != null) {
                connector.setVersion(version);
            }
            if (curator != null) {
                connector.setCurator(URI.create(curator));
            }
            if (maintainer != null) {
                connector.setMaintainer(URI.create(maintainer));
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
        var configModelImpl = (ConfigurationModelImpl) configModelService.getConfigModel();
        configModelImpl.setConnectorDescription(connector);
        configModelService.saveState();
        return updated;
    }
}
