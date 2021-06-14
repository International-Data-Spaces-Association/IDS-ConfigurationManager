package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Node;
import lombok.AllArgsConstructor;

import java.util.List;

import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.NodeEXIST_NEXT.nodeEXIST_NEXT;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.NodeNOT.nodeNOT;

/**
 * evaluates to true, if all following places satisfy the given formula
 */
@AllArgsConstructor
public class NodeFORALL_NEXT implements StateFormula {
    private StateFormula parameter;

    public static NodeFORALL_NEXT nodeFORALL_NEXT(final StateFormula parameter){
        return new NodeFORALL_NEXT(parameter);
    }

    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        return nodeNOT(nodeEXIST_NEXT(nodeNOT(parameter))).evaluate(node, paths);
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
