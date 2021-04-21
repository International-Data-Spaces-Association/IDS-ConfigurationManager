package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.StateFormula;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionFORALL_MODAL implements TransitionFormula {

    public static TransitionFORALL_MODAL transitionFORALL_MODAL(TransitionFormula parameter1, StateFormula parameter2){
        return new TransitionFORALL_MODAL(parameter1, parameter2);
    }

    private TransitionFormula parameter1;
    private StateFormula parameter2;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "FORALL_MODAL";
    }
}
