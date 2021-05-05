package de.fraunhofer.isst.configmanager.petrinet.builder;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeExpression;
import de.fraunhofer.isst.configmanager.petrinet.model.*;
import de.fraunhofer.isst.configmanager.petrinet.simulator.PetriNetSimulator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.*;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeAND.nodeAND;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeEXIST_UNTIL.nodeEXIST_UNTIL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeExpression.nodeExpression;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeFORALL_MODAL.nodeFORALL_MODAL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeFORALL_NEXT.nodeFORALL_NEXT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeFORALL_UNTIL.nodeFORALL_UNTIL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeMODAL.nodeMODAL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeNF.nodeNF;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeNOT.nodeNOT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeOR.nodeOR;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodePOS.nodePOS;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.ArcExpression.arcExpression;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionAF.transitionAF;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionAND.transitionAND;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionEXIST_UNTIL.transitionEXIST_UNTIL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFORALL_UNTIL.transitionFORALL_UNTIL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionMODAL.transitionMODAL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionNOT.transitionNOT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.FF.FF;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.TT.TT;
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
     * Generate a random PetriNet, try to simulate it and print out the GraphViz representation
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
            subroutes.add(new RouteStepBuilder(URI.create("http://subroute" + i))._appRouteStart_(randomSubList(endpointlist))._appRouteEnd_(randomSubList(endpointlist)).build());
        }
        var appRoute = new AppRouteBuilder(URI.create("http://approute"))
                ._appRouteStart_(randomSubList(endpointlist))
                ._appRouteEnd_(randomSubList(endpointlist))
                ._hasSubRoute_(subroutes)
                .build();

        //build a petriNet from the generated AppRoute and log generated GraphViz representation
        var petriNet = InfomodelPetriNetBuilder.petriNetFromAppRoute(appRoute, false);
        var ser = new Serializer();
        log.info(ser.serialize(appRoute));
        log.info(GraphVizGenerator.generateGraphViz(petriNet));

        //build a full Graph of all possible steps in the PetriNet and log generated GraphViz representation
        var graph = PetriNetSimulator.buildStepGraph(petriNet);
        log.info(String.valueOf(graph.getArcs().size()));
        log.info(GraphVizGenerator.generateGraphViz(graph));
        var allPaths = PetriNetSimulator.getAllPaths(graph);
        log.info(allPaths.toString());
        var formula = nodeAND(nodeMODAL(transitionNOT(FF())), nodeOR(nodeNF(nodeExpression(x -> true, "testMsg")),TT()));
        var formula2 = nodeAND(nodeFORALL_NEXT(nodeMODAL(transitionAF(arcExpression(x -> true,"")))), TT());
        var formula3 = nodeEXIST_UNTIL(nodeMODAL(TT()), nodeNF(nodeExpression(x -> x.getSourceArcs().isEmpty(), "")));
        log.info("Formula 1: " + formula.writeFormula());
        log.info("Result: " + CTLEvaluator.evaluate(formula,graph.getInitial().getNodes().stream().filter(node -> node instanceof Place).findAny().get(), allPaths));
        log.info("Formula 2: " + formula2.writeFormula());
        log.info("Result: " + CTLEvaluator.evaluate(formula2,graph.getInitial().getNodes().stream().filter(node -> node instanceof Place).findAny().get(), allPaths));
        log.info("Formula 3: " + formula3.writeFormula());
        log.info("Result: " + CTLEvaluator.evaluate(formula3,graph.getInitial().getNodes().stream().filter(node -> node.getID().equals(URI.create("place://source"))).findAny().get(), allPaths));
    }

    @Test
    @Disabled
    void testExamplePetriNet(){
        var nodes = new HashSet<Node>();
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
        nodes.addAll(List.of(start, copy, init, dat1, dat2, con1, con2, con3, con4, sample, mean, med, rules, stor1, stor2, stor3, stor4, end));
        //create transitions with context
        var initTrans = new TransitionImpl(URI.create("trans://init"));
        initTrans.setContextObject(new ContextObject(List.of(), null, null, null));
        var getData = new TransitionImpl(URI.create("trans://getData"));
        getData.setContextObject(new ContextObject(List.of(), null, "data", null));
        var copyData = new TransitionImpl(URI.create("trans://copyData"));
        copyData.setContextObject(new ContextObject(List.of("france"), "data", "data", null));
        var extract = new TransitionImpl(URI.create("trans://extractSample"));
        extract.setContextObject(new ContextObject(List.of("france"), "data", "sample", "data"));
        var calcMean = new TransitionImpl(URI.create("trans://calcMean"));
        calcMean.setContextObject(new ContextObject(List.of("france"), "data", "mean", "data"));
        var calcMed = new TransitionImpl(URI.create("trans://calcMedian"));
        calcMed.setContextObject(new ContextObject(List.of("france"), "data", "median", "data"));
        var calcRules = new TransitionImpl(URI.create("trans://calcAPrioriRules"));
        calcRules.setContextObject(new ContextObject(List.of("france", "high_performance"), "data", "rules", "data"));
        var store1 = new TransitionImpl(URI.create("trans://storeData1"));
        store1.setContextObject(new ContextObject(List.of(), "sample", null, "sample"));
        var store2 = new TransitionImpl(URI.create("trans://storeData2"));
        store2.setContextObject(new ContextObject(List.of(), "mean", null, "mean"));
        var store3 = new TransitionImpl(URI.create("trans://storeData3"));
        store3.setContextObject(new ContextObject(List.of(), "median", null, "median"));
        var store4 = new TransitionImpl(URI.create("trans://storeData4"));
        store4.setContextObject(new ContextObject(List.of(), "rules", null, "rules"));
        var endTrans = new TransitionImpl(URI.create("trans://end"));
        endTrans.setContextObject(new ContextObject(List.of(), null, null, null));
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
        var petriNet = new PetriNetImpl(URI.create("https://petrinet"), nodes, arcs);
        log.info(GraphVizGenerator.generateGraphViz(petriNet));
        //build stepGraph and visualize
        var graph = PetriNetSimulator.buildStepGraph(petriNet);
        //log.info(GraphVizGenerator.generateGraphViz(graph));
        log.info(String.format("%d possible states!", graph.getSteps().size()));
        //find valid paths and visualize
        var transitionList = PetriNetSimulator.getAllPaths(graph);
        log.info("Transitions: " + transitionList.size());
        /*
        var allPaths = PetriNetSimulator.getAllPaths(graph);
        log.info(String.format("Found %d valid Paths!", allPaths.size()));
        //create formula and evaluate on start node (exists path from start to end)
        var formula = nodePOS(nodeNF(nodeExpression(x -> x.getSourceArcs().isEmpty(), "")));
        log.info("Evaluating Formula: " + formula.writeFormula());
        log.info("Result: " + CTLEvaluator.evaluate(formula, graph.getInitial().getNodes().stream().filter(node -> node.getID().equals(URI.create("place://start"))).findAny().get(), allPaths));
        var formulaFrance = nodeMODAL(
                transitionNOT(
                        transitionFORALL_UNTIL(
                                transitionAND(
                                        transitionAF(arcExpression(transition ->
                                        {if(transition.getContext().getRead() != null && transition.getContext().getRead().equals("data")) { return transition.getContext().getContext().contains("france"); } return true;}, "")
                                        ),
                                        transitionPOS(
                                                transitionOR(
                                                        transitionMODAL(nodeNF(nodeExpression(node -> node.getID().equals(URI.create("place://end")), ""))),
                                                        transitionAF(arcExpression(transition -> transition.getContext().getErase() != null && transition.getContext().getErase().equals("data"), "")))
                                        )),
                                transitionAND(
                                        transitionAF(arcExpression(transition ->
                                                {if(transition.getContext().getRead() != null && transition.getContext().getRead().equals("data")) {
                                                    return transition.getContext().getContext().contains("france");
                                                } return true;}, "")),
                                        transitionPOS(
                                                transitionOR(
                                                        transitionMODAL(nodeNF(nodeExpression(node -> node.getID().equals(URI.create("place://end")), ""))),
                                                        transitionAF(arcExpression(transition -> transition.getContext().getErase() != null && transition.getContext().getErase().equals("data"), "")))
                                                )
                                        )
                        )
                )
        );
        log.info("Formula France: " + formulaFrance.writeFormula());
        log.info("Result: " + CTLEvaluator.evaluate(formulaFrance, graph.getInitial().getNodes().stream().filter(node -> node.getID().equals(URI.create("place://start"))).findAny().get(), allPaths));
         */
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

    @Test
    @Disabled
    public void testFormula(){
        var formula = nodeAND(nodeMODAL(transitionNOT(FF())), nodeOR(nodeNF(nodeExpression(x -> true, "testMsg")),TT()));
        log.info(formula.writeFormula());
    }
}
