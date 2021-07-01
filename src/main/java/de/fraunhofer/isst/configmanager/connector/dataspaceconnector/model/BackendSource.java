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
