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

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Entity class for persisting the endpoint with the coordinates.
 */
@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EndpointInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    String routeId;
    String endpointId;
    int xCoordinate;
    int yCoordinate;

    public EndpointInformation() {
    }

    public EndpointInformation(final String routeId, final String endpointId, final int xCoordinate,
                               final int yCoordinate) {
        this.routeId = routeId;
        this.endpointId = endpointId;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }
}
