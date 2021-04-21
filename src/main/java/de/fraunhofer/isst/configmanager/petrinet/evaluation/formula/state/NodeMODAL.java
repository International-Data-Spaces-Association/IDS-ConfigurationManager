package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFormula;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeMODAL implements StateFormula {

    public static NodeMODAL nodeMODAL(TransitionFormula parameter){
        return new NodeMODAL(parameter);
    }

    private TransitionFormula parameter;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "MODAL";
    }

}
