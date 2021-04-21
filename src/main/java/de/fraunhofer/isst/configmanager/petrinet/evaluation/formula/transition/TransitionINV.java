package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionINV implements TransitionFormula {

    public static TransitionINV transitionINV(TransitionFormula parameter){
        return new TransitionINV(parameter);
    }

    private TransitionFormula parameter;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "INV";
    }
}
