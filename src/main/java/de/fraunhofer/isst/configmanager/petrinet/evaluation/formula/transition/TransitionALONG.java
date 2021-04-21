package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionALONG implements TransitionFormula {
    
    public static TransitionALONG transitionALONG(TransitionFormula parameter){
        return new TransitionALONG(parameter);
    }
    
    private TransitionFormula parameter;
    
    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "ALONG";
    }
}
