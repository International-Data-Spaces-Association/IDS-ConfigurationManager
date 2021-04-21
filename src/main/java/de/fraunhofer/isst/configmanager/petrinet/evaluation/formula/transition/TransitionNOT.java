package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionNOT implements TransitionFormula {

    public static TransitionNOT nodeNOT(TransitionFormula parameter){
        return new TransitionNOT(parameter);
    }

    private TransitionFormula parameter;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "NOT";
    }
}
