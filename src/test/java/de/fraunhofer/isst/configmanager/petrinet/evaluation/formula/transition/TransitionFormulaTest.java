package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Arc;
import de.fraunhofer.isst.configmanager.petrinet.model.ArcImpl;
import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.PetriNet;
import de.fraunhofer.isst.configmanager.petrinet.model.PetriNetImpl;
import de.fraunhofer.isst.configmanager.petrinet.model.PlaceImpl;
import de.fraunhofer.isst.configmanager.petrinet.model.TransitionImpl;
import de.fraunhofer.isst.configmanager.petrinet.simulator.PetriNetSimulator;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.FF.FF;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.TT.TT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeExpression.nodeExpression;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeNF.nodeNF;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.ArcExpression.arcExpression;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionAF.transitionAF;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionAND.transitionAND;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionEXIST_MODAL.transitionEXIST_MODAL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionEXIST_NEXT.transitionEXIST_NEXT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionEXIST_UNTIL.transitionEXIST_UNTIL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFORALL_MODAL.transitionFORALL_MODAL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFORALL_NEXT.transitionFORALL_NEXT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFORALL_UNTIL.transitionFORALL_UNTIL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionMODAL.transitionMODAL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionNOT.transitionNOT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionOR.transitionOR;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransitionFormulaTest {

    @Test
    void testBasicFormulas() {
        //test AND
        final var formAND1 = transitionAND(TT(), TT());
        final var formAND2 = transitionAND(TT(), FF());
        final var formAND3 = transitionAND(FF(), FF());
        assertTrue(formAND1.evaluate(null, List.of()));
        assertFalse(formAND2.evaluate(null, List.of()));
        assertFalse(formAND3.evaluate(null, List.of()));

        //test OR
        final var formOR1 = transitionOR(TT(), TT());
        final var formOR2 = transitionOR(TT(), FF());
        final var formOR3 = transitionOR(FF(), FF());
        assertTrue(formOR1.evaluate(null, List.of()));
        assertTrue(formOR2.evaluate(null, List.of()));
        assertFalse(formOR3.evaluate(null, List.of()));

        //test NOT
        final var formNOT1 = transitionNOT(TT());
        final var formNOT2 = transitionNOT(FF());
        assertFalse(formNOT1.evaluate(null, List.of()));
        assertTrue(formNOT2.evaluate(null, List.of()));
    }

    @Test
    void testExpression() {
        final var formula = transitionAF(arcExpression(x -> true, ""));
        //should not accept null
        assertFalse(formula.evaluate(null, List.of()));
        //should evaluate to true
        assertTrue(formula.evaluate(new TransitionImpl(URI.create("https://test")), List.of()));
    }

    @Test
    void testEXISTS() {
        final var petriNet = createNet();
        final var stepGraph = PetriNetSimulator.buildStepGraph(petriNet);
        final var paths = PetriNetSimulator.getAllPaths(stepGraph);
        final var startTrans = petriNet.getNodes().stream().filter(node -> "trans://start".equals(node.getID().toString())).findAny().get();

        final var formulaExistsUntil = transitionEXIST_UNTIL(TT(), transitionAF(arcExpression(x -> "trans://midEnd".equals(x.getID().toString()), "")));
        final var formulaExistsNext = transitionEXIST_NEXT(formulaExistsUntil);
        final var formulaExistsModal = transitionEXIST_MODAL(formulaExistsUntil, TT());

        assertTrue(formulaExistsUntil.evaluate(startTrans, paths));
        assertTrue(formulaExistsNext.evaluate(startTrans, paths));
        assertTrue(formulaExistsModal.evaluate(startTrans, paths));
    }

    @Test
    void testFORALL() {
        final var petriNet = createNet();
        final var stepGraph = PetriNetSimulator.buildStepGraph(petriNet);
        final var paths = PetriNetSimulator.getAllPaths(stepGraph);
        final var startTrans = petriNet.getNodes().stream().filter(node -> "trans://start".equals(node.getID().toString())).findAny().get();

        final var formulaForallUntil = transitionFORALL_UNTIL(TT(), transitionAF(arcExpression(x -> "trans://midEnd".equals(x.getID().toString()), "")));
        final var formulaForallNext = transitionFORALL_NEXT(formulaForallUntil);
        final var formulaForallModal = transitionFORALL_MODAL(formulaForallUntil, TT());

        assertFalse(formulaForallUntil.evaluate(startTrans, paths));
        assertFalse(formulaForallNext.evaluate(startTrans, paths));
        assertFalse(formulaForallModal.evaluate(startTrans, paths));
    }

    @Test
    void testMODAL() {
        final var node = new PlaceImpl(URI.create("place://start"));
        final var trans = new TransitionImpl(URI.create("trans://modal"));
        final var arc = new ArcImpl(trans, node);
        final var formulaModal1 = transitionMODAL(nodeNF(nodeExpression(x -> true, "")));
        final var formulaModal2 = transitionMODAL(nodeNF(nodeExpression(x -> false, "")));
        assertTrue(formulaModal1.evaluate(trans, List.of()));
        assertFalse(formulaModal2.evaluate(trans, List.of()));
    }

    private PetriNet createNet(){
        //create nodes
        final var nodePre = new PlaceImpl(URI.create("place://pre"));
        nodePre.setMarkers(1);
        final var transStart = new TransitionImpl(URI.create("trans://start"));
        final var nodeStart = new PlaceImpl(URI.create("place://start"));
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
                nodePre,
                transStart,
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
        final var arcPreStart = new ArcImpl(nodePre, transStart);
        final var arcStart = new ArcImpl(transStart, nodeStart);
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
                arcPreStart,
                arcStart,
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
