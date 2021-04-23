package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeNOT.nodeNOT;

@AllArgsConstructor
public class NodeFORALL_NEXT implements StateFormula {

    public static NodeFORALL_NEXT nodeFORALL_NEXT(StateFormula parameter){
        return new NodeFORALL_NEXT(parameter);
    }

    private StateFormula parameter;

    @Override
    public boolean evaluate(Node node) {
        return nodeNOT(nodeFORALL_NEXT(nodeNOT(parameter))).evaluate(node);
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
