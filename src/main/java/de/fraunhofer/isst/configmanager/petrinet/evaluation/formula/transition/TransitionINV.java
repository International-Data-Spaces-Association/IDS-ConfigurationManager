package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

import java.util.List;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionNOT.transitionNOT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionPOS.transitionPOS;

/**
 * evaluates to true, if parameter evaluates to true for all reachable transitions
 */
@AllArgsConstructor
public class TransitionINV implements TransitionFormula {
    private TransitionFormula parameter;

    public static TransitionINV transitionINV(final TransitionFormula parameter){
        return new TransitionINV(parameter);
    }

    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        return transitionNOT(transitionPOS(transitionNOT(parameter))).evaluate(node, paths);
    }

    @Override
    public String symbol() {
        return "INV";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), parameter.writeFormula());
    }
}