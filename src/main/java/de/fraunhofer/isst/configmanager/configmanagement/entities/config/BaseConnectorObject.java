package de.fraunhofer.isst.configmanager.configmanagement.entities.config;

import de.fraunhofer.iais.eis.BaseConnector;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

/**
 * A custom connector entity which extends from ConfigObject. This solution is needed to be able to persist connector in
 * the internal database.
 */
@Entity
@NoArgsConstructor
public class BaseConnectorObject extends ConfigObject<BaseConnector> {

    public BaseConnectorObject(BaseConnector connector) {
        super(connector);
    }
}
