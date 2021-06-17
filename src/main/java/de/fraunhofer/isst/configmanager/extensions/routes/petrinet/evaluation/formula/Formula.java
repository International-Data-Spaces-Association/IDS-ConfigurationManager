package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Node;

import java.util.List;

/**
 * A generic Formula, can be a {@link de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.StateFormula}
 * or a {@link de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.transition.TransitionFormula}.
 */
public interface Formula {

    boolean evaluate(Node node, List<List<Node>> paths);

    String symbol();

    String writeFormula();
}
