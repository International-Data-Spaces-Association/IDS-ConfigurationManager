package de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.net.URI;


@Schema(
        name = "BackendSource",
        description = "Information of the backend system.",
        oneOf = BackendSource.class
)
@Data
public class BackendSource implements Serializable {

    @JsonProperty("type")
    private Type type;
    @JsonProperty("url")
    private URI url;
    @JsonProperty("username")
    private String username;
    @JsonProperty("password")
    private String password;

    /**
     * Constructor for BackendSource.
     */
    public BackendSource() { }

    /**
     * This enum is used to describe how the backend is accessed.
     */
    @Schema(
            name = "Type",
            description = "Information of the backend system.",
            oneOf = Type.class
    )
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

        Type(String string) {
            type = string;
        }

        @Override
        public String toString() {
            return type;
        }
    }
}
