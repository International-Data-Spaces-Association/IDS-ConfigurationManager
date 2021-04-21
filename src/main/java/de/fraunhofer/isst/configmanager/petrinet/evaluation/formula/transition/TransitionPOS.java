package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.TT.TT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionEXIST_UNTIL.transitionEXIST_UNTIL;

@AllArgsConstructor
public class TransitionPOS implements TransitionFormula {

    public static TransitionPOS transitionPOS(TransitionFormula parameter){
        return new TransitionPOS(parameter);
    }

    private TransitionFormula parameter;

    @Override
    public boolean evaluate() {
        return transitionEXIST_UNTIL(TT(), parameter).evaluate();
    }

    @Override
    public String symbol() {
        return "POS";
    }

}
