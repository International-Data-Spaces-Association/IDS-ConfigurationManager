package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.model.*;
import de.fraunhofer.isst.configmanager.petrinet.simulator.PetriNetSimulator;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.FF.FF;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.TT.TT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeAND.nodeAND;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeEXIST_MODAL.nodeEXIST_MODAL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeEXIST_NEXT.nodeEXIST_NEXT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeEXIST_UNTIL.nodeEXIST_UNTIL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeExpression.nodeExpression;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeFORALL_MODAL.nodeFORALL_MODAL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeFORALL_NEXT.nodeFORALL_NEXT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeFORALL_UNTIL.nodeFORALL_UNTIL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeMODAL.nodeMODAL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeNF.nodeNF;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeNOT.nodeNOT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeOR.nodeOR;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.ArcExpression.arcExpression;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionAF.transitionAF;
import static org.junit.jupiter.api.Assertions.*;

class StateFormulaTest {
    
    @Test
    void testBasicFormulas() {
        //test AND
        var formAND1 = nodeAND(TT(), TT());
        var formAND2 = nodeAND(TT(), FF());
        var formAND3 = nodeAND(FF(), FF());
        assertTrue(formAND1.evaluate(null, List.of()));
        assertFalse(formAND2.evaluate(null, List.of()));
        assertFalse(formAND3.evaluate(null, List.of()));

        //test OR
        var formOR1 = nodeOR(TT(), TT());
        var formOR2 = nodeOR(TT(), FF());
        var formOR3 = nodeOR(FF(), FF());
        assertTrue(formOR1.evaluate(null, List.of()));
        assertTrue(formOR2.evaluate(null, List.of()));
        assertFalse(formOR3.evaluate(null, List.of()));

        //test NOT
        var formNOT1 = nodeNOT(TT());
        var formNOT2 = nodeNOT(FF());
        assertFalse(formNOT1.evaluate(null, List.of()));
        assertTrue(formNOT2.evaluate(null, List.of()));
    }

    @Test
    void testExpression() {
        var formula = nodeNF(nodeExpression(x -> true, ""));
        //should not accept null
        assertFalse(formula.evaluate(null, List.of()));
        //should evaluate to true
        assertTrue(formula.evaluate(new PlaceImpl(URI.create("https://test")), List.of()));
    }

    @Test
    void testEXISTS() {
        var petriNet = createNet();
        var stepGraph = PetriNetSimulator.buildStepGraph(petriNet);
        var paths = PetriNetSimulator.getAllPaths(stepGraph);
        var startNode = petriNet.getNodes().stream().filter(node -> node.getID().toString().equals("place://start")).findAny().get();

        var formulaExistsUntil = nodeEXIST_UNTIL(TT(), nodeNF(nodeExpression(x -> x.getID().toString().equals("place://end"), "")));
        var formulaExistsNext = nodeEXIST_NEXT(formulaExistsUntil);
        var formulaExistsModal = nodeEXIST_MODAL(formulaExistsUntil, TT());

        assertTrue(formulaExistsUntil.evaluate(startNode, paths));
        assertTrue(formulaExistsNext.evaluate(startNode, paths));
        assertTrue(formulaExistsModal.evaluate(startNode, paths));
    }

    @Test
    void testFORALL() {
        var petriNet = createNet();
        var stepGraph = PetriNetSimulator.buildStepGraph(petriNet);
        var paths = PetriNetSimulator.getAllPaths(stepGraph);
        var startNode = petriNet.getNodes().stream().filter(node -> node.getID().toString().equals("place://start")).findAny().get();

        var formulaForallUntil = nodeFORALL_UNTIL(TT(), nodeNF(nodeExpression(x -> x.getID().toString().equals("place://end"), "")));
        var formulaForallNext = nodeFORALL_NEXT(formulaForallUntil);
        var formulaForallModal = nodeFORALL_MODAL(formulaForallUntil, TT());

        assertFalse(formulaForallUntil.evaluate(startNode, paths));
        assertFalse(formulaForallNext.evaluate(startNode, paths));
        assertFalse(formulaForallModal.evaluate(startNode, paths));
    }

    @Test
    void testMODAL() {
        var node = new PlaceImpl(URI.create("place://start"));
        var trans = new TransitionImpl(URI.create("trans://modal"));
        var arc = new ArcImpl(node, trans);
        var formulaModal1 = nodeMODAL(transitionAF(arcExpression(x -> true, "")));
        var formulaModal2 = nodeMODAL(transitionAF(arcExpression(x -> false, "")));
        assertTrue(formulaModal1.evaluate(node, List.of()));
        assertFalse(formulaModal2.evaluate(node, List.of()));
    }

    private PetriNet createNet(){
        //create nodes
        var nodeStart = new PlaceImpl(URI.create("place://start"));
        nodeStart.setMarkers(1);
        var nodeMiddle = new PlaceImpl(URI.create("place://mid"));
        var nodeEnd = new PlaceImpl(URI.create("place://end"));
        var nodeLoop1 = new PlaceImpl(URI.create("place://loop1"));
        var nodeLoop2 = new PlaceImpl(URI.create("place://loop2"));
        var transStartMid = new TransitionImpl(URI.create("trans://startMid"));
        var transMidEnd = new TransitionImpl(URI.create("trans://midEnd"));
        var transStartLoop = new TransitionImpl(URI.create("trans://startLoop1"));
        var transLoop1 = new TransitionImpl(URI.create("trans://loop1"));
        var transLoop2 = new TransitionImpl(URI.create("trans://loop2"));

        //create nodeset
        Set<Node> nodeSet = Set.of(
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
        var arcStartMid1 = new ArcImpl(nodeStart, transStartMid);
        var arcStartMid2 = new ArcImpl(transStartMid, nodeMiddle);
        var arcMidEnd1 = new ArcImpl(nodeMiddle, transMidEnd);
        var arcMidEnd2 = new ArcImpl(transMidEnd, nodeEnd);
        var arcStartLoop1 = new ArcImpl(nodeStart, transStartLoop);
        var arcStartLoop2 = new ArcImpl(transStartLoop, nodeLoop1);
        var arcLoop1 = new ArcImpl(nodeLoop1, transLoop1);
        var arcLoop2 = new ArcImpl(transLoop1, nodeLoop2);
        var arcLoop3 = new ArcImpl(nodeLoop2, transLoop2);
        var arcLoop4 = new ArcImpl(transLoop2, nodeLoop1);

        //create arcset
        Set<Arc> arcSet = Set.of(
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