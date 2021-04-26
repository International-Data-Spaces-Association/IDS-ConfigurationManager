package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeNOT implements StateFormula {

    public static NodeNOT nodeNOT(StateFormula parameter){
        return new NodeNOT(parameter);
    }

    private StateFormula parameter;

    @Override
    public boolean evaluate(Node node) {
        return !parameter.evaluate(node);
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
