package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionFORALL_UNTIL implements TransitionFormula {

    public TransitionFORALL_UNTIL nodeFORALL_UNTIL(TransitionFormula parameter1, TransitionFormula parameter2){
        return new TransitionFORALL_UNTIL(parameter1, parameter2);
    }

    private TransitionFormula parameter1, parameter2;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "FORALL_UNTIL";
    }
}
