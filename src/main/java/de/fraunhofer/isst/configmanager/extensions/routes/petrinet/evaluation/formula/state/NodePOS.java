package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Node;
import lombok.AllArgsConstructor;

import java.util.List;

import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.TT.TT;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.NodeEXIST_UNTIL.nodeEXIST_UNTIL;

/**
 * Evaluates to true, if some Place is reachable, which fulfills the given parameter
 */
@AllArgsConstructor
public class NodePOS implements StateFormula {
    private StateFormula parameter;

    public static NodePOS nodePOS(final StateFormula parameter) {
        return new NodePOS(parameter);
    }

    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        return nodeEXIST_UNTIL(TT(), parameter).evaluate(node, paths);
    }

    @Override
    public String symbol() {
        return "POS";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), parameter.writeFormula());
    }

}
