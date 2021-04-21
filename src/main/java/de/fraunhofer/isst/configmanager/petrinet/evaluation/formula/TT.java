package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.StateFormula;
import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFormula;

public class TT implements StateFormula, TransitionFormula {

    public static TT TT(){
        return new TT();
    }

    @Override
    public boolean evaluate() {
        return true;
    }

    @Override
    public String symbol() {
        return "TT";
    }
}
