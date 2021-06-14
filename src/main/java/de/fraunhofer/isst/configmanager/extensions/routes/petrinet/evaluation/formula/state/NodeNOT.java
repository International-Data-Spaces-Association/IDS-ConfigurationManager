package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Node;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * evaluates to true, if given subformula evaluates to false
 */
@AllArgsConstructor
public class NodeNOT implements StateFormula {
    private StateFormula parameter;

    public static NodeNOT nodeNOT(final StateFormula parameter){
        return new NodeNOT(parameter);
    }

    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        return !parameter.evaluate(node, paths);
    }

    @Override
    public String symbol() {
        return "NOT";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), parameter.writeFormula());
    }
}
