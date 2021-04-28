package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFormula;
import de.fraunhofer.isst.configmanager.petrinet.model.Arc;
import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Place;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class NodeFORALL_MODAL implements StateFormula {

    public static NodeFORALL_MODAL nodeFORALL_MODAL(StateFormula parameter1, TransitionFormula parameter2){
        return new NodeFORALL_MODAL(parameter1, parameter2);
    }

    private StateFormula parameter1;

    private TransitionFormula parameter2;

    // parameter1, must be true for all successor states, parameter2 must
    // be true for the transitions between the current state and its successors.
    @Override
    public boolean evaluate(Node node, List<List<Node>> paths) {
        if(!(node instanceof Place)) return false;
        var followingTransitions = paths.stream().filter(path -> path.size() == 2 && path.get(0) == node).map(path -> path.get(1)).collect(Collectors.toSet());
        var followingPlaces = followingTransitions.stream().map(Node::getSourceArcs).flatMap(Collection::stream).map(Arc::getTarget).collect(Collectors.toSet());
        for(var place : followingPlaces){
            if(!parameter1.evaluate(place, paths)) return false;
        }
        for(var transition : followingTransitions){
            if(!parameter2.evaluate(transition, paths)) return false;
        }
        return true;
    }

    @Override
    public String symbol() {
        return "FORALL_MODAL";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s, %s)", symbol(), parameter1.writeFormula(), parameter2.writeFormula());
    }
}
