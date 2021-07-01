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
package de.fraunhofer.isst.configmanager.data.util;

import de.fraunhofer.iais.eis.Endpoint;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * The class helps to persist objects inherited from the endpoint from the information model.
 */
@Data
@MappedSuperclass
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BackendConfig<T extends Endpoint> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    @Convert(converter = EndpointConverter.class)
    @Column(columnDefinition = "TEXT")
    T endpoint;

    public BackendConfig(final T endpoint) {
        this.endpoint = endpoint;
    }
}
