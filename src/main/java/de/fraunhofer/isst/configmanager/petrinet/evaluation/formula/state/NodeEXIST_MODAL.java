package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFormula;
import de.fraunhofer.isst.configmanager.petrinet.model.Arc;
import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Place;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Evaluates to true, if there is a successor place for which parameter1 holds, while parameter2 holds for the
 * transition in between.
 */
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NodeEXIST_MODAL implements StateFormula {

    StateFormula parameter1;
    TransitionFormula parameter2;

    public static NodeEXIST_MODAL nodeEXIST_MODAL(final StateFormula parameter1,
                                                   final TransitionFormula parameter2) {
        return new NodeEXIST_MODAL(parameter1, parameter2);
    }

    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        if (!(node instanceof Place)) {
            return false;
        }

        final var followingTransitions = node.getSourceArcs().stream()
                .map(Arc::getTarget)
                .collect(Collectors.toSet());

        for (final var trans : followingTransitions) {
            if (parameter2.evaluate(trans, paths)) {
                final var followingPlaces = trans.getSourceArcs().stream().map(Arc::getTarget).collect(Collectors.toSet());
                for (final var following : followingPlaces) {
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
