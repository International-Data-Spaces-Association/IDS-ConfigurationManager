package de.fraunhofer.isst.configmanager.extensions.configuration.api.service;

import de.fraunhofer.iais.eis.BaseConnectorImpl;
import de.fraunhofer.iais.eis.ConfigurationModelImpl;
import de.fraunhofer.iais.eis.ConnectorEndpointBuilder;
import de.fraunhofer.iais.eis.SecurityProfile;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

/**
 * Service class for the connector, which manages a list of configurations and a set of observers,
 * notified when the current configuration changes.
 */
@Slf4j
@Service
@Transactional
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ConnectorService {

    transient ConfigModelService configModelService;

    @Autowired
    public ConnectorService(final ConfigModelService configModelService) {
        this.configModelService = configModelService;
        //TODO: Getter + Load from DB (empty constructor)
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
        final var connector = (BaseConnectorImpl) configModelService.getConfigModel().getConnectorDescription();

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
        final var configModelImpl = (ConfigurationModelImpl) configModelService.getConfigModel();
        configModelImpl.setConnectorDescription(connector);
        configModelService.saveState();

        return updated;
    }
}
