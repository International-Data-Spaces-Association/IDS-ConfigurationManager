package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeFORALL_NEXT implements StateFormula {

    public static NodeFORALL_NEXT nodeFORALL_NEXT(StateFormula parameter){
        return new NodeFORALL_NEXT(parameter);
    }

    private StateFormula parameter;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "FORALL_NEXT";
    }

}
