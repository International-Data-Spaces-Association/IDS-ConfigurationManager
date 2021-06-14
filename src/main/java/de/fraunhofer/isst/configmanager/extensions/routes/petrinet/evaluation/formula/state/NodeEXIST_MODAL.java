package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.transition.TransitionFormula;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Node;
import lombok.AllArgsConstructor;

import java.util.List;

import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.NodeAND.nodeAND;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.NodeMODAL.nodeMODAL;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.transition.TransitionMODAL.transitionMODAL;

/**
 * evaluates to true, if there is a successor place for which parameter1 holds, while parameter2 holds for the
 * transition in between.
 */
@AllArgsConstructor
public class NodeEXIST_MODAL implements StateFormula {
    private StateFormula parameter1;
    private TransitionFormula parameter2;

    private static NodeEXIST_MODAL nodeEXIST_MODAL(final StateFormula parameter1,
                                                   final TransitionFormula parameter2) {
        return new NodeEXIST_MODAL(parameter1, parameter2);
    }

    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        return transitionMODAL(nodeAND(parameter1, nodeMODAL(parameter2))).evaluate(node, paths);
    }

    @Override
    public String symbol() {
        return "EXIST_MODAL";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s, %s)", symbol(), parameter1.writeFormula(), parameter2.writeFormula());
    }
}
