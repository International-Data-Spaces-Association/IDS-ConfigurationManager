package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.StateFormula;
import de.fraunhofer.isst.configmanager.petrinet.model.Arc;
import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Transition;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TransitionFORALL_MODAL implements TransitionFormula {

    public static TransitionFORALL_MODAL transitionFORALL_MODAL(TransitionFormula parameter1, StateFormula parameter2){
        return new TransitionFORALL_MODAL(parameter1, parameter2);
    }

    private TransitionFormula parameter1;
    private StateFormula parameter2;

    // parameter1, must be true for all successor transitions, parameter2 must
    // be true for the states between the current transition and its successors.
    @Override
    public boolean evaluate(Node node, List<List<Node>> paths) {
        if(!(node instanceof Transition)) return false;
        var followingPlaces = node.getSourceArcs().stream().map(Arc::getTarget).collect(Collectors.toSet());
        followingPlaces.retainAll(paths.stream().filter(path -> paths.size() == 2).map(path -> path.get(0)).collect(Collectors.toSet()));
        var followingTransitions = paths.stream().filter(path -> path.size() == 2).filter(path -> followingPlaces.contains(path.get(0))).map(path -> path.get(1)).collect(Collectors.toSet());
        for(var transition : followingTransitions){
            if(!parameter1.evaluate(transition, paths)) return false;
        }
        for(var place : followingPlaces){
            if(!parameter2.evaluate(place, paths)) return false;
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
