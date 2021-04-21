package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionEXIST_UNTIL implements TransitionFormula {

    public static TransitionEXIST_UNTIL transitionEXIST_UNTIL(TransitionFormula parameter1, TransitionFormula parameter2){
        return new TransitionEXIST_UNTIL(parameter1, parameter2);
    }

    private TransitionFormula parameter1, parameter2;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "EXIST_UNTIL";
    }
}
