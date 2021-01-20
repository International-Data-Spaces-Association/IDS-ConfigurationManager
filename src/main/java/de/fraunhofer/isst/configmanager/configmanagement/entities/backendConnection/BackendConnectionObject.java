package de.fraunhofer.isst.configmanager.configmanagement.entities.backendConnection;

import de.fraunhofer.iais.eis.GenericEndpoint;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Entity
@NoArgsConstructor
public class BackendConnectionObject extends BackendConfig<GenericEndpoint> {

    public BackendConnectionObject(GenericEndpoint endpoint) {
        super(endpoint);
    }

}
