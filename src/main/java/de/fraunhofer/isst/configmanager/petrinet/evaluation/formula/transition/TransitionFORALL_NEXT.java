package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionFORALL_NEXT implements TransitionFormula {

    public static TransitionFORALL_NEXT transitionFORALL_NEXT(TransitionFormula parameter){
        return new TransitionFORALL_NEXT(parameter);
    }

    private TransitionFormula parameter;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "FORALL_NEXT";
    }

}
