package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionEXIST_UNTIL implements TransitionFormula {

    public static TransitionEXIST_UNTIL transitionEXIST_UNTIL(TransitionFormula parameter1, TransitionFormula parameter2){
        return new TransitionEXIST_UNTIL(parameter1, parameter2);
    }

    private TransitionFormula parameter1, parameter2;

    //TODO
    // True if a path exists, where parameter1 is true on each transition of the path,
    // and parameter2 is true on the final transition of the path
    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "EXIST_UNTIL";
    }
}