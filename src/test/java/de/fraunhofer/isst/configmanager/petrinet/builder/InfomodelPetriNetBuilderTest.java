package de.fraunhofer.isst.configmanager.petrinet.builder;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeExpression;
import de.fraunhofer.isst.configmanager.petrinet.model.Place;
import de.fraunhofer.isst.configmanager.petrinet.simulator.PetriNetSimulator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.*;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeAND.nodeAND;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeFORALL_NEXT.nodeFORALL_NEXT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeMODAL.nodeMODAL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeNF.nodeNF;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeOR.nodeOR;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.ArcExpression.arcExpression;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionAF.transitionAF;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionNOT.transitionNOT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.FF.FF;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.TT.TT;

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
        var formula = nodeAND(nodeMODAL(transitionNOT(FF())), nodeOR(nodeNF(NodeExpression.nodeExpression(x -> true, "testMsg")),TT()));
        var formula2 = nodeAND(nodeFORALL_NEXT(nodeMODAL(transitionAF(arcExpression(x -> true,"")))), TT());
        log.info("Formula 1: " + formula.writeFormula());
        log.info("Result: " + CTLEvaluator.evaluate(formula,graph.getInitial().getNodes().stream().filter(node -> node instanceof Place).findAny().get(), allPaths));
        log.info("Formula 2: " + formula2.writeFormula());
        log.info("Result: " + CTLEvaluator.evaluate(formula2,graph.getInitial().getNodes().stream().filter(node -> node instanceof Place).findAny().get(), allPaths));
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
        var formula = nodeAND(nodeMODAL(transitionNOT(FF())), nodeOR(nodeNF(NodeExpression.nodeExpression(x -> true, "testMsg")),TT()));
        log.info(formula.writeFormula());
    }
}
