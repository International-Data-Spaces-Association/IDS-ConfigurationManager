package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.StateFormula;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.transition.TransitionFormula;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Node;

import java.util.List;

/**
 * TT operator evaluates to True everytime
 */
public class TT implements StateFormula, TransitionFormula {

    public static TT TT() {
        return new TT();
    }

    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        return true;
    }

    @Override
    public String symbol() {
        return "TT";
    }

    @Override
    public String writeFormula() {
        return symbol();
    }
}
