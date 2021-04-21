package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeALONG implements StateFormula {

    public static NodeALONG nodeALONG(StateFormula parameter){
        return new NodeALONG(parameter);
    }

    private StateFormula parameter;

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "ALONG";
    }
}
