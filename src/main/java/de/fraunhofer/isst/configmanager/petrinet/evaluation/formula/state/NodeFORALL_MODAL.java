package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFormula;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeFORALL_MODAL implements StateFormula {

    public static NodeFORALL_MODAL nodeFORALL_MODAL(StateFormula parameter1, TransitionFormula parameter2){
        return new NodeFORALL_MODAL(parameter1, parameter2);
    }

    private StateFormula parameter1;

    private TransitionFormula parameter2;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "FORALL_MODAL";
    }
}
