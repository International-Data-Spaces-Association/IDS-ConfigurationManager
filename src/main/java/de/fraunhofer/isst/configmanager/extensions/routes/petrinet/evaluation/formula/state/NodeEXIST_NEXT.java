package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Node;
import lombok.AllArgsConstructor;

import java.util.List;

import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.NodeMODAL.nodeMODAL;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.transition.TransitionMODAL.transitionMODAL;

/**
 * Evaluates to true, if there is a following place fulfilling the given formula.
 */
@AllArgsConstructor
public class NodeEXIST_NEXT implements StateFormula {
    private StateFormula parameter;

    public static NodeEXIST_NEXT nodeEXIST_NEXT(final StateFormula parameter) {
        return new NodeEXIST_NEXT(parameter);
    }

    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        return nodeMODAL(transitionMODAL(parameter)).evaluate(node, paths);
    }

    @Override
    public String symbol() {
        return "EXIST_NEXT";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), parameter.writeFormula());
    }
}
