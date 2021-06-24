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
package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Place;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Custom Expression to be evaluated on a {@link Place}.
 */
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NodeExpression {

    /**
     * Subexpression (function from {@link Place} to boolean.
     */
    NodeSubExpression subExpression;

    /**
     * Information message to return when subExpression is not fulfilled by a transition.
     */
    String message;

    public static NodeExpression nodeExpression(final NodeSubExpression nodeSubExpression,
                                                final String message) {
        return new NodeExpression(nodeSubExpression, message);
    }
}
