package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFormula;
import lombok.AllArgsConstructor;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeAND.nodeAND;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeMODAL.nodeMODAL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionMODAL.transitionMODAL;

@AllArgsConstructor
public class NodeEXIST_MODAL implements StateFormula {

    private static NodeEXIST_MODAL nodeEXIST_MODAL(StateFormula parameter1, TransitionFormula parameter2){
        return new NodeEXIST_MODAL(parameter1, parameter2);
    }

    private StateFormula parameter1;
    private TransitionFormula parameter2;

    @Override
    public boolean evaluate() {
        return transitionMODAL(nodeAND(parameter1, nodeMODAL(parameter2))).evaluate();
    }

    @Override
    public String symbol() {
        return "EXIST_MODAL";
    }
}
