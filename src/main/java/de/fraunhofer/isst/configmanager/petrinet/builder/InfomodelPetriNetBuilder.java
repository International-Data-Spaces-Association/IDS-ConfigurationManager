package de.fraunhofer.isst.configmanager.petrinet.builder;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.Formula;
import de.fraunhofer.isst.configmanager.petrinet.model.*;
import lombok.experimental.UtilityClass;

import java.net.URI;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.TT.TT;

/**
 * Provide static methods, to generate a Petri Net (https://en.wikipedia.org/wiki/Petri_net) from an Infomodel AppRoute.
 */
@UtilityClass
public class InfomodelPetriNetBuilder {

    /**
     * Generate a Petri Net from a given infomodel {@link AppRoute}.
     * RouteSteps will be represented as Places, Endpoints as Transitions.
     *
     * @param appRoute an Infomodel {@link AppRoute}
     * @return a Petri Net created from the AppRoute
     */
    public static PetriNet petriNetFromAppRoute(final AppRoute appRoute,
                                                final boolean includeAppRoute) {
        //create sets for places, transitions and arcs
        final var places = new HashMap<URI, Place>();
        final var transitions = new HashMap<URI, Transition>();
        final var arcs = new HashSet<Arc>();

        if (includeAppRoute){
            //create initial place from AppRoute
            final var place = new PlaceImpl(appRoute.getId());
            places.put(place.getID(), place);

            //for every AppRouteStart create a Transition and add AppRouteStart -> AppRoute
            for (final var endpoint : appRoute.getAppRouteStart()) {
                final var trans = (TransitionImpl) getTransition(transitions, endpoint);
                var writes = appRoute.getAppRouteOutput().stream().map(Resource::getId).collect(Collectors.toList());
                trans.setContextObject(new ContextObject(List.of(), "", writes.toString(), "", ContextObject.TransType.APP));
                final var arc = new ArcImpl(trans, place);
                arcs.add(arc);
            }

            //for every AppRouteEnd create a Transition and add AppRoute -> AppRouteEnd
            for (final var endpoint : appRoute.getAppRouteEnd()) {
                final var trans = (TransitionImpl) getTransition(transitions, endpoint);
                trans.setContextObject(new ContextObject(List.of(), "", "", "", ContextObject.TransType.CONTROL));
                final var arc = new ArcImpl(place, trans);
                arcs.add(arc);
            }
        }

        //add every SubRoute of the AppRoute to the PetriNet
        for (final var subroute : appRoute.getHasSubRoute()) {
            addSubRouteToPetriNet(subroute, arcs, places, transitions);
        }

        //create a PetriNet with all Arcs, Transitions and Places from the AppRoute
        final var nodes = new HashSet<Node>();
        nodes.addAll(places.values());
        nodes.addAll(transitions.values());

        final var petriNet = new PetriNetImpl(appRoute.getId(), nodes, arcs);
        addFirstAndLastNode(petriNet);

        return petriNet;
    }

    /**
     * @param petriNet petrinet created from infomodel approute
     * @return petrinet with filled writes and reads in contextobj
     */
    private PetriNet fillWriteAndErase(PetriNet petriNet){
        //TODO check if everything is filled properly
        var transitions = petriNet.getNodes().stream()
                .filter(node -> node instanceof Transition)
                .collect(Collectors.toList());
        for(var trans : transitions){
            var previous = trans.getTargetArcs().stream()
                    .map(Arc::getSource)
                    .map(Node::getTargetArcs)
                    .flatMap(Collection::stream)
                    .map(Arc::getSource)
                    .filter(node -> node instanceof TransitionImpl)
                    .map(node -> ((TransitionImpl) node).getContext().getWrite())
                    .filter(Predicate.not(String::isEmpty))
                    .collect(Collectors.joining(","));
            ((TransitionImpl) trans).getContext().setRead(previous);
            if(((TransitionImpl) trans).getContext().getType() == ContextObject.TransType.APP){
                var readSplit = previous.split(",");
                var writeSplit = List.of(((TransitionImpl) trans).getContext().getWrite().split(","));
                var erased = Arrays.stream(readSplit).filter(x -> !writeSplit.contains(x)).collect(Collectors.joining(","));
                ((TransitionImpl) trans).getContext().setErase(erased);
            }else{
                ((TransitionImpl) trans).getContext().setWrite(previous);
            }
        }
        return petriNet;
    }

    /**
     * Add a {@link RouteStep} to the Petri Net as a new Subroute.
     *
     * @param subRoute the subRoute that will be added to the current Petri Net
     * @param arcs list of arcs of the current Petri Net
     * @param places list of places of the current Petri Net
     * @param transitions list of transitions of the current Petri Net
     */
    private static void addSubRouteToPetriNet(final RouteStep subRoute,
                                              final Set<Arc> arcs,
                                              final Map<URI, Place> places,
                                              final Map<URI, Transition> transitions) {

        //if a place with subroutes ID already exists in the map, the SubRoute was already added to the Petri Net
        if (places.containsKey(subRoute.getId())) {
            return;
        }

        //create a new place from the subRoute
        final var place = new PlaceImpl(subRoute.getId());
        places.put(place.getID(), place);

        //for every AppRouteStart create a transition and add AppRouteStart -> SubRoute
        for (final var endpoint : subRoute.getAppRouteStart()) {
            final var trans = (TransitionImpl) getTransition(transitions, endpoint);
            var writes = subRoute.getAppRouteOutput().stream().map(Resource::getId).collect(Collectors.toList());
            trans.setContextObject(new ContextObject(List.of(), "", writes.toString(), "", ContextObject.TransType.APP));
            final var arc = new ArcImpl(trans, place);
            arcs.add(arc);
        }

        //for every AppRouteEnd create a transition and add SubRoute -> AppRouteEnd
        for (final var endpoint : subRoute.getAppRouteEnd()) {
            final var trans = (TransitionImpl) getTransition(transitions, endpoint);
            trans.setContextObject(new ContextObject(List.of(), "", "", "", ContextObject.TransType.CONTROL));
            final var arc = new ArcImpl(place, trans);
            arcs.add(arc);
        }
    }

    /**
     * Get the transition for the given {@link Endpoint} by ID, or generate a new one if no transition for that endpoint exists.
     *
     * @param transitions the transition that will be created or found in the map
     * @param endpoint the endpoint for which the transition should be found
     * @return the existing transition with id from the map, or a new transition
     */
    private static Transition getTransition(final Map<URI, Transition> transitions,
                                            final Endpoint endpoint){
        if (transitions.containsKey(endpoint.getId())) {
            return transitions.get(endpoint.getId());
        } else {
            final var trans = new TransitionImpl(endpoint.getId());
            transitions.put(trans.getID(), trans);
            return trans;
        }
    }

    /**
     * Add a source node to every transition without input and a sink node to every transition without output.
     *
     * @param petriNet petrinet to which first and last places are added
     */
    private static void addFirstAndLastNode(final PetriNet petriNet) {
        final var first = new PlaceImpl(URI.create("place://source"));
        final var last = new PlaceImpl(URI.create("place://sink"));

        first.setMarkers(1);

        for (final var node : petriNet.getNodes()) {
            if (node instanceof TransitionImpl) {
                //if node has no arc with itself as target, add arc: first->node
                if (node.getTargetArcs().isEmpty()) {
                    final var arc = new ArcImpl(first, node);
                    petriNet.getArcs().add(arc);
                }
                //if node has no arc with itself as source, add arc: node->last
                if (node.getSourceArcs().isEmpty()) {
                    final var arc = new ArcImpl(node, last);
                    petriNet.getArcs().add(arc);
                }
            }
        }
        petriNet.getNodes().add(first);
        petriNet.getNodes().add(last);
    }

    private static List<Formula> extractPoliciesFromAppRoute(AppRoute appRoute){
        List<Formula> formulas = new ArrayList<>();
        var resources = appRoute.getAppRouteOutput();
        for(var resource : resources){
            formulas.addAll(formulasFromResource(resource));
        }
        for(var route : appRoute.getHasSubRoute()){
            formulas.addAll(extractPoliciesFromSubRoute(route));
        }
        return formulas;
    }

    private static List<Formula> extractPoliciesFromSubRoute(RouteStep subroute){
        List<Formula> formulas = new ArrayList<>();
        for(var resource : subroute.getAppRouteOutput()){
            formulas.addAll(formulasFromResource(resource));
        }
        for(var route: subroute.getHasSubRoute()){
            formulas.addAll(extractPoliciesFromSubRoute(route));
        }
        return formulas;
    }

    private static List<Formula> formulasFromResource(Resource resource){
        var offers = resource.getContractOffer();
        List<Formula> formulas = new ArrayList<>();
        for(var offer : offers){
            formulas.add(buildFormulaFromContractOffer(offer, resource.getId()));
        }
        return formulas;
    }

    private static Formula buildFormulaFromContractOffer(ContractOffer offer, URI resourceID){
        //TODO build formula from Contract Offer
        return null;
    }
}
