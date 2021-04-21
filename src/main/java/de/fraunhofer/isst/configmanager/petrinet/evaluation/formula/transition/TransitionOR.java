package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionOR implements TransitionFormula {

    public static TransitionOR transitionOR(TransitionFormula parameter1, TransitionFormula parameter2){
        return new TransitionOR(parameter1, parameter2);
    }

    private TransitionFormula parameter1, parameter2;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "OR";
    }
}
