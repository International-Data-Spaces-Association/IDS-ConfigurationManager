package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * evaluates to true, if given subformula evaluates to false
 */
@AllArgsConstructor
public class TransitionNOT implements TransitionFormula {
    private TransitionFormula parameter;

    public static TransitionNOT transitionNOT(final TransitionFormula parameter){
        return new TransitionNOT(parameter);
    }

    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        return !parameter.evaluate(node, paths);
    }

    @Override
    public String symbol() {
        return "NOT";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), parameter.writeFormula());
    }
}
