package de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.net.URI;
import java.util.List;

/**
 * This class provides a model to handle data resource metadata.
 *
 * @author Julia Pampus
 * @version $Id: $Id
 */
@Schema(
        name = "ResourceMetadata",
        description = "Metadata of a resource",
        oneOf = ResourceMetadata.class,
        example = "{\n" +
                "  \"title\": \"Sample Resource\",\n" +
                "  \"description\": \"This is an example resource containing weather data.\",\n" +
                "  \"keywords\": [\n" +
                "    \"weather\",\n" +
                "    \"data\",\n" +
                "    \"sample\"\n" +
                "  ],\n" +
                "  \"owner\": \"https://openweathermap.org/\",\n" +
                "  \"license\": \"ODbL\",\n" +
                "  \"version\": \"1.0\"\n" +
                "}\n"
)
@Data
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResourceMetadata implements Serializable {
    @JsonProperty("title")
    String title;

    @JsonProperty("description")
    String description;

    @ElementCollection
    @JsonProperty("keywords")
    List<String> keywords;

    @Column(columnDefinition = "BLOB")
    @JsonProperty("policy")
    String policy;

    @JsonProperty("owner")
    URI owner;

    @JsonProperty("license")
    URI license;

    @JsonProperty("version")
    String version;

    @NotNull
    @ElementCollection
    @Column(columnDefinition = "BLOB")
    @JsonProperty("representations")
    List<ResourceRepresentation> representations;

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final var mapper = new ObjectMapper();
        String jsonString = null;
        try {
            jsonString = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        return jsonString;
    }
}
