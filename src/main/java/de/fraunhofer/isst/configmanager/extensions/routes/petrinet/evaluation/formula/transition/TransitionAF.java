package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Transition;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Evaluates to true, if given {@link ArcExpression} evaluates to true.
 */
@AllArgsConstructor
public class TransitionAF implements TransitionFormula {
    private ArcExpression parameter;

    public static TransitionAF transitionAF(final ArcExpression parameter) {
        return new TransitionAF(parameter);
    }

    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        return node instanceof Transition && parameter.getSubExpression().evaluate((Transition) node);
    }

    @Override
    public String symbol() {
        return "AF";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), "expression");
    }
}
