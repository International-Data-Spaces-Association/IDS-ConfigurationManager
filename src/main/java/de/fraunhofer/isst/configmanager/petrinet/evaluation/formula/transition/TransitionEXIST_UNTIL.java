package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Transition;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class TransitionEXIST_UNTIL implements TransitionFormula {

    public static TransitionEXIST_UNTIL transitionEXIST_UNTIL(TransitionFormula parameter1, TransitionFormula parameter2){
        return new TransitionEXIST_UNTIL(parameter1, parameter2);
    }

    private TransitionFormula parameter1, parameter2;

    // True if a path exists, where parameter1 is true on each transition of the path,
    // and parameter2 is true on the final transition of the path
    @Override
    public boolean evaluate(Node node, List<List<Node>> paths) {
        if(!(node instanceof Transition)) return false;
        check: for(var path: paths){
            if(path.get(0).equals(node)){
                for(int i = 0; i<path.size()-1;i++){
                    if(!parameter1.evaluate(path.get(i), paths)) continue check;
                }
                if(parameter2.evaluate(path.get(path.size()-1), paths)) return true;
            }
        }
        return false;
    }

    @Override
    public String symbol() {
        return "EXIST_UNTIL";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s, %s)", symbol(), parameter1.writeFormula(), parameter2.writeFormula());
    }
}
