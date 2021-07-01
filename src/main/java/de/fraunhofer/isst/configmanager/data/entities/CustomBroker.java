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
package de.fraunhofer.isst.configmanager.data.entities;

import de.fraunhofer.isst.configmanager.data.enums.BrokerRegistrationStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.net.URI;
import java.util.List;

/**
 * A custom broker entity, to be able to persist the broker in the intern database.
 */
@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomBroker {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    URI brokerUri;

    String title;

    BrokerRegistrationStatus brokerRegistrationStatus;

    @ElementCollection
    List<String> registeredResources;

    public CustomBroker() {
    }

    public CustomBroker(final URI brokerUri) {
        this.brokerUri = brokerUri;
    }
}
