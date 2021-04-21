package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.TT.TT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFORALL_UNTIL.transitionFORALL_UNTIL;

@AllArgsConstructor
public class TransitionEV implements TransitionFormula {

    public static TransitionEV transitionEV(TransitionFormula parameter){
        return new TransitionEV(parameter);
    }

    private TransitionFormula parameter;

    @Override
    public boolean evaluate() {
        return transitionFORALL_UNTIL(TT(), parameter).evaluate();
    }

    @Override
    public String symbol() {
        return "EV";
    }
}
