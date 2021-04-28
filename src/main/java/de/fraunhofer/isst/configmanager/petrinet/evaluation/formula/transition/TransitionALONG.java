package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

import java.util.List;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionEV.transitionEV;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionNOT.transitionNOT;

@AllArgsConstructor
public class TransitionALONG implements TransitionFormula {
    
    public static TransitionALONG transitionALONG(TransitionFormula parameter){
        return new TransitionALONG(parameter);
    }
    
    private TransitionFormula parameter;
    
    @Override
    public boolean evaluate(Node node, List<List<Node>> paths) {
        return transitionNOT(transitionEV(transitionNOT(parameter))).evaluate(node, paths);
    }

    @Override
    public String symbol() {
        return "ALONG";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), parameter.writeFormula());
    }
}
