package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.StateFormula;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.transition.TransitionFormula;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Node;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * FF operator evaluates to False everytime.
 */
@NoArgsConstructor
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
