package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
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
    public boolean evaluate(Node node) {
        return nodeNOT(nodeEV(nodeNOT(parameter))).evaluate(node);
    }

    @Override
    public String symbol() {
        return "ALONG";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), parameter.writeFormula());
    }
}
