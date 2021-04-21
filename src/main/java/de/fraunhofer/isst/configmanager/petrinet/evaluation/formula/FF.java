package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.StateFormula;
import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFormula;
import org.hibernate.engine.transaction.spi.TransactionImplementor;

public class FF implements StateFormula, TransitionFormula {

    public static FF FF(){
        return new FF();
    }

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public String symbol() {
        return "FF";
    }
}
