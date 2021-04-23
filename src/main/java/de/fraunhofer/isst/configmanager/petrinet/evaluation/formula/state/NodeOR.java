package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeOR implements StateFormula {

    public static NodeOR nodeOR(StateFormula parameter1, StateFormula parameter2){
        return new NodeOR(parameter1, parameter2);
    }

    private StateFormula parameter1, parameter2;

    @Override
    public boolean evaluate(Node node) {
        return parameter1.evaluate(node) || parameter2.evaluate(node);
    }

    @Override
    public String symbol() {
        return "OR";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s, %s)", symbol(), parameter1.writeFormula(), parameter2.writeFormula());
    }
}
