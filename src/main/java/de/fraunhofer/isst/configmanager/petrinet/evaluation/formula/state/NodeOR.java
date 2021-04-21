package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeOR implements StateFormula {

    public static NodeOR nodeOR(StateFormula parameter1, StateFormula parameter2){
        return new NodeOR(parameter1, parameter2);
    }

    private StateFormula parameter1, parameter2;

    @Override
    public boolean evaluate() {
        return parameter1.evaluate() || parameter2.evaluate();
    }

    @Override
    public String symbol() {
        return "OR";
    }
}
