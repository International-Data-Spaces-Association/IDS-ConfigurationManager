package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionAND implements TransitionFormula {

    public static TransitionAND transitionAND(TransitionFormula parameter1, TransitionFormula parameter2){
        return new TransitionAND(parameter1, parameter2);
    }

    private TransitionFormula parameter1, parameter2;

    @Override
    public boolean evaluate(Node node) {
        return parameter1.evaluate(node) && parameter2.evaluate(node);
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
