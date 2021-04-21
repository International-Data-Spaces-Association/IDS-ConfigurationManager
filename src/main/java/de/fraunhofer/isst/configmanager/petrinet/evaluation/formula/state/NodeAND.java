package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeAND implements StateFormula {

    public static NodeAND nodeAND(StateFormula parameter1, StateFormula parameter2){
        return new NodeAND(parameter1, parameter2);
    }

    private StateFormula parameter1, parameter2;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "AND";
    }
}
