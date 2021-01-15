package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.configmanagement.entities.config.BaseConnectorObject;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.ConnectorList;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.ConnectorListRepository;
import de.fraunhofer.isst.configmanager.configmanagement.service.listeners.ConnectorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ConnectorService {

    private final static Logger logger = LoggerFactory.getLogger(ConnectorService.class);
    private final ConnectorListRepository connectorListRepository;
    private List<ConnectorListener> listeners;
    private final Serializer serializer;
    private ConnectorList connectors;

    @Autowired
    public ConnectorService(Serializer serializer,
                            ConnectorListRepository connectorListRepository) {
        this.connectorListRepository = connectorListRepository;
        this.serializer = serializer;

        // If no connector is found in the database, a default connector is created at this point.
        if (connectorListRepository.count() == 0) {
            logger.info("Db is empty! Creating default configuration");
            BaseConnector connector = new BaseConnectorBuilder()
                    ._inboundModelVersion_(new ArrayList<>(List.of("3.1.0")))
                    ._outboundModelVersion_("3.1.0")
                    ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                    ._maintainer_(URI.create("https://example.com"))
                    ._curator_(URI.create("https://example.com"))
                    .build();
            BaseConnectorObject configObject = new BaseConnectorObject(connector);
            connectors = new ConnectorList();
            connectors.getConnectorConfigs().add(configObject);
            connectors = connectorListRepository.saveAndFlush(connectors);
        } else {
            logger.info("Loading configurations from db");
            connectors = connectorListRepository.findAll().stream().findAny().get();
        }
    }

    /**
     * @param listeners, which notify about changes
     */
    @Autowired
    public void setListeners(List<ConnectorListener> listeners) {
        this.listeners = listeners;
    }

    /**
     * The method saves the state in the repository.
     */
    public void saveState() {
        connectors = connectorListRepository.saveAndFlush(connectors);
    }

    /**
     * @return all currently managed connectors
     */
    public List<Connector> getConnectors() {
        return this.connectors.getConnectors();
    }

    /**
     * @param title                 the title of the connector
     * @param description           the description of the connector
     * @param endpointAccessURL     the access url to the connector
     * @param version               the version of the connector
     * @param curator               the curator of the connector
     * @param maintainer            the maintainer of the connector
     * @param inboundedModelVersion the inbounded model version of the connector
     * @param outbundedModelVersion the outbunded model version of the connector
     * @return base connector
     */
    public BaseConnector createConnector(String title, String description, String endpointAccessURL,
                                         String version, String curator, String maintainer,
                                         String inboundedModelVersion, String outbundedModelVersion) {

        return new BaseConnectorBuilder()
                ._title_(Util.asList(new TypedLiteral(title)))
                ._description_(Util.asList(new TypedLiteral(description)))
                ._hasEndpoint_(Util.asList(new ConnectorEndpointBuilder()._accessURL_(URI.create(endpointAccessURL)).build()))
                ._version_(version)
                ._curator_(URI.create(curator))
                ._maintainer_(URI.create(maintainer))
                ._inboundModelVersion_(Util.asList(inboundedModelVersion))
                ._outboundModelVersion_(outbundedModelVersion)
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE).build();
    }
}
