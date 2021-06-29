package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

import java.util.List;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeNOT.nodeNOT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodePOS.nodePOS;
/**
 * evaluates to true, if parameter evaluates to true for all reachable places
 */
@AllArgsConstructor
public class NodeINV implements StateFormula {
    private StateFormula parameter;

    public static NodeINV nodeINV(final StateFormula parameter){
        return new NodeINV(parameter);
    }

    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        return nodeNOT(nodePOS(nodeNOT(parameter))).evaluate(node, paths);
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
