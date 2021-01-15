package de.fraunhofer.isst.configmanager.petrinet.builder;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.petrinet.simulator.PetriNetSimulator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Test building a PetriNet from a randomly generated AppRoute
 */
class InfomodelPetriNetBuilderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfomodelPetriNetBuilderTest.class);
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
        LOGGER.info(ser.serialize(appRoute));
        LOGGER.info(GraphVizGenerator.generateGraphViz(petriNet));

        //build a full Graph of all possible steps in the PetriNet and log generated GraphViz representation
        var graph = PetriNetSimulator.buildStepGraph(petriNet);
        LOGGER.info(String.valueOf(graph.getArcs().size()));
        LOGGER.info(GraphVizGenerator.generateGraphViz(graph));
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
}