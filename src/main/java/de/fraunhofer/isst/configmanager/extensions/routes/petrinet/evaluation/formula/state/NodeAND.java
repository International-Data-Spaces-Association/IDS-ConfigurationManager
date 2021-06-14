package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Node;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * evaluates to true, if parameter1 and parameter2 evaluate to true
 */
@AllArgsConstructor
public class NodeAND implements StateFormula {

    private StateFormula parameter1;
    private StateFormula parameter2;

    public static NodeAND nodeAND(final StateFormula parameter1, final StateFormula parameter2) {
        return new NodeAND(parameter1, parameter2);
    }

    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        return parameter1.evaluate(node, paths) && parameter2.evaluate(node, paths);
    }

    @Override
    public String symbol() {
        return "AND";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s, %s)", symbol(), parameter1.writeFormula(), parameter2.writeFormula());
    }
}
