package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class TransitionNOT implements TransitionFormula {

    public static TransitionNOT transitionNOT(TransitionFormula parameter){
        return new TransitionNOT(parameter);
    }

    private TransitionFormula parameter;

    @Override
    public boolean evaluate(Node node, List<List<Node>> paths) {
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
