package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.transition.TransitionFormula;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Arc;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Place;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Evaluates to true, if parameter1 evaluates to true for every following place and parameter2 evaluates to true
 * for every transition in between.
 */
@AllArgsConstructor
public class NodeFORALL_MODAL implements StateFormula {
    private StateFormula parameter1;
    private TransitionFormula parameter2;

    public static NodeFORALL_MODAL nodeFORALL_MODAL(final StateFormula parameter1,
                                                    final TransitionFormula parameter2) {
        return new NodeFORALL_MODAL(parameter1, parameter2);
    }

    // parameter1, must be true for all successor states, parameter2 must
    // be true for the transitions between the current state and its successors.
    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        if (!(node instanceof Place)) {
            return false;
        }

        final var followingTransitions = paths.stream().filter(path -> path.size() == 2 && path.get(0) == node).map(path -> path.get(1)).collect(Collectors.toSet());
        final var followingPlaces = followingTransitions.stream().map(Node::getSourceArcs).flatMap(Collection::stream).map(Arc::getTarget).collect(Collectors.toSet());

        for (final var place : followingPlaces) {
            if (!parameter1.evaluate(place, paths)) {
                return false;
            }
        }

        for (final var transition : followingTransitions) {
            if (!parameter2.evaluate(transition, paths)) {
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
