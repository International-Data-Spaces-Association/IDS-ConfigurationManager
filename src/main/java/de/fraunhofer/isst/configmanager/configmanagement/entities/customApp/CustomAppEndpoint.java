package de.fraunhofer.isst.configmanager.configmanagement.entities.customApp;

import de.fraunhofer.iais.eis.AppEndpoint;
import de.fraunhofer.isst.configmanager.configmanagement.entities.backendConnection.BackendConfig;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Entity
@NoArgsConstructor
public class CustomAppEndpoint extends BackendConfig<AppEndpoint> {

    public CustomAppEndpoint(AppEndpoint appEndpoint) {
        super(appEndpoint);
    }

}
