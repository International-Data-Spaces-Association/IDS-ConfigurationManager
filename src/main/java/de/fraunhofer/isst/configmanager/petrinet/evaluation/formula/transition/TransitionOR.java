package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionOR implements TransitionFormula {

    public static TransitionOR transitionOR(TransitionFormula parameter1, TransitionFormula parameter2){
        return new TransitionOR(parameter1, parameter2);
    }

    private TransitionFormula parameter1, parameter2;

    @Override
    public boolean evaluate(Node node) {
        return parameter1.evaluate(node) || parameter2.evaluate(node);
    }

    @Override
    public String symbol() {
        return "OR";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s, %s)", symbol(), parameter1.writeFormula(), parameter2.writeFormula());
    }
}
