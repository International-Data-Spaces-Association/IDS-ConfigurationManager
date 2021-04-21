package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionAF implements TransitionFormula {

    public static TransitionAF transitionAF(ArcExpression parameter){
        return new TransitionAF(parameter);
    }

    private ArcExpression parameter;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "AF";
    }
}
