package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.transition.TransitionFormula;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Arc;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Place;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Evaluates to true, if parameter evaluates to true for a transition directly following the current place.
 */
@AllArgsConstructor
public class NodeMODAL implements StateFormula {
    private TransitionFormula parameter;

    public static NodeMODAL nodeMODAL(final TransitionFormula parameter) {
        return new NodeMODAL(parameter);
    }

    // MODAL, is true if parameter evaluates to true for a transition following the current state
    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        return node instanceof Place
                && node.getSourceArcs().stream()
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
