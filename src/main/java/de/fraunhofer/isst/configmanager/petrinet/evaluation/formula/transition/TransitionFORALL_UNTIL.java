package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionFORALL_UNTIL implements TransitionFormula {

    public static TransitionFORALL_UNTIL transitionFORALL_UNTIL(TransitionFormula parameter1, TransitionFormula parameter2){
        return new TransitionFORALL_UNTIL(parameter1, parameter2);
    }

    private TransitionFormula parameter1, parameter2;

    //TODO like EXIST_UNTIL but requires conditions for all paths
    @Override
    public boolean evaluate(Node node) {
        return false;
    }

    @Override
    public String symbol() {
        return "FORALL_UNTIL";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s, %s)", symbol(), parameter1.writeFormula(), parameter2.writeFormula());
    }
}
