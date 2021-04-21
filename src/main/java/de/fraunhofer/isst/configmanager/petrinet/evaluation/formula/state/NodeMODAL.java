package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFormula;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeMODAL implements StateFormula {

    public static NodeMODAL nodeMODAL(TransitionFormula parameter){
        return new NodeMODAL(parameter);
    }

    private TransitionFormula parameter;

    //TODO
    // MODAL, is true if parameter evaluates to true for a transition following the current state
    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "MODAL";
    }

}
