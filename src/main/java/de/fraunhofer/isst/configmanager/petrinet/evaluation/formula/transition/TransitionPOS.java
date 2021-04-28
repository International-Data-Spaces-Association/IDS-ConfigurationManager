package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

import java.util.List;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.TT.TT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionEXIST_UNTIL.transitionEXIST_UNTIL;

@AllArgsConstructor
public class TransitionPOS implements TransitionFormula {

    public static TransitionPOS transitionPOS(TransitionFormula parameter){
        return new TransitionPOS(parameter);
    }

    private TransitionFormula parameter;

    @Override
    public boolean evaluate(Node node, List<List<Node>> paths) {
        return transitionEXIST_UNTIL(TT(), parameter).evaluate(node, paths);
    }

    @Override
    public String symbol() {
        return "POS";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), parameter.writeFormula());
    }

}
