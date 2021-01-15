package de.fraunhofer.isst.configmanager.configmanagement.entities.backendConnection;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.net.URI;

/**
 * A connection instance specifies for example the address, at which the artifact can be found.
 */
public abstract class ConnectionInstance {

    @JsonProperty("@id")
    @NotNull
    private URI id;
}
