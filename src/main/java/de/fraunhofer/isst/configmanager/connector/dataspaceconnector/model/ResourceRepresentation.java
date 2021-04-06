package de.fraunhofer.isst.configmanager.connector.dataspaceconnector.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

/**
 * ResourceRepresentation class.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResourceRepresentation implements Serializable {
    private static final long serialVersionUID = 42L;

    @Id
    @JsonProperty("uuid")
    UUID uuid;

    @JsonProperty("type")
    String type;

    @JsonProperty("byteSize")
    Integer byteSize;

    @JsonProperty("source")
    @Column(columnDefinition = "BLOB")
    BackendSource source;
}
