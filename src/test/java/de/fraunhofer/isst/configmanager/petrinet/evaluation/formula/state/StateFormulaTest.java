package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Arc;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.ArcImpl;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.PetriNet;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.PetriNetImpl;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.PlaceImpl;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.TransitionImpl;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.simulator.PetriNetSimulator;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.FF.FF;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.TT.TT;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.NodeAND.nodeAND;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.NodeEXIST_MODAL.nodeEXIST_MODAL;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.NodeEXIST_NEXT.nodeEXIST_NEXT;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.NodeEXIST_UNTIL.nodeEXIST_UNTIL;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.NodeExpression.nodeExpression;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.NodeFORALL_MODAL.nodeFORALL_MODAL;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.NodeFORALL_NEXT.nodeFORALL_NEXT;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.NodeFORALL_UNTIL.nodeFORALL_UNTIL;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.NodeMODAL.nodeMODAL;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.NodeNF.nodeNF;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.NodeNOT.nodeNOT;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state.NodeOR.nodeOR;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.transition.ArcExpression.arcExpression;
import static de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.transition.TransitionAF.transitionAF;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StateFormulaTest {

    @Test
    void testBasicFormulas() {
        //test AND
        final var formAND1 = nodeAND(TT(), TT());
        final var formAND2 = nodeAND(TT(), FF());
        final var formAND3 = nodeAND(FF(), FF());
        assertTrue(formAND1.evaluate(null, List.of()));
        assertFalse(formAND2.evaluate(null, List.of()));
        assertFalse(formAND3.evaluate(null, List.of()));

        //test OR
        final var formOR1 = nodeOR(TT(), TT());
        final var formOR2 = nodeOR(TT(), FF());
        final var formOR3 = nodeOR(FF(), FF());
        assertTrue(formOR1.evaluate(null, List.of()));
        assertTrue(formOR2.evaluate(null, List.of()));
        assertFalse(formOR3.evaluate(null, List.of()));

        //test NOT
        final var formNOT1 = nodeNOT(TT());
        final var formNOT2 = nodeNOT(FF());
        assertFalse(formNOT1.evaluate(null, List.of()));
        assertTrue(formNOT2.evaluate(null, List.of()));
    }

    @Test
    void testExpression() {
        final var formula = nodeNF(nodeExpression(x -> true, ""));
        //should not accept null
        assertFalse(formula.evaluate(null, List.of()));
        //should evaluate to true
        assertTrue(formula.evaluate(new PlaceImpl(URI.create("https://test")), List.of()));
    }

    @Test
    void testEXISTS() {
        final var petriNet = createNet();
        final var stepGraph = PetriNetSimulator.buildStepGraph(petriNet);
        final var paths = PetriNetSimulator.getAllPaths(stepGraph);
        final var startNode = petriNet.getNodes().stream().filter(node -> "place://start".equals(node.getID().toString())).findAny().get();

        final var formulaExistsUntil = nodeEXIST_UNTIL(TT(), nodeNF(nodeExpression(x -> "place://end".equals(x.getID().toString()), "")));
        final var formulaExistsNext = nodeEXIST_NEXT(formulaExistsUntil);
        final var formulaExistsModal = nodeEXIST_MODAL(formulaExistsUntil, TT());

        assertTrue(formulaExistsUntil.evaluate(startNode, paths));
        assertTrue(formulaExistsNext.evaluate(startNode, paths));
        assertTrue(formulaExistsModal.evaluate(startNode, paths));
    }

    @Test
    void testFORALL() {
        final var petriNet = createNet();
        final var stepGraph = PetriNetSimulator.buildStepGraph(petriNet);
        final var paths = PetriNetSimulator.getAllPaths(stepGraph);
        final var startNode = petriNet.getNodes().stream().filter(node -> "place://start".equals(node.getID().toString())).findAny().get();

        final var formulaForallUntil = nodeFORALL_UNTIL(TT(), nodeNF(nodeExpression(x -> "place://end".equals(x.getID().toString()), "")));
        final var formulaForallNext = nodeFORALL_NEXT(formulaForallUntil);
        final var formulaForallModal = nodeFORALL_MODAL(formulaForallUntil, TT());

        assertFalse(formulaForallUntil.evaluate(startNode, paths));
        assertFalse(formulaForallNext.evaluate(startNode, paths));
        assertFalse(formulaForallModal.evaluate(startNode, paths));
    }

    @Test
    void testMODAL() {
        final var node = new PlaceImpl(URI.create("place://start"));
        final var trans = new TransitionImpl(URI.create("trans://modal"));
        final var arc = new ArcImpl(node, trans);
        final var formulaModal1 = nodeMODAL(transitionAF(arcExpression(x -> true, "")));
        final var formulaModal2 = nodeMODAL(transitionAF(arcExpression(x -> false, "")));
        assertTrue(formulaModal1.evaluate(node, List.of()));
        assertFalse(formulaModal2.evaluate(node, List.of()));
    }

    private PetriNet createNet(){
        //create nodes
        final var nodeStart = new PlaceImpl(URI.create("place://start"));
        nodeStart.setMarkers(1);
        final var nodeMiddle = new PlaceImpl(URI.create("place://mid"));
        final var nodeEnd = new PlaceImpl(URI.create("place://end"));
        final var nodeLoop1 = new PlaceImpl(URI.create("place://loop1"));
        final var nodeLoop2 = new PlaceImpl(URI.create("place://loop2"));
        final var transStartMid = new TransitionImpl(URI.create("trans://startMid"));
        final var transMidEnd = new TransitionImpl(URI.create("trans://midEnd"));
        final var transStartLoop = new TransitionImpl(URI.create("trans://startLoop1"));
        final var transLoop1 = new TransitionImpl(URI.create("trans://loop1"));
        final var transLoop2 = new TransitionImpl(URI.create("trans://loop2"));

        //create nodeset
        final Set<Node> nodeSet = Set.of(
                nodeStart,
                nodeMiddle,
                nodeEnd,
                nodeLoop1,
                nodeLoop2,
                transStartMid,
                transMidEnd,
                transStartLoop,
                transLoop1,
                transLoop2
        );

        //create transitions
        final var arcStartMid1 = new ArcImpl(nodeStart, transStartMid);
        final var arcStartMid2 = new ArcImpl(transStartMid, nodeMiddle);
        final var arcMidEnd1 = new ArcImpl(nodeMiddle, transMidEnd);
        final var arcMidEnd2 = new ArcImpl(transMidEnd, nodeEnd);
        final var arcStartLoop1 = new ArcImpl(nodeStart, transStartLoop);
        final var arcStartLoop2 = new ArcImpl(transStartLoop, nodeLoop1);
        final var arcLoop1 = new ArcImpl(nodeLoop1, transLoop1);
        final var arcLoop2 = new ArcImpl(transLoop1, nodeLoop2);
        final var arcLoop3 = new ArcImpl(nodeLoop2, transLoop2);
        final var arcLoop4 = new ArcImpl(transLoop2, nodeLoop1);

        //create arcset
        final Set<Arc> arcSet = Set.of(
                arcStartMid1,
                arcStartMid2,
                arcMidEnd1,
                arcMidEnd2,
                arcStartLoop1,
                arcStartLoop2,
                arcLoop1,
                arcLoop2,
                arcLoop3,
                arcLoop4
        );

        return new PetriNetImpl(URI.create("net://petrinet"), nodeSet, arcSet);
    }
}
