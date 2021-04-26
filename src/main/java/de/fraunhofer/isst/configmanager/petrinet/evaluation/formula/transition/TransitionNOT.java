package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionNOT implements TransitionFormula {

    public static TransitionNOT transitionNOT(TransitionFormula parameter){
        return new TransitionNOT(parameter);
    }

    private TransitionFormula parameter;

    @Override
    public boolean evaluate(Node node) {
        return !parameter.evaluate(node);
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
