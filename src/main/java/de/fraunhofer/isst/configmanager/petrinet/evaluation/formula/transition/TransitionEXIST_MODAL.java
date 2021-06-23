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
