package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Place;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class NodeFORALL_UNTIL implements StateFormula {

    public static NodeFORALL_UNTIL nodeFORALL_UNTIL(StateFormula parameter1, StateFormula parameter2){
        return new NodeFORALL_UNTIL(parameter1, parameter2);
    }

    private StateFormula parameter1, parameter2;

    //like EXIST_UNTIL for all paths
    @Override
    public boolean evaluate(Node node, List<List<Node>> paths) {
        if(!(node instanceof Place)) return false;
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
