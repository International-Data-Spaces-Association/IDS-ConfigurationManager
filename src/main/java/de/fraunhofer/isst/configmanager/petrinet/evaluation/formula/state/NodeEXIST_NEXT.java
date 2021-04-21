package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeEXIST_NEXT implements StateFormula {

    public static NodeEXIST_NEXT nodeEXIST_NEXT(StateFormula parameter){
        return new NodeEXIST_NEXT(parameter);
    }
    private StateFormula parameter;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "EXIST_NEXT";
    }
}
