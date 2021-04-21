package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFormula;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeEXIST_MODAL implements StateFormula {

    private static NodeEXIST_MODAL nodeEXIST_MODAL(StateFormula parameter1, TransitionFormula parameter2){
        return new NodeEXIST_MODAL(parameter1, parameter2);
    }

    private StateFormula parameter1;
    private TransitionFormula parameter2;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "EXIST_MODAL";
    }
}
