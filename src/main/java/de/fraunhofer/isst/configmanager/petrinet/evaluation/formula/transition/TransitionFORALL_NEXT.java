package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

import java.util.List;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionEXIST_NEXT.transitionEXIST_NEXT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionNOT.transitionNOT;

@AllArgsConstructor
public class TransitionFORALL_NEXT implements TransitionFormula {

    public static TransitionFORALL_NEXT transitionFORALL_NEXT(TransitionFormula parameter){
        return new TransitionFORALL_NEXT(parameter);
    }

    private TransitionFormula parameter;

    @Override
    public boolean evaluate(Node node, List<List<Node>> paths) {
        return transitionNOT(transitionEXIST_NEXT(transitionNOT(parameter))).evaluate(node, paths);
    }

    @Override
    public String symbol() {
        return "FORALL_NEXT";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), parameter.writeFormula());
    }

}
