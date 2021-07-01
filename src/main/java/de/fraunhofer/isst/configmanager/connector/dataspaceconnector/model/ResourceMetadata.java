/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.isst.configmanager.connector.dataspaceconnector.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 */
@Data
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResourceMetadata implements Serializable {
    private static final long serialVersionUID = 42L;

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
