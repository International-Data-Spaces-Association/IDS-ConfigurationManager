package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeNOT.nodeNOT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodePOS.nodePOS;

@AllArgsConstructor
public class NodeINV implements StateFormula {

    public static NodeINV nodeINV(StateFormula parameter){
        return new NodeINV(parameter);
    }

    private StateFormula parameter;

    @Override
    public boolean evaluate() {
        return nodeNOT(nodePOS(nodeNOT(parameter))).evaluate();
    }

    @Override
    public String symbol() {
        return "INV";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), parameter.writeFormula());
    }
}
