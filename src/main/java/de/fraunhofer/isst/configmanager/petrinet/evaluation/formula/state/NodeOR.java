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
package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * evaluates to true, if at least one of the two subformulas evaluates to true
 */
@AllArgsConstructor
public class NodeOR implements StateFormula {
    private StateFormula parameter1;
    private StateFormula parameter2;

    public static NodeOR nodeOR(final StateFormula parameter1,
                                final StateFormula parameter2){
        return new NodeOR(parameter1, parameter2);
    }

    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        return parameter1.evaluate(node, paths) || parameter2.evaluate(node, paths);
    }

    @Override
    public String symbol() {
        return "OR";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s, %s)", symbol(), parameter1.writeFormula(), parameter2.writeFormula());
    }
}
