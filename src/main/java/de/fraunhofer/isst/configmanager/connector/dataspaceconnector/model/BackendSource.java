package de.fraunhofer.isst.configmanager.connector.dataspaceconnector.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.net.URI;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BackendSource implements Serializable {
    private static final long serialVersionUID = 42L;

    @JsonProperty("type")
    Type type;

    @JsonProperty("url")
    URI url;

    @JsonProperty("username")
    String username;

    @JsonProperty("password")
    String password;

    /**
     * Constructor for BackendSource.
     */
    public BackendSource() {
    }

    /**
     * This enum is used to describe how the backend is accessed.
     */
    public enum Type {
        @JsonProperty("local")
        LOCAL("local"),

        @JsonProperty("http-get")
        HTTP_GET("http-get"),

        @JsonProperty("https-get")
        HTTPS_GET("https-get"),

        @JsonProperty("https-get-basicauth")
        HTTPS_GET_BASICAUTH("https-get-basicauth");

        private final String type;

        Type(final String string) {
            type = string;
        }

        @Override
        public String toString() {
            return type;
        }
    }
}
