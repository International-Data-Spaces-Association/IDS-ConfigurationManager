package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class TransitionAND implements TransitionFormula {
    private TransitionFormula parameter1;
    private TransitionFormula parameter2;

    public static TransitionAND transitionAND(final TransitionFormula parameter1,
                                              final TransitionFormula parameter2) {
        return new TransitionAND(parameter1, parameter2);
    }

    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        return parameter1.evaluate(node, paths) && parameter2.evaluate(node, paths);
    }

    @Override
    public String symbol() {
        return "AND";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s, %s)", symbol(), parameter1.writeFormula(), parameter2.writeFormula());
    }
}
