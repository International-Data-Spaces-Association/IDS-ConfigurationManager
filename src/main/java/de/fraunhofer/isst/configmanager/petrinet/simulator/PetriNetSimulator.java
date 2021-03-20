package de.fraunhofer.isst.configmanager.petrinet.simulator;

import de.fraunhofer.isst.configmanager.petrinet.builder.GraphVizGenerator;
import de.fraunhofer.isst.configmanager.petrinet.model.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class Providing static methods to simulate a PetriNet based on a given initial state,
 * or creating the graph of all possible steps the PetriNet can take in an execution.
 *
 * (both methods could be running indefinitely, if the given initial PetriNet contains a
 * marker generating circle, so the PetriNet has an infinite amount of reachable states)
 */
@Slf4j
public class PetriNetSimulator {
    /**
     * Make a step in the current petriNet, finding all transitions that can be used
     * and taking all of them
     * (normally a petri net only uses one random transition at a time
     *  TODO: only take one random transition at a time)
     * @param petriNet the current step in the petrinet
     * @return true if something in the petrinet changed after taking the transitions
     */
    private static boolean makeStep(PetriNet petriNet){
        var changed = false;
        var nodesLosingMarkers = new ArrayList<Node>();
        var nodesGainingMarkers = new ArrayList<Node>();
        for(var node : petriNet.getNodes()){
            if(node instanceof TransitionImpl){
                var allPreviousHaveMarker = isPossible(node);
                if(allPreviousHaveMarker){
                    nodesLosingMarkers.addAll(node.getTargetArcs().stream()
                            .map(Arc::getSource).collect(Collectors.toList()));
                    nodesGainingMarkers.addAll(node.getSourceArcs().stream()
                            .map(Arc::getTarget).collect(Collectors.toList()));
                    changed = true;
                }
            }
        }
        nodesGainingMarkers.stream().distinct().forEach(node -> {
            ((PlaceImpl) node).setMarkers(((PlaceImpl) node).getMarkers() + 1);
        });
        nodesLosingMarkers.stream().distinct().forEach(node -> {
            ((PlaceImpl) node).setMarkers(((PlaceImpl) node).getMarkers() - 1);
        });
        return changed;
    }
    
    /**
     * For a given initial PetriNet: execute a step as long as something changes
     * @param petriNet the initial PetriNet
     */
    public static void simulateNet(PetriNet petriNet){
        int i = 0;;
        log.info("Starting Simulation!");
        log.info(GraphVizGenerator.generateGraphViz(petriNet));
        while(makeStep(petriNet)){
            log.info("Something changed!");
            i++;
            log.info(GraphVizGenerator.generateGraphViz(petriNet));
        }
        log.info("Nothing changed! Finished simulation of PetriNet!");
    }
    
    /**
     * For a given petriNet, find all transitions that can be taken
     * @param petriNet a given PetriNet
     * @return List of Transition nodes, for which all previous nodes have markers
     */
    private static List<Node> getPossibleTransitions(PetriNet petriNet){
        var possible = new ArrayList<Node>();
        for(var node : petriNet.getNodes()){
            if(isPossible(node)) possible.add(node);
        }
        return possible;
    }
    
    /**
     * Given a petri net and a (transition) node: do the transition
     * @param petriNet a given PetriNet
     * @param node a given Node of the PetriNet
     */
    private static void doTransition(PetriNet petriNet, Node node){
        if(!isPossible(node)){
            return;
        }
        for(var arc : node.getTargetArcs()){
            var place = (PlaceImpl) arc.getSource();
            place.setMarkers(place.getMarkers()-1);
        }
        for(var arc : node.getSourceArcs()){
            var place = (PlaceImpl) arc.getTarget();
            place.setMarkers(place.getMarkers()+1);
        }
    }
    
    /**
     * Build a StepGraph with the given PetriNet as starting Point for executions.
     *
     * @param petriNet the initial PetriNet
     * @return the StepGraph with all reachable states of the given PetriNet
     */
    public static StepGraph buildStepGraph(PetriNet petriNet){
        var stepGraph = new StepGraph();
        stepGraph.getSteps().add(petriNet);
        for(var node : getPossibleTransitions(petriNet)){
            addStepToStepGraph(petriNet, petriNet.deepCopy(), node, stepGraph);
        }
        return stepGraph;
    }
    
    /**
     * Execute a possible transition of the current PetriNet and add the result
     * to the StepGraph.
     *
     * @param parent the current PetriNet
     * @param copy a copy of the current PetriNet which will be modified
     * @param transition the transition the PetriNet should execute
     * @param stepGraph the stepgraph the resulting PetriNet will be added to
     *                  (if it doesn't already contain an equal PetriNet)
     */
    private static void addStepToStepGraph(PetriNet parent, PetriNet copy, Node transition, StepGraph stepGraph){
        log.info("Adding Step!");
        Node transitionCopy = null;
        for(var node : copy.getNodes()){
            if(node.getID().equals(transition.getID())){
                transitionCopy = node;
            }
        }
        doTransition(copy, transitionCopy);
        for(var net : stepGraph.getSteps()){
            if (net.equals(copy)){
                stepGraph.getArcs().add(new NetArc(parent, net));
                return;
            }
        }
        stepGraph.getArcs().add(new NetArc(parent, copy));
        stepGraph.getSteps().add(copy);
        for(var node : getPossibleTransitions(copy)){
            addStepToStepGraph(copy, copy.deepCopy(), node, stepGraph);
        }
    }
    
    /**
     * For a given node: if it is a transition, check if all previous nodes have markers
     * @param node a given Node
     * @return true if it is a transition ready to be used
     */
    private static boolean isPossible(Node node){
        if(node instanceof TransitionImpl) {
            return node.getTargetArcs().stream()
                    .map(Arc::getSource)
                    .map(place -> (PlaceImpl) place)
                    .map(PlaceImpl::getMarkers)
                    .allMatch(markers -> markers > 0);
        }
        return false;
    }

}
