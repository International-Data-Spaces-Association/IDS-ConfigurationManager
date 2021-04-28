package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Place;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class NodeEXIST_UNTIL implements StateFormula {

    public static NodeEXIST_UNTIL nodeEXIST_UNTIL(StateFormula parameter1, StateFormula parameter2){
        return new NodeEXIST_UNTIL(parameter1, parameter2);
    }

    private StateFormula parameter1, parameter2;

    @Override
    // True if a path exists, where parameter1 is true on each node of the path,
    // and parameter2 is true on the final node of the path
    public boolean evaluate(Node node, List<List<Node>> paths) {
        if(!(node instanceof Place)) return false;
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
