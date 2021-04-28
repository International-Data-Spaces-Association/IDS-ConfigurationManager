package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFormula;
import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class NodeFORALL_MODAL implements StateFormula {

    public static NodeFORALL_MODAL nodeFORALL_MODAL(StateFormula parameter1, TransitionFormula parameter2){
        return new NodeFORALL_MODAL(parameter1, parameter2);
    }

    private StateFormula parameter1;

    private TransitionFormula parameter2;

    //TODO
    // parameter1, must be true for all successor states, parameter2 must
    // be true for the transitions between the current state and its successors.
    @Override
    public boolean evaluate(Node node, List<List<Node>> paths) {
        return false;
    }

    @Override
    public String symbol() {
        return "FORALL_MODAL";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s, %s)", symbol(), parameter1.writeFormula(), parameter2.writeFormula());
    }
}
