package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionEV implements TransitionFormula {

    public static TransitionEV transitionEV(TransitionFormula parameter){
        return new TransitionEV(parameter);
    }

    private TransitionFormula parameter;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "EV";
    }
}
