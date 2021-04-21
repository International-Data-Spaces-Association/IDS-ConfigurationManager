package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeEV implements StateFormula {

    public static NodeEV nodeEV(StateFormula parameter){
        return new NodeEV(parameter);
    }

    private StateFormula parameter;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "EV";
    }
}
