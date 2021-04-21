package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeNF implements StateFormula {

    public static NodeNF nodeNF(NodeExpression parameter){
        return new NodeNF(parameter);
    }

    private NodeExpression parameter;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "NF";
    }
}
