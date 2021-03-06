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
package de.fraunhofer.isst.configmanager.petrinet.builder;

import de.fraunhofer.iais.eis.AppRouteBuilder;
import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.iais.eis.EndpointBuilder;
import de.fraunhofer.iais.eis.RouteStep;
import de.fraunhofer.iais.eis.RouteStepBuilder;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.CTLEvaluator;
import de.fraunhofer.isst.configmanager.petrinet.model.*;
import de.fraunhofer.isst.configmanager.petrinet.simulator.ParallelEvaluator;
import de.fraunhofer.isst.configmanager.petrinet.simulator.PetriNetSimulator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.FF.FF;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.TT.TT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeAND.nodeAND;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeEV.nodeEV;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeEXIST_UNTIL.nodeEXIST_UNTIL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeExpression.nodeExpression;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeFORALL_NEXT.nodeFORALL_NEXT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeMODAL.nodeMODAL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeNF.nodeNF;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeOR.nodeOR;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.ArcExpression.arcExpression;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionAF.transitionAF;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionAND.transitionAND;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionEV.transitionEV;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionMODAL.transitionMODAL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionNOT.transitionNOT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionOR.transitionOR;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionPOS.transitionPOS;

/**
 * Test building a PetriNet from a randomly generated AppRoute
 */
@Slf4j
class InfomodelPetriNetBuilderTest {
    private static final int MINIMUM_ENDPOINT = 5;
    private static final int MAXIMUM_ENDPOINT = 10;

    private static final int MINIMUM_SUBROUTE = 3;
    private static final int MAXIMUM_SUBROUTE = 5;

    private static final int MINIMUM_STARTEND = 1;
    private static final int MAXIMUM_STARTEND = 3;

    /**
     * Example: Generate a random PetriNet, try to simulate it and print out the GraphViz representation
     * Generated PetriNet can have an infinite amount of possible configurations, if this happens the
     * example will run indefinitely.
     */
    @Test
    @Disabled
    void testBuildPetriNet() throws IOException {
        //Randomly generate an AppRoute
        var endpointlist = new ArrayList<Endpoint>();
        for (int i = 0; i < ThreadLocalRandom.current().nextInt(MINIMUM_ENDPOINT, MAXIMUM_ENDPOINT); i++){
            endpointlist.add(new EndpointBuilder(URI.create("http://endpoint" + i)).build());
        }
        var subroutes = new ArrayList<RouteStep>();
        for (int i = 0; i < ThreadLocalRandom.current().nextInt(MINIMUM_SUBROUTE,MAXIMUM_SUBROUTE); i++){
            subroutes.add(new RouteStepBuilder(URI.create("http://subroute" + i))
                    ._appRouteStart_((ArrayList<Endpoint>) randomSubList(endpointlist))
                    ._appRouteEnd_((ArrayList<Endpoint>) randomSubList(endpointlist)).build());
        }
        var appRoute = new AppRouteBuilder(URI.create("http://approute"))
                ._appRouteStart_((ArrayList<Endpoint>) randomSubList(endpointlist))
                ._appRouteEnd_((ArrayList<Endpoint>) randomSubList(endpointlist))
                ._hasSubRoute_(subroutes)
                .build();

        //build a petriNet from the generated AppRoute and log generated GraphViz representation
        var petriNet = InfomodelPetriNetBuilder.petriNetFromAppRoute(appRoute, false);
        var ser = new Serializer();
        if (log.isInfoEnabled()) {
            log.info(ser.serialize(appRoute));
            log.info(GraphVizGenerator.generateGraphViz(petriNet));
        }

        //build a full Graph of all possible steps in the PetriNet and log generated GraphViz representation
        var graph = PetriNetSimulator.buildStepGraph(petriNet);

        if (log.isInfoEnabled()) {
            log.info(String.valueOf(graph.getArcs().size()));
            log.info(GraphVizGenerator.generateGraphViz(graph));
        }

        var allPaths = PetriNetSimulator.getAllPaths(graph);

        if (log.isInfoEnabled()) {
            log.info(allPaths.toString());
        }

        var formula = nodeAND(nodeMODAL(transitionNOT(FF())), nodeOR(nodeNF(nodeExpression(x -> true, "testMsg")),TT()));
        var formula2 = nodeAND(nodeFORALL_NEXT(nodeMODAL(transitionAF(arcExpression(x -> true,"")))), TT());
        var formula3 = nodeEXIST_UNTIL(nodeMODAL(TT()), nodeNF(nodeExpression(x -> x.getSourceArcs().isEmpty(), "")));

        if (log.isInfoEnabled()) {
            log.info("Formula 1: " + formula.writeFormula());
            log.info("Result: " + CTLEvaluator.evaluate(formula, graph.getInitial().getNodes().stream().filter(node -> node instanceof Place).findAny().get(), allPaths));
            log.info("Formula 2: " + formula2.writeFormula());
            log.info("Result: " + CTLEvaluator.evaluate(formula2, graph.getInitial().getNodes().stream().filter(node -> node instanceof Place).findAny().get(), allPaths));
            log.info("Formula 3: " + formula3.writeFormula());
            log.info("Result: " + CTLEvaluator.evaluate(formula3, graph.getInitial().getNodes().stream().filter(node -> node.getID().equals(URI.create("place://source"))).findAny().get(), allPaths));
        }
    }

    /**
     * Example: Create a set of Formulas and evaluate them on the example PetriNet
     */
    @Test
    @Disabled
    void testExamplePetriNet(){
        //build the example net and log DOT visualization
        var petriNet = buildPaperNet();
        log.info(GraphVizGenerator.generateGraphViz(petriNet));

        //build stepGraph
        var graph = PetriNetSimulator.buildStepGraph(petriNet);
        log.info(String.format("%d possible states!", graph.getSteps().size()));

        //get set of paths from calculated stepgraph
        var allPaths = PetriNetSimulator.getAllPaths(graph);
        log.info(String.format("Found %d valid Paths!", allPaths.size()));

        //Evaluate Formula 1: a transition is reachable, which reads data without 'france' in context, after that transition data is overwritten or erased (or an end is reached)
        var formulaFrance = transitionPOS(
                                            transitionAND(
                                                    transitionAF(arcExpression(x -> x.getContext().getRead() != null && x.getContext().getRead().equals("data") && !x.getContext().getContext().contains("france"), "")),
                                                    transitionEV(
                                                            transitionOR(
                                                                    transitionAF(arcExpression(x -> x.getContext().getWrite() != null && x.getContext().getWrite().equals("data") || x.getContext().getErase() != null && x.getContext().getErase().equals("data"), "")),
                                                                    transitionMODAL(nodeNF(nodeExpression(x -> x.getSourceArcs().isEmpty(), " ")))
                                                            )
                                                    )
                                            )
        );
        log.info("Formula France: " + formulaFrance.writeFormula());
        log.info("Result: " + CTLEvaluator.evaluate(formulaFrance, graph.getInitial().getNodes().stream().filter(node -> node.getID().equals(URI.create("trans://getData"))).findAny().get(), allPaths));

        //Evaluate Formula 2: a transition is reachable, which reads data
        var formulaDataUsage = nodeMODAL(transitionPOS(transitionAF(arcExpression(x -> x.getContext().getRead() != null && x.getContext().getRead().equals("data"), ""))));
        log.info("Formula Data: " + formulaDataUsage.writeFormula());
        log.info("Result: " + CTLEvaluator.evaluate(formulaDataUsage, graph.getInitial().getNodes().stream().filter(node -> node.getID().equals(URI.create("place://start"))).findAny().get(), allPaths));

        //Evaluate Formula 3: a transition is reachable, which is reading data. From there another transition is reachable, which also reads data, from this the end or a transition which overwrites or erases data is reachable.
        var formulaUseAndDelete = transitionPOS(
                                                transitionAND(
                                                        transitionAF(arcExpression(x -> x.getContext().getRead() != null && x.getContext().getRead().equals("data"), "")),
                                                        transitionPOS(
                                                                transitionAND(
                                                                    transitionAF(arcExpression(x -> x.getContext().getRead() != null || x.getContext().getRead().equals("data"), "")),
                                                                    transitionEV(
                                                                        transitionOR(
                                                                                transitionAF(arcExpression(x -> x.getContext().getWrite() != null && x.getContext().getWrite().equals("data") || x.getContext().getErase() != null && x.getContext().getErase().equals("data"), "")),
                                                                                transitionMODAL(nodeNF(nodeExpression(x -> x.getSourceArcs().isEmpty(), " ")))
                                                                        )
                                                                    )
                                                                )

                                                            )
                                                )
        );
        log.info("Formula Use And Delete: " + formulaUseAndDelete.writeFormula());
        log.info("Result: " + CTLEvaluator.evaluate(formulaUseAndDelete, graph.getInitial().getNodes().stream().filter(node -> node.getID().equals(URI.create("trans://getData"))).findAny().get(), allPaths));
    }

    /**
     * Example: Unfold the example PetriNet and check for parallel evaluations
     */
    @Test
    @Disabled
    void testUnfoldNet(){
        //build example petrinet
        var petriNet = buildPaperNet();

        //unfold and visualize example petrinet
        var unfolded = PetriNetSimulator.getUnfoldedPetriNet(petriNet);
        log.info(GraphVizGenerator.generateGraphViz(unfolded));

        //build step graph of unfolded net
        var unfoldedGraph = PetriNetSimulator.buildStepGraph(unfolded);
        log.info(String.format("Step Graph has %d possible combinations!", unfoldedGraph.getSteps().size()));

        //get possible parallel executions of transitions from the calculated stepgraph
        var parallelSets = PetriNetSimulator.getParallelSets(unfoldedGraph);
        log.info(String.format("Found %d possible parallel executions!", parallelSets.size()));

        //evaluate: 3 transitions are reading data in parallel
        var result = ParallelEvaluator.nParallelTransitionsWithCondition(x -> x.getContext().getRead() != null && x.getContext().getRead().equals("data"), 3, parallelSets);
        log.info(String.format("3 parallel reading Transitions: %s", result));
    }

    /**
     * @param input A List
     * @param <T> Generic Type for given list
     * @return a random sublist with a size between MINIMUM_STARTEND and MAXIMUM_STARTEND
     */
    public static <T> ArrayList<? extends T> randomSubList(List<T> input) {
        var newSize = ThreadLocalRandom.current().nextInt(MINIMUM_STARTEND,MAXIMUM_STARTEND);
        var list = new ArrayList<>(input);
        Collections.shuffle(list);
        ArrayList<T> newList = new ArrayList<>();
        for(int i = 0; i< newSize; i++){
            newList.add(list.get(i));
        }
        return newList;
    }

    /**
     * Build the example PetriNet from the paper, to evaluate formulas on
     * @return Example PetriNet described in the WFDU Paper
     */
    private PetriNet buildPaperNet(){
        //create nodes
        var start = new PlaceImpl(URI.create("place://start"));
        start.setMarkers(1);
        var copy = new PlaceImpl(URI.create("place://copy"));
        var init = new PlaceImpl(URI.create("place://init"));
        var dat1 = new PlaceImpl(URI.create("place://data1"));
        var dat2 = new PlaceImpl(URI.create("place://data2"));
        var con1 = new PlaceImpl(URI.create("place://control1"));
        var con2 = new PlaceImpl(URI.create("place://control2"));
        var con3 = new PlaceImpl(URI.create("place://control3"));
        var con4 = new PlaceImpl(URI.create("place://control4"));
        var sample = new PlaceImpl(URI.create("place://sample"));
        var mean = new PlaceImpl(URI.create("place://mean"));
        var med = new PlaceImpl(URI.create("place://median"));
        var rules = new PlaceImpl(URI.create("place://rules"));
        var stor1 = new PlaceImpl(URI.create("place://stored1"));
        var stor2 = new PlaceImpl(URI.create("place://stored2"));
        var stor3 = new PlaceImpl(URI.create("place://stored3"));
        var stor4 = new PlaceImpl(URI.create("place://stored4"));
        var end = new PlaceImpl(URI.create("place://end"));
        var nodes = new HashSet<Node>(List.of(start, copy, init, dat1, dat2, con1, con2, con3, con4, sample, mean, med, rules, stor1, stor2, stor3, stor4, end));
        //create transitions with context
        var initTrans = new TransitionImpl(URI.create("trans://init"));
        initTrans.setContextObject(new ContextObject(List.of(), null, null, null, ContextObject.TransType.CONTROL));
        var getData = new TransitionImpl(URI.create("trans://getData"));
        getData.setContextObject(new ContextObject(List.of(), null, "data", null, ContextObject.TransType.APP));
        var copyData = new TransitionImpl(URI.create("trans://copyData"));
        copyData.setContextObject(new ContextObject(List.of(""), "data", "data", null, ContextObject.TransType.APP));
        var extract = new TransitionImpl(URI.create("trans://extractSample"));
        extract.setContextObject(new ContextObject(List.of("france"), "data", "sample", "data", ContextObject.TransType.APP));
        var calcMean = new TransitionImpl(URI.create("trans://calcMean"));
        calcMean.setContextObject(new ContextObject(List.of("france"), "data", "mean", "data", ContextObject.TransType.APP));
        var calcMed = new TransitionImpl(URI.create("trans://calcMedian"));
        calcMed.setContextObject(new ContextObject(List.of("france"), "data", "median", "data", ContextObject.TransType.APP));
        var calcRules = new TransitionImpl(URI.create("trans://calcAPrioriRules"));
        calcRules.setContextObject(new ContextObject(List.of("france", "high_performance"), "data", "rules", "data", ContextObject.TransType.APP));
        var store1 = new TransitionImpl(URI.create("trans://storeData1"));
        store1.setContextObject(new ContextObject(List.of(), "sample", null, "sample", ContextObject.TransType.APP));
        var store2 = new TransitionImpl(URI.create("trans://storeData2"));
        store2.setContextObject(new ContextObject(List.of(), "mean", null, "mean", ContextObject.TransType.APP));
        var store3 = new TransitionImpl(URI.create("trans://storeData3"));
        store3.setContextObject(new ContextObject(List.of(), "median", null, "median", ContextObject.TransType.APP));
        var store4 = new TransitionImpl(URI.create("trans://storeData4"));
        store4.setContextObject(new ContextObject(List.of(), "rules", null, "rules", ContextObject.TransType.APP));
        var endTrans = new TransitionImpl(URI.create("trans://end"));
        endTrans.setContextObject(new ContextObject(List.of(), null, null, null, ContextObject.TransType.CONTROL));
        nodes.addAll(List.of(initTrans, getData, copyData, extract, calcMean, calcMed, calcRules, store1, store2, store3, store4, endTrans));
        //create arcs
        var arcs = new HashSet<Arc>();
        arcs.add(new ArcImpl(start, initTrans));
        arcs.add(new ArcImpl(initTrans, copy));
        arcs.add(new ArcImpl(initTrans, copy));
        arcs.add(new ArcImpl(initTrans, init));
        arcs.add(new ArcImpl(init, getData));
        arcs.add(new ArcImpl(getData, dat1));
        arcs.add(new ArcImpl(copy, copyData));
        arcs.add(new ArcImpl(dat1, copyData));
        arcs.add(new ArcImpl(copyData, dat1));
        arcs.add(new ArcImpl(copyData, dat2));
        arcs.add(new ArcImpl(getData, con1));
        arcs.add(new ArcImpl(getData, con2));
        arcs.add(new ArcImpl(getData, con3));
        arcs.add(new ArcImpl(getData, con4));
        arcs.add(new ArcImpl(dat2, extract));
        arcs.add(new ArcImpl(dat2, calcMean));
        arcs.add(new ArcImpl(dat2, calcMed));
        arcs.add(new ArcImpl(dat2, calcRules));
        arcs.add(new ArcImpl(con1, extract));
        arcs.add(new ArcImpl(con2, calcMean));
        arcs.add(new ArcImpl(con3, calcMed));
        arcs.add(new ArcImpl(con4, calcRules));
        arcs.add(new ArcImpl(extract, sample));
        arcs.add(new ArcImpl(calcMean, mean));
        arcs.add(new ArcImpl(calcMed, med));
        arcs.add(new ArcImpl(calcRules, rules));
        arcs.add(new ArcImpl(extract, copy));
        arcs.add(new ArcImpl(calcMean, copy));
        arcs.add(new ArcImpl(calcMed, copy));
        arcs.add(new ArcImpl(calcRules, copy));
        arcs.add(new ArcImpl(sample, store1));
        arcs.add(new ArcImpl(mean, store2));
        arcs.add(new ArcImpl(med, store3));
        arcs.add(new ArcImpl(rules, store4));
        arcs.add(new ArcImpl(store1, stor1));
        arcs.add(new ArcImpl(store2, stor2));
        arcs.add(new ArcImpl(store3, stor3));
        arcs.add(new ArcImpl(store4, stor4));
        arcs.add(new ArcImpl(stor1, endTrans));
        arcs.add(new ArcImpl(stor2, endTrans));
        arcs.add(new ArcImpl(stor3, endTrans));
        arcs.add(new ArcImpl(stor4, endTrans));
        arcs.add(new ArcImpl(endTrans, end));
        //create petriNet and visualize
        return new PetriNetImpl(URI.create("https://petrinet"), nodes, arcs);
    }
}
