package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.StateFormula;
import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFormula;
import de.fraunhofer.isst.configmanager.petrinet.model.Node;

public class TT implements StateFormula, TransitionFormula {

    public static TT TT(){
        return new TT();
    }

    @Override
    public boolean evaluate(Node node) {
        return true;
    }

    @Override
    public String symbol() {
        return "TT";
    }

    @Override
    public String writeFormula() {
        return symbol();
    }
}
