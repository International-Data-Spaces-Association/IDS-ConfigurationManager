package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeFORALL_UNTIL implements StateFormula {

    public static NodeFORALL_UNTIL nodeFORALL_UNTIL(StateFormula parameter1, StateFormula parameter2){
        return new NodeFORALL_UNTIL(parameter1, parameter2);
    }

    private StateFormula parameter1, parameter2;

    //TODO like EXIST_UNTIL for all paths
    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "FORALL_UNTIL";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s, %s)", symbol(), parameter1.writeFormula(), parameter2.writeFormula());
    }
}
