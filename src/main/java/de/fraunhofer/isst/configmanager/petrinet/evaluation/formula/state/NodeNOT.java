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
        return false;
    }

    @Override
    public String symbol() {
        return "NOT";
    }
}
