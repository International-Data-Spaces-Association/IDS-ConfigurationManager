package de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

/**
 * ResourceRepresentation class.
 */
@Schema(
        name = "ResourceRepresentation",
        description = "Representation of a resource",
        oneOf = ResourceRepresentation.class,
        example = "{\n" +
                "      \"type\": \"json\",\n" +
                "      \"byteSize\": 105,\n" +
                "      \"source\": {\n" +
                "        \"type\":\"http-get\", \n" +
                "        \"url\": \"https://samples.openweathermap.org/data/2.5/weather?lat=35&lon=139&appid=439d4b804bc8187953eb36d2a8c26a02\",\n" +
                "        \"username\": \"-\",\n" +
                "        \"password\": \"-\",\n" +
                "        \"system\": \"Open Weather Map API\"\n" +
                "      }\n" +
                "    }"
)
@Data
public class ResourceRepresentation implements Serializable {

    @Id
    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("type")
    private String type;

    @JsonProperty("byteSize")
    private Integer byteSize;

    @JsonProperty("source")
    @Column(columnDefinition = "BLOB")
    private BackendSource source;
}
