package de.fraunhofer.isst.configmanager.configmanagement.entities.backendConnection;

import com.fasterxml.jackson.annotation.JsonAlias;

import javax.validation.constraints.NotNull;
import java.net.URI;

/**
 * Special type of a backend connection.
 */
public class HttpBackendConnection extends BackendConnection {

    @NotNull
    @JsonAlias({"path"})
    private URI path;

    @NotNull
    @JsonAlias({"representationInstanceObject"})
    private ConnectionInstance representationInstanceObject;

    public HttpBackendConnection() {
    }
}