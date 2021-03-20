package de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

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
public class ResourceMetadata implements Serializable {
    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @ElementCollection
    @JsonProperty("keywords")
    private List<String> keywords;

    @Column(columnDefinition = "BLOB")
    @JsonProperty("policy")
    private String policy;

    @JsonProperty("owner")
    private URI owner;

    @JsonProperty("license")
    private URI license;

    @JsonProperty("version")
    private String version;

    @NotNull
    @ElementCollection
    @Column(columnDefinition = "BLOB")
    @JsonProperty("representations")
    private List<ResourceRepresentation> representations;

    /**
     * Constructor for ResourceMetadata.
     */
    public ResourceMetadata() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = null;
        try {
            jsonString = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonString;
    }
}
