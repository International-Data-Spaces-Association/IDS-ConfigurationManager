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
package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.policy;

import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.Rule;
import lombok.experimental.UtilityClass;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * PolicyUtils Class from DataspaceConnector.
 */
@UtilityClass
public class PolicyUtils {

    /**
     * Iterate over all rules of a contract and add the ones with the element as their target to a
     * rule list.
     *
     * @param contract The contract.
     * @param element  The requested element.
     * @return List of ids rules.
     * @throws IllegalArgumentException If the message is null.
     */
    public static List<? extends Rule> getRulesForTargetId(final Contract contract,
                                                           final URI element) {
        final var rules = new ArrayList<Rule>();

        if (contract.getPermission() != null) {
            for (final var permission : contract.getPermission()) {
                final var target = permission.getTarget();
                if (target != null && target.equals(element)) {
                    rules.add(permission);
                }
            }
        }

        if (contract.getProhibition() != null) {
            for (final var prohibition : contract.getProhibition()) {
                final var target = prohibition.getTarget();
                if (target != null && target.equals(element)) {
                    rules.add(prohibition);
                }
            }
        }

        if (contract.getObligation() != null) {
            for (final var obligation : contract.getObligation()) {
                final var target = obligation.getTarget();
                if (target != null && target.equals(element)) {
                    rules.add(obligation);
                }
            }
        }

        return rules;
    }
}
