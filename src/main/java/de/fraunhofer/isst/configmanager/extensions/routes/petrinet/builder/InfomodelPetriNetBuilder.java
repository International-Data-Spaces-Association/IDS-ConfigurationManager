/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.builder;

import de.fraunhofer.iais.eis.AppRoute;
import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.RouteStep;
import de.fraunhofer.iais.eis.Rule;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.Formula;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Arc;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.ArcImpl;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.ContextObject;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.PetriNet;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.PetriNetImpl;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Place;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.PlaceImpl;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Transition;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.TransitionImpl;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.policy.PolicyUtils;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.policy.RuleFormulaBuilder;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.policy.RuleUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provide static methods, to generate a Petri Net (https://en.wikipedia.org/wiki/Petri_net) from an Infomodel AppRoute.
 */
@Slf4j
@UtilityClass
public class InfomodelPetriNetBuilder {

    /**
     * Generate a Petri Net from a given infomodel {@link AppRoute}.
     * RouteSteps will be represented as Places, Endpoints as Transitions.
     *
     * @param includeAppRoute AppRoute to be included
     * @param appRoute an Infomodel {@link AppRoute}
     * @return a Petri Net created from the AppRoute
     */
    public static PetriNet petriNetFromAppRoute(final AppRoute appRoute,
                                                final boolean includeAppRoute) {
        //create sets for places, transitions and arcs
        final var places = new HashMap<URI, Place>();
        final var transitions = new HashMap<URI, Transition>();
        final var arcs = new HashSet<Arc>();

        if (includeAppRoute) {
            //create initial place from AppRoute
            final var place = new PlaceImpl(appRoute.getId());
            places.put(place.getID(), place);

            //for every AppRouteStart create a Transition and add AppRouteStart -> AppRoute
            for (final var endpoint : appRoute.getAppRouteStart()) {
                final var trans = (TransitionImpl) getTransition(transitions, endpoint);
                Set<String> writes = new HashSet<>();
                if (appRoute.getAppRouteOutput() != null && !appRoute.getAppRouteOutput().isEmpty()) {
                    writes = appRoute.getAppRouteOutput().stream().map(Resource::getId).map(URI::toString).collect(Collectors.toCollection(HashSet::new));
                }
                if (trans.getContext() == null) {
                    trans.setContextObject(new ContextObject(endpoint.getEndpointInformation().stream().map(TypedLiteral::getValue).collect(Collectors.toSet()), new HashSet<>(), writes, new HashSet<>(), ContextObject.TransType.APP));
                } else {
                    trans.getContext().getWrite().addAll(writes);
                }
                final var arc = new ArcImpl(trans, place);
                arcs.add(arc);
            }

            //for every AppRouteEnd create a Transition and add AppRoute -> AppRouteEnd
            for (final var endpoint : appRoute.getAppRouteEnd()) {
                final var trans = (TransitionImpl) getTransition(transitions, endpoint);
                Set<String> reads = new HashSet<>();
                if (appRoute.getAppRouteOutput() != null && !appRoute.getAppRouteOutput().isEmpty()) {
                    reads = appRoute.getAppRouteOutput().stream().map(Resource::getId).map(URI::toString).collect(Collectors.toCollection(HashSet::new));
                }
                if (trans.getContext() == null) {
                    trans.setContextObject(new ContextObject(endpoint.getEndpointInformation()
                            .stream().map(TypedLiteral::getValue)
                            .collect(Collectors.toSet()), reads, Set.of(), Set.of(), ContextObject.TransType.APP));
                } else {
                    trans.getContext().getRead().addAll(reads);
                }
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
            Set<String> writes = new HashSet<>();
            if (subRoute.getAppRouteOutput() != null && !subRoute.getAppRouteOutput().isEmpty()) {
                writes = subRoute.getAppRouteOutput()
                        .stream().map(Resource::getId)
                        .map(URI::toString).collect(Collectors.toCollection(HashSet::new));
            }
            final var context = endpoint.getEndpointInformation() != null ? endpoint.getEndpointInformation()
                    .stream().map(TypedLiteral::getValue)
                    .collect(Collectors.toSet()) : new HashSet<String>();
            if (trans.getContext() == null) {
                trans.setContextObject(new ContextObject(context, new HashSet<>(), writes, new HashSet<>(), ContextObject.TransType.APP));
            } else {
                trans.getContext().getWrite().addAll(writes);
            }
            final var arc = new ArcImpl(trans, place);
            arcs.add(arc);
        }

        //for every AppRouteEnd create a transition and add SubRoute -> AppRouteEnd
        for (final var endpoint : subRoute.getAppRouteEnd()) {
            final var trans = (TransitionImpl) getTransition(transitions, endpoint);
            Set<String> reads = new HashSet<>();
            if (subRoute.getAppRouteOutput() != null && !subRoute.getAppRouteOutput().isEmpty()) {
                reads = subRoute.getAppRouteOutput().stream().map(Resource::getId).map(URI::toString).collect(Collectors.toCollection(HashSet::new));
            }
            final var context = endpoint.getEndpointInformation() != null ? endpoint.getEndpointInformation().stream().map(TypedLiteral::getValue).collect(Collectors.toSet()) : new HashSet<String>();
            if (trans.getContext() == null) {
                trans.setContextObject(new ContextObject(context, reads, new HashSet<>(), new HashSet<>(), ContextObject.TransType.APP));
            } else {
                trans.getContext().getRead().addAll(reads);
            }
            final var arc = new ArcImpl(place, trans);
            arcs.add(arc);
        }
    }

    /**
     * @param petriNet petrinet created from infomodel approute
     * @return petrinet with filled writes and reads in contextobj
     */
    public PetriNet fillWriteAndErase(final PetriNet petriNet) {
        //TODO check if everything is filled properly
        final var transitions = petriNet.getNodes().stream()
                .filter(node -> node instanceof Transition)
                .collect(Collectors.toList());
        for (final var trans : transitions) {
            final var previous = trans.getTargetArcs().stream()
                    .map(Arc::getSource)
                    .map(Node::getTargetArcs)
                    .flatMap(Collection::stream)
                    .map(Arc::getSource)
                    .filter(node -> node instanceof TransitionImpl)
                    .map(node -> ((TransitionImpl) node).getContext().getWrite())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            ((TransitionImpl) trans).getContext().setRead(previous);
            if (((TransitionImpl) trans).getContext().getType() == ContextObject.TransType.APP) {
                final var writeSplit = ((TransitionImpl) trans).getContext().getWrite();
                final var erased = previous.stream().filter(x -> !writeSplit.contains(x)).collect(Collectors.toSet());
                ((TransitionImpl) trans).getContext().setErase(erased);
            } else {
                ((TransitionImpl) trans).getContext().setWrite(previous);
            }
        }
        return petriNet;
    }

    /**
     * Get the transition for the given {@link Endpoint} by ID, or generate a new one if no transition for that endpoint exists.
     *
     * @param transitions the transition that will be created or found in the map
     * @param endpoint the endpoint for which the transition should be found
     * @return the existing transition with id from the map, or a new transition
     */
    private static Transition getTransition(final Map<URI, Transition> transitions,
                                            final Endpoint endpoint) {
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

    /**
     * Add an initial CONTROL place (and transition) before all places with markers.
     *
     * @param petriNet a PetriNet
     * @return petrinet with initial control transition
     */
    public static PetriNet addControlTransitions(final PetriNet petriNet) {
        final var initials = petriNet.getNodes()
                .stream().filter(node -> node instanceof Place && ((Place) node).getMarkers() >= 1).collect(Collectors.toSet());

        final var controlTrans = new TransitionImpl(URI.create("trans://controlStart"));
        final var controlPlace = new PlaceImpl(URI.create("place://controlPlace"));

        controlPlace.setMarkers(1);
        controlTrans.setContextObject(new ContextObject(Set.of(), Set.of(), Set.of(), Set.of(), ContextObject.TransType.CONTROL));

        final var controlArc = new ArcImpl(controlPlace, controlTrans);

        petriNet.getArcs().add(controlArc);
        petriNet.getNodes().add(controlPlace);
        petriNet.getNodes().add(controlTrans);

        for (final var startPlace : initials) {
            final var arc = new ArcImpl(controlTrans, startPlace);
            ((Place) startPlace).setMarkers(0);
            petriNet.getArcs().add(arc);
        }
        return petriNet;
    }

    /**
     * @param appRoute the AppRoute to extract policies from
     * @return List of Formulas representing the AppRoutes policies
     */
    public static List<Formula> extractPoliciesFromAppRoute(final AppRoute appRoute) {
        final List<Formula> formulas = new ArrayList<>();
        final var resourceStream = resourceStream(appRoute);
        final var resources = resourceStream.collect(Collectors.toSet());
        for (final var resource : resources) {
            formulas.addAll(formulasFromResource(resource));
        }
        return formulas;
    }

    /**
     * @param appRoute an infomodel approute
     * @return stream of all resources in the approute
     */
    private static Stream<Resource> resourceStream(final AppRoute appRoute) {
        return Stream.concat(
                appRoute.getAppRouteOutput().stream(),
                appRoute.getHasSubRoute() != null ? appRoute.getHasSubRoute().stream().flatMap(InfomodelPetriNetBuilder::resourceStream) : Stream.empty()
        );
    }

    /**
     * @param resource resource for which contract offer rules are transformed to policies
     * @return List of Formulas representing the policies tied to the resource
     */
    private static List<Formula> formulasFromResource(final Resource resource) {
        final var offers = resource.getContractOffer();
        if (offers == null || offers.isEmpty()) {
            return List.of();
        }
        final List<Formula> formulas = new ArrayList<>();
        for (final var offer : offers) {
            final var rules = PolicyUtils.getRulesForTargetId(offer, resource.getId());
            for (final var rule : rules) {
                final var formula = buildFormulaFromRule(rule, resource.getId());
                if (formula != null) {
                    formulas.add(formula);
                }
            }
        }
        return formulas;
    }

    /**
     * @param rule a rule which will be transformed to a {@link Formula}
     * @param resourceID resourceID for which the rule is applied
     * @return Formula, representing the given rule
     */
    private static Formula buildFormulaFromRule(final Rule rule, final URI resourceID) {
        final var pattern = RuleUtils.getPatternByRule(rule);

        if (log.isInfoEnabled()) {
            log.info(pattern.toString());
        }

        return RuleFormulaBuilder.buildFormula(pattern, rule, resourceID);
    }
}
