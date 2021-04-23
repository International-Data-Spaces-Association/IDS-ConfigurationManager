package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.StateFormula;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionMODAL implements TransitionFormula {

    public static TransitionMODAL transitionMODAL(StateFormula parameter){
        return new TransitionMODAL(parameter);
    }

    private StateFormula parameter;

    //TODO
    // MODAL, is true if parameter evaluates to true for a state following the current transition
    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "MODAL";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), parameter.writeFormula());
    }

}
