package de.fraunhofer.isst.configmanager.configmanagement.entities.configLists;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.isst.configmanager.configmanagement.entities.config.BaseConnectorObject;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * List of all Configurations modeled as JPA entity
 */
@Entity
@Data
public class ConnectorList {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn
    private List<BaseConnectorObject> connectorConfigs = new ArrayList<>();

    /**
     * @return list of connectors
     */
    public List<Connector> getConnectors() {
        return connectorConfigs.stream().map(BaseConnectorObject::getConnector).collect(Collectors.toList());
    }
}
