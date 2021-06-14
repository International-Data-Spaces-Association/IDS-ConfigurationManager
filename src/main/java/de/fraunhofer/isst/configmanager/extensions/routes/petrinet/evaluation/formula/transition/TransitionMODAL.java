package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.StateFormula;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Arc;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Transition;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * evaluates to true, if parameter evaluates to true for a place directly following the transition
 */
@AllArgsConstructor
public class TransitionMODAL implements TransitionFormula {
    private StateFormula parameter;

    public static TransitionMODAL transitionMODAL(final StateFormula parameter){
        return new TransitionMODAL(parameter);
    }

    // MODAL, is true if parameter evaluates to true for a state following the current transition
    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        return node instanceof Transition &&
                node.getSourceArcs().stream()
                        .map(Arc::getTarget)
                        .map(place -> parameter.evaluate(place, paths))
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
