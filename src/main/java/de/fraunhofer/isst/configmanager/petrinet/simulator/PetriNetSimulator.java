package de.fraunhofer.isst.configmanager.petrinet.simulator;

import de.fraunhofer.isst.configmanager.petrinet.builder.GraphVizGenerator;
import de.fraunhofer.isst.configmanager.petrinet.model.*;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
@UtilityClass
public class PetriNetSimulator {
    /**
     * Make a step in the current petriNet, finding all transitions that can be used
     * and taking all of them
     * (normally a petri net only uses one random transition at a time
     *  TODO: only take one random transition at a time)
     * @param petriNet the current step in the petrinet
     * @return true if something in the petrinet changed after taking the transitions
     */
    private static boolean makeStep(final PetriNet petriNet){
        var changed = false;
        final var nodesLosingMarkers = new ArrayList<Node>();
        final var nodesGainingMarkers = new ArrayList<Node>();

        for (final var node : petriNet.getNodes()) {
            if (node instanceof TransitionImpl) {
                final var allPreviousHaveMarker = isPossible(node);

                if (allPreviousHaveMarker) {
                    nodesLosingMarkers.addAll(node.getTargetArcs().stream()
                            .map(Arc::getSource).collect(Collectors.toList()));
                    nodesGainingMarkers.addAll(node.getSourceArcs().stream()
                            .map(Arc::getTarget).collect(Collectors.toList()));
                    changed = true;
                }
            }
        }

        nodesGainingMarkers.stream().distinct().forEach(node -> ((PlaceImpl) node).setMarkers(((PlaceImpl) node).getMarkers() + 1));

        nodesLosingMarkers.stream().distinct().forEach(node -> ((PlaceImpl) node).setMarkers(((PlaceImpl) node).getMarkers() - 1));

        return changed;
    }
    
    /**
     * For a given initial PetriNet: execute a step as long as something changes
     * @param petriNet the initial PetriNet
     */
    public static void simulateNet(final PetriNet petriNet){
        if (log.isInfoEnabled()) {
            log.info("Starting Simulation!");
            log.info(GraphVizGenerator.generateGraphViz(petriNet));
        }

        while (makeStep(petriNet)) {
            if (log.isInfoEnabled()) {
                log.info("Something changed!");
                log.info(GraphVizGenerator.generateGraphViz(petriNet));
            }
        }

        if (log.isInfoEnabled()) {
            log.info("Nothing changed! Finished simulation of PetriNet!");
        }
    }
    
    /**
     * For a given petriNet, find all transitions that can be taken
     * @param petriNet a given PetriNet
     * @return List of Transition nodes, for which all previous nodes have markers
     */
    private static List<Node> getPossibleTransitions(final PetriNet petriNet){
        final var possible = new ArrayList<Node>();

        for (final var node : petriNet.getNodes()) {
            if (isPossible(node)) {
                possible.add(node);
            }
        }

        return possible;
    }
    
    /**
     * Given a petri net and a (transition) node: do the transition
     * @param petriNet a given PetriNet
     * @param node a given Node of the PetriNet
     */
    private static void doTransition(final PetriNet petriNet, final Node node){
        if (!isPossible(node)){
            return;
        }

        for (final var arc : node.getTargetArcs()) {
            final var place = (PlaceImpl) arc.getSource();
            place.setMarkers(place.getMarkers() - 1);
        }

        for (final var arc : node.getSourceArcs()) {
            final var place = (PlaceImpl) arc.getTarget();
            place.setMarkers(place.getMarkers() + 1);
        }
    }
    
    /**
     * Build a StepGraph with the given PetriNet as starting Point for executions.
     *
     * @param petriNet the initial PetriNet
     * @return the StepGraph with all reachable states of the given PetriNet
     */
    public static StepGraph buildStepGraph(final PetriNet petriNet){
        final var stepGraph = new StepGraph(petriNet);
        stepGraph.getSteps().add(petriNet);

        for (final var node : getPossibleTransitions(petriNet)) {
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
    private static void addStepToStepGraph(final PetriNet parent,
                                           final PetriNet copy,
                                           final Node transition,
                                           final StepGraph stepGraph){
        Node transitionCopy = null;
        for (final var node : copy.getNodes()) {
            if (node.getID().equals(transition.getID())) {
                transitionCopy = node;
            }
        }

        doTransition(copy, transitionCopy);

        for (final var net : stepGraph.getSteps()) {
            if (net.equals(copy)){
                stepGraph.getArcs().add(new NetArc(parent, net, transition.getID()));
                return;
            }
        }

        stepGraph.getArcs().add(new NetArc(parent, copy, transition.getID()));
        stepGraph.getSteps().add(copy);

        for (final var node : getPossibleTransitions(copy)) {
            addStepToStepGraph(copy, copy.deepCopy(), node, stepGraph);
        }
    }
    
    /**
     * For a given node: if it is a transition, check if all previous nodes have markers
     * @param node a given Node
     * @return true if it is a transition ready to be used
     */
    private static boolean isPossible(final Node node) {
        if (node instanceof TransitionImpl) {
            return node.getTargetArcs().stream()
                    .map(Arc::getSource)
                    .map(PlaceImpl.class::cast)
                    .map(PlaceImpl::getMarkers)
                    .allMatch(markers -> markers > 0);
        }

        return false;
    }

    /**
     * @param stepGraph PetriNet StepGraph
     * @return all paths possible in given petriNet
     */
    public static List<List<Node>> getAllPaths(final StepGraph stepGraph){
        final var len1 = getPathsOfLength1(stepGraph);
        List<List<Node>> lenN = new ArrayList<>(len1);
        final List<List<Node>> allPaths = new ArrayList<>(len1);

        var i = 1;

        while (!lenN.isEmpty()) {
            if (log.isInfoEnabled()) {
                log.info("Calculating paths of length " + ++i);
            }
            lenN = getPathsOfLengthNplus1(len1, lenN);

            if (!lenN.isEmpty()) {
                allPaths.addAll(lenN);
            }
        }

        allPaths.sort(Comparator.comparingInt(List::size));

        return filterPaths(allPaths);
    }

    /**
     * @param stepGraph PetriNet StepGraph
     * @return all possible paths of length 1 (either Place -> Transition or Transition -> Place)
     */
    private static List<List<Node>> getPathsOfLength1(final StepGraph stepGraph){
        final List<List<Node>> paths = new ArrayList<>();

        for (final var node : stepGraph.getInitial().getNodes()) {
            if (node instanceof Place) {
                final var followingTransitions = node.getSourceArcs().stream().map(Arc::getTarget)
                        .filter(trans -> stepGraph.getArcs().stream()
                                .map(NetArc::getUsedTransition)
                                .anyMatch(used -> used.equals(trans.getID())))
                        .collect(Collectors.toList());

                for (final var succ : followingTransitions) {
                    paths.add(List.of(node, succ));
                }
            }

            if (node instanceof Transition) {
                for(final var succ : node.getSourceArcs().stream().map(Arc::getTarget).collect(Collectors.toSet())){
                    paths.add(List.of(node, succ));
                }
            }
        }
        return paths;
    }

    /**
     * @param pathsLen1 set of possible paths of length 1
     * @param pathsLenN all possible paths of length n (stop considering as soon as path gets circular (first = last))
     * @return all possible paths of length n+1
     */
    private static List<List<Node>> getPathsOfLengthNplus1(final List<List<Node>> pathsLen1,
                                                           final List<List<Node>> pathsLenN) {
        final List<List<Node>> pathsLenNplus1 = new ArrayList<>();

        for (final var pathN : pathsLenN) {
            for (final var path1 : pathsLen1) {
                if (pathN.get(pathN.size() - 1).equals(path1.get(0)) && circleFree(pathN)) {
                    final var pathNplus1 = new ArrayList<>(pathN);

                    pathNplus1.add(path1.get(path1.size()-1));
                    pathsLenNplus1.add(pathNplus1);
                }
            }
        }

        return pathsLenNplus1;
    }

    public static boolean circleFree(final List list){
        return list.stream().distinct().count() == list.size();
    }

    public static List<List<Object>> calculatePaths(final StepGraph stepGraph) {
        final var len1 =calcPathsOfLength1(stepGraph);
        List<List<Object>> lenN = new ArrayList<>(len1);

        lenN = lenN.stream().filter(path -> path.get(0).equals(stepGraph.getInitial())).collect(Collectors.toList());

        final List<List<Object>> allPaths = new ArrayList<>(lenN);

        var i = 1;

        while (!lenN.isEmpty()) {
            if (log.isInfoEnabled()) {
                log.info("Calculating paths of length " + ++i);
            }

            lenN = calcPathsOfLengthNplus1(len1, lenN);

            if (log.isInfoEnabled()) {
                log.info(String.format("Length %d: %d", i, lenN.size()));
            }

            if (!lenN.isEmpty()) {
                allPaths.addAll(lenN);
            }
        }

        allPaths.sort(Comparator.comparingInt(List::size));

        return allPaths;
    }

    private static List<List<Object>> calcPathsOfLength1(final StepGraph stepGraph) {
        final List<List<Object>> paths = new ArrayList<>();

        for (final var arc : stepGraph.getArcs()) {
            final var sourcePart = new ArrayList<>();
            sourcePart.add(arc.getSource());
            sourcePart.add(arc.getUsedTransition());
            paths.add(sourcePart);

            final var targetPart = new ArrayList<>();
            targetPart.add(arc.getUsedTransition());
            targetPart.add(arc.getTarget());
            paths.add(targetPart);
        }
        return paths;
    }

    private static List<List<Object>> calcPathsOfLengthNplus1(final List<List<Object>> len1,
                                                              final List<List<Object>> lenN){
        final List<List<Object>> pathsLenNplus1 = new ArrayList<>();

        for (final var pathN : lenN) {
            for (final var path1 : len1) {
                if(pathN.get(pathN.size()-1).equals(path1.get(0)) && circleFree(pathN)){
                    final var pathNplus1 = new ArrayList<>(pathN);
                    pathNplus1.add(path1.get(path1.size()-1));
                    pathsLenNplus1.add(pathNplus1);
                }
            }
        }
        return pathsLenNplus1;
    }

    public static PetriNet getUnfoldedPetriNet(PetriNet petriNet){
        var unfolded = petriNet.deepCopy();
        var transitions = unfolded.getNodes().stream().filter(trans -> trans instanceof Transition).filter(trans -> ((Transition) trans).getContext().getType() == ContextObject.TransType.APP).collect(Collectors.toList());
        for(var transition : transitions){
            unfolded.getNodes().remove(transition);
            var transpart1 = new TransitionImpl(URI.create(String.format("%s_start", transition.getID().toString())));
            var transpart2 = new TransitionImpl(URI.create(String.format("%s_end", transition.getID().toString())));
            var transplace = new InnerPlace(URI.create(String.format("%s_place", transition.getID().toString())), (Transition) transition);
            unfolded.getNodes().add(transpart1);
            unfolded.getNodes().add(transpart2);
            unfolded.getNodes().add(transplace);
            var innerArc1 = new ArcImpl(transpart1, transplace);
            var innerArc2 = new ArcImpl(transplace, transpart2);
            unfolded.getArcs().add(innerArc1);
            unfolded.getArcs().add(innerArc2);
            var targetArcs = transition.getTargetArcs();
            var sourceArcs = transition.getSourceArcs();
            unfolded.getArcs().removeAll(targetArcs);
            unfolded.getArcs().removeAll(sourceArcs);
            for(var arc : targetArcs){
                var newArc = new ArcImpl(arc.getSource(), transpart1);
                unfolded.getArcs().add(newArc);
            }
            for(var arc : sourceArcs){
                var newArc = new ArcImpl(transpart2, arc.getTarget());
                unfolded.getArcs().add(newArc);
            }
        }
        return unfolded;
    }

    public static List<List<Transition>> getParallelSets(StepGraph stepGraph){
        List<List<Transition>> parallelSets = new ArrayList<>();
        for(var step : stepGraph.getSteps()){
            var parallelTrans = step.getNodes().stream().filter(node -> node instanceof InnerPlace)
                    .filter(place -> ((InnerPlace) place).getMarkers() > 0)
                    .map(place -> ((InnerPlace) place).getOriginalTrans())
                    .distinct()
                    .collect(Collectors.toList());
            if(parallelTrans.size() >= 2) parallelSets.add(parallelTrans);
        }
        return parallelSets;
    }

    private static List<List<Node>> filterPaths(final List<List<Node>> paths) {
        final List<List<Node>> filtered = new ArrayList<>(List.copyOf(paths));

        final var filteredCopy = new ArrayList<>(filtered);

        for (final var pathY : filteredCopy) {
            for (final var pathX : paths) {
                if(pathX.get(0).equals(pathY.get(0)) && !pathX.equals(pathY) && Collections.indexOfSubList(pathY, pathX) != -1){
                    filtered.remove(pathX);
                }
            }
        }
        //put circlefree paths at beginnig of list, so they get checked first
        filtered.sort((o1, o2) -> Boolean.compare(circleFree(o2), circleFree(o1)));
        return filtered;
    }
}
