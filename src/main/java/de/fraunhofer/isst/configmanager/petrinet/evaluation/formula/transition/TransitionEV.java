package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

import java.util.List;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.TT.TT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFORALL_UNTIL.transitionFORALL_UNTIL;

@AllArgsConstructor
public class TransitionEV implements TransitionFormula {
    private TransitionFormula parameter;

    public static TransitionEV transitionEV(final TransitionFormula parameter) {
        return new TransitionEV(parameter);
    }

    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        return transitionFORALL_UNTIL(TT(), parameter).evaluate(node, paths);
    }

    @Override
    public String symbol() {
        return "EV";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), parameter.writeFormula());
    }
}
