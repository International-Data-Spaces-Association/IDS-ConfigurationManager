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
