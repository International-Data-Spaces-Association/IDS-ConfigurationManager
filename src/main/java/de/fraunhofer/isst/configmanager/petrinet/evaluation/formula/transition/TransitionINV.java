package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

import java.util.List;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionNOT.transitionNOT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionPOS.transitionPOS;

@AllArgsConstructor
public class TransitionINV implements TransitionFormula {

    public static TransitionINV transitionINV(TransitionFormula parameter){
        return new TransitionINV(parameter);
    }

    private TransitionFormula parameter;

    @Override
    public boolean evaluate(Node node, List<List<Node>> paths) {
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
