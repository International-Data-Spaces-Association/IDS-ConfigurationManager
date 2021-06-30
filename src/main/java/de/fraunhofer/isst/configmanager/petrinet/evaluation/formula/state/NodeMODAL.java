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

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFormula;
import de.fraunhofer.isst.configmanager.petrinet.model.Arc;
import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Place;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * evaluates to true, if parameter evaluates to true for a transition directly following the current place
 */
@AllArgsConstructor
public class NodeMODAL implements StateFormula {
    private TransitionFormula parameter;

    public static NodeMODAL nodeMODAL(final TransitionFormula parameter){
        return new NodeMODAL(parameter);
    }

    // MODAL, is true if parameter evaluates to true for a transition following the current state
    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        return node instanceof Place &&
                node.getSourceArcs().stream()
                        .map(Arc::getTarget)
                        .map(transition -> parameter.evaluate(transition, paths))
                        .reduce(false, (a, b) -> a || b);
    }

    @Override
    public String symbol() {
        return "MODAL";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), parameter.writeFormula());
    }
}
