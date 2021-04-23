package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeNOT.nodeNOT;

@AllArgsConstructor
public class NodeFORALL_NEXT implements StateFormula {

    public static NodeFORALL_NEXT nodeFORALL_NEXT(StateFormula parameter){
        return new NodeFORALL_NEXT(parameter);
    }

    private StateFormula parameter;

    @Override
    public boolean evaluate() {
        return nodeNOT(nodeFORALL_NEXT(nodeNOT(parameter))).evaluate();
    }

    @Override
    public String symbol() {
        return "FORALL_NEXT";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), parameter.writeFormula());
    }

}
