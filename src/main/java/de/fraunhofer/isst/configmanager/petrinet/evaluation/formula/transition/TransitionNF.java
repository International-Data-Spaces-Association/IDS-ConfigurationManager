package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionNF implements TransitionFormula {

    public static TransitionNF transitionNF(ArcExpression parameter){
        return new TransitionNF(parameter);
    }

    private ArcExpression parameter;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "NF";
    }
}
