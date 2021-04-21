package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionPOS implements TransitionFormula {

    public static TransitionPOS nodePOS(TransitionFormula parameter){
        return new TransitionPOS(parameter);
    }

    private TransitionFormula parameter;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "POS";
    }

}
