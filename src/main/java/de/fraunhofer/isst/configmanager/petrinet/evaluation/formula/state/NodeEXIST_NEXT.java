package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeMODAL.nodeMODAL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionMODAL.transitionMODAL;

@AllArgsConstructor
public class NodeEXIST_NEXT implements StateFormula {

    public static NodeEXIST_NEXT nodeEXIST_NEXT(StateFormula parameter){
        return new NodeEXIST_NEXT(parameter);
    }
    private StateFormula parameter;

    @Override
    public boolean evaluate() {
        return nodeMODAL(transitionMODAL(parameter)).evaluate();
    }

    @Override
    public String symbol() {
        return "EXIST_NEXT";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), parameter.writeFormula());
    }
}
