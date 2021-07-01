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
package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.StateFormula;
import de.fraunhofer.isst.configmanager.petrinet.model.Arc;
import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Transition;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Evaluates to true, if there is a successor transition for which parameter1 holds, while parameter2 holds for the
 * place in between.
 */
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransitionEXIST_MODAL implements TransitionFormula {

    TransitionFormula parameter1;
    StateFormula parameter2;

    public static TransitionEXIST_MODAL transitionEXIST_MODAL(final TransitionFormula parameter1,
                                                               final StateFormula parameter2) {
        return new TransitionEXIST_MODAL(parameter1, parameter2);
    }

    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        if (!(node instanceof Transition)) {
            return false;
        }

        final var followingPlaces = node.getSourceArcs().stream()
                .map(Arc::getTarget)
                .collect(Collectors.toSet());

        for (final var place : followingPlaces) {
            if (parameter2.evaluate(place, paths)) {
                final var followingTrans = place.getSourceArcs().stream().map(Arc::getTarget).collect(Collectors.toSet());

                for (final var following : followingTrans) {
                    if (parameter1.evaluate(following, paths)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String symbol() {
        return "EXIST_MODAL";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s, %s)", symbol(), parameter1.writeFormula(), parameter2.writeFormula());
    }
}
