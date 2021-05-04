package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Transition;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class TransitionFORALL_UNTIL implements TransitionFormula {

    public static TransitionFORALL_UNTIL transitionFORALL_UNTIL(TransitionFormula parameter1, TransitionFormula parameter2){
        return new TransitionFORALL_UNTIL(parameter1, parameter2);
    }

    private TransitionFormula parameter1, parameter2;

    //like EXIST_UNTIL but requires conditions for all paths
    @Override
    public boolean evaluate(Node node, List<List<Node>> paths) {
        if(!(node instanceof Transition)) return false;
        for(var path: paths){
            if(path.get(0).equals(node)){
                for(int i = 0; i<path.size()-1;i++){
                    if(!parameter1.evaluate(path.get(i), paths)) return false;
                }
                if(!parameter2.evaluate(path.get(path.size()-1), paths)) return false;
            }
        }
        return true;
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
