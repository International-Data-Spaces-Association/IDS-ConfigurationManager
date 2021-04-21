package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionAND implements TransitionFormula {

    public static TransitionAND transitionAND(TransitionFormula parameter1, TransitionFormula parameter2){
        return new TransitionAND(parameter1, parameter2);
    }

    private TransitionFormula parameter1, parameter2;

    @Override
    public boolean evaluate() {
        return parameter1.evaluate() && parameter2.evaluate();
    }

    @Override
    public String symbol() {
        return "AND";
    }
}
