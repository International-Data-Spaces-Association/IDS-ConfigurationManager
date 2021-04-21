package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.StateFormula;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionEXIST_MODAL implements TransitionFormula {

    private static TransitionEXIST_MODAL transitionEXIST_MODAL(TransitionFormula parameter1, StateFormula parameter2){
        return new TransitionEXIST_MODAL(parameter1, parameter2);
    }

    private TransitionFormula parameter1;
    private StateFormula parameter2;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "EXIST_MODAL";
    }
}
