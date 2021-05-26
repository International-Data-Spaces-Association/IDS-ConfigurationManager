package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Transition;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class TransitionAF implements TransitionFormula {
    private ArcExpression parameter;

    public static TransitionAF transitionAF(final ArcExpression parameter){
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
