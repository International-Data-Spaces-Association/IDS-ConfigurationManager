package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeMODAL.nodeMODAL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionMODAL.transitionMODAL;

@AllArgsConstructor
public class TransitionEXIST_NEXT implements TransitionFormula {

    public static TransitionEXIST_NEXT transitionEXIST_NEXT(TransitionFormula parameter){
        return new TransitionEXIST_NEXT(parameter);
    }
    private TransitionFormula parameter;

    @Override
    public boolean evaluate() {
        return transitionMODAL(nodeMODAL(parameter)).evaluate();
    }

    @Override
    public String symbol() {
        return "EXIST_NEXT";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), parameter.writeFormula());
    }
}
