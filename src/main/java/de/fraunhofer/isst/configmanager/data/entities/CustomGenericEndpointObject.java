package de.fraunhofer.isst.configmanager.data.entities;

import de.fraunhofer.iais.eis.GenericEndpoint;
import de.fraunhofer.isst.configmanager.data.util.BackendConfig;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

/**
 * A custom generic endpoint entity which extends from BackendConfig. This solution is needed to
 * be able to persist endpoint in the internal database.
 */
@Entity
@NoArgsConstructor
public class CustomGenericEndpointObject extends BackendConfig<GenericEndpoint> {
    public CustomGenericEndpointObject(final GenericEndpoint endpoint) {
        super(endpoint);
    }
}
