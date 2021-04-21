package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeEV.nodeEV;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeNOT.nodeNOT;

@AllArgsConstructor
public class NodeALONG implements StateFormula {

    public static NodeALONG nodeALONG(StateFormula parameter){
        return new NodeALONG(parameter);
    }

    private StateFormula parameter;

    @Override
    public boolean evaluate() {
        return nodeNOT(nodeEV(nodeNOT(parameter))).evaluate();
    }

    @Override
    public String symbol() {
        return "ALONG";
    }
}
