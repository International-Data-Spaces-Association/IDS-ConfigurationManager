package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.StateFormula;
import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFormula;
import de.fraunhofer.isst.configmanager.petrinet.model.Node;

import java.util.List;

public class FF implements StateFormula, TransitionFormula {

    public static FF FF() {
        return new FF();
    }

    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        return false;
    }

    @Override
    public String symbol() {
        return "FF";
    }

    @Override
    public String writeFormula() {
        return symbol();
    }
}
