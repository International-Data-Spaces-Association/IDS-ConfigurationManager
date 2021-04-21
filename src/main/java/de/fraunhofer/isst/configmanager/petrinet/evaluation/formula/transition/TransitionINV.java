package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionNOT.transitionNOT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionPOS.transitionPOS;

@AllArgsConstructor
public class TransitionINV implements TransitionFormula {

    public static TransitionINV transitionINV(TransitionFormula parameter){
        return new TransitionINV(parameter);
    }

    private TransitionFormula parameter;

    @Override
    public boolean evaluate() {
        return transitionNOT(transitionPOS(transitionNOT(parameter))).evaluate();
    }

    @Override
    public String symbol() {
        return "INV";
    }
}
