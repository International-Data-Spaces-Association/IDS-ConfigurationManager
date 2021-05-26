package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Place;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class NodeNF implements StateFormula {
    private NodeExpression parameter;

    public static NodeNF nodeNF(final NodeExpression parameter){
        return new NodeNF(parameter);
    }

    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        return node instanceof Place && parameter.getSubExpression().evaluate((Place) node);
    }

    @Override
    public String symbol() {
        return "NF";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), "expression");
    }
}
