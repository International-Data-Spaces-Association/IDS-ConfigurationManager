package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeEXIST_UNTIL implements StateFormula {

    public static NodeEXIST_UNTIL nodeEXIST_UNTIL(StateFormula parameter1, StateFormula parameter2){
        return new NodeEXIST_UNTIL(parameter1, parameter2);
    }

    private StateFormula parameter1, parameter2;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "EXIST_UNTIL";
    }
}
