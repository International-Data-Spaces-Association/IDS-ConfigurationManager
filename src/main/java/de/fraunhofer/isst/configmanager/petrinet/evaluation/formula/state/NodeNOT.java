package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeNOT implements StateFormula {

    public static NodeNOT nodeNOT(StateFormula parameter){
        return new NodeNOT(parameter);
    }

    private StateFormula parameter;

    @Override
    public boolean evaluate() {
        return !parameter.evaluate();
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
