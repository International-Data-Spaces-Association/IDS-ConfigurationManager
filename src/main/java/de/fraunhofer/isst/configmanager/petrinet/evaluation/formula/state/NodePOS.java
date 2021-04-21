package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodePOS implements StateFormula {

    public static NodePOS nodePOS(StateFormula parameter){
        return new NodePOS(parameter);
    }

    private StateFormula parameter;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "POS";
    }

}
