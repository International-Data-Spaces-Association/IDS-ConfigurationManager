package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeEXIST_UNTIL implements StateFormula {

    public static NodeEXIST_UNTIL nodeEXIST_UNTIL(StateFormula parameter1, StateFormula parameter2){
        return new NodeEXIST_UNTIL(parameter1, parameter2);
    }

    private StateFormula parameter1, parameter2;

    @Override
    //TODO
    // True if a path exists, where parameter1 is true on each node of the path,
    // and parameter2 is true on the final node of the path
    public boolean evaluate(Node node) {
        return false;
    }

    @Override
    public String symbol() {
        return "EXIST_UNTIL";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s, %s)", symbol(), parameter1.writeFormula(), parameter2.writeFormula());
    }
}
