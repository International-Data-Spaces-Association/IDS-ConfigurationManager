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

import de.fraunhofer.iais.eis.AppEndpoint;
import de.fraunhofer.isst.configmanager.data.util.BackendConfig;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

/**
 * A custom endpoint entity which extends from BackendConfig. This solution is needed to be able
 * to persist endpoint in
 * the internal database.
 */
@Entity
@NoArgsConstructor
public class CustomAppEndpoint extends BackendConfig<AppEndpoint> {
    public CustomAppEndpoint(final AppEndpoint appEndpoint) {
        super(appEndpoint);
    }
}
