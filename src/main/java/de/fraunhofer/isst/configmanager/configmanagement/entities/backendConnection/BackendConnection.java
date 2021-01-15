package de.fraunhofer.isst.configmanager.configmanagement.entities.backendConnection;


import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.net.URI;

/**
 * A backend connection defines where a resource has been stored.
 * This connection can be for example a database, an address to a file or a simple http address.
 */
public abstract class BackendConnection {

    @NotNull
    @JsonProperty("@id")
    private URI id;

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

}
