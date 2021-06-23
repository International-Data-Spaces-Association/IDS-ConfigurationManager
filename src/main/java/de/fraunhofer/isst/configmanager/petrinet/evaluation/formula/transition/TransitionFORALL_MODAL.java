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
 * Evaluates to true, if parameter1 evaluates to true for every following transition and parameter2 evaluates to true
 * for every Place in between.
 */
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransitionFORALL_MODAL implements TransitionFormula {

    TransitionFormula parameter1;
    StateFormula parameter2;

    public static TransitionFORALL_MODAL transitionFORALL_MODAL(final TransitionFormula parameter1,
                                                                final StateFormula parameter2) {
        return new TransitionFORALL_MODAL(parameter1, parameter2);
    }

    // parameter1, must be true for all successor transitions, parameter2 must
    // be true for the states between the current transition and its successors.
    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        if (!(node instanceof Transition)) {
            return false;
        }

        final var followingPlaces = node.getSourceArcs().stream().map(Arc::getTarget).collect(Collectors.toSet());

        followingPlaces.retainAll(paths.stream().filter(path -> path.get(0) == node).map(path -> path.get(1)).collect(Collectors.toSet()));

        final var followingTransitions = paths.stream().filter(path -> followingPlaces.contains(path.get(0))).map(path -> path.get(1)).collect(Collectors.toSet());

        for (final var transition : followingTransitions) {
            if (!parameter1.evaluate(transition, paths)) {
                return false;
            }
        }

        for (final var place : followingPlaces) {
            if (!parameter2.evaluate(place, paths)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String symbol() {
        return "FORALL_MODAL";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s, %s)", symbol(), parameter1.writeFormula(), parameter2.writeFormula());
    }
}
