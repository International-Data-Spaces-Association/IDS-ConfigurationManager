package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionEXIST_NEXT implements TransitionFormula {

    public static TransitionEXIST_NEXT transitionEXIST_NEXT(TransitionFormula parameter){
        return new TransitionEXIST_NEXT(parameter);
    }
    private TransitionFormula parameter;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "EXIST_NEXT";
    }
}
