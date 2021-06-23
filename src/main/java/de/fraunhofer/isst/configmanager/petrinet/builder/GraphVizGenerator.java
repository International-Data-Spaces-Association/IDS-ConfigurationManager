package de.fraunhofer.isst.configmanager.petrinet.builder;

import de.fraunhofer.isst.configmanager.petrinet.model.PetriNet;
import de.fraunhofer.isst.configmanager.petrinet.model.PlaceImpl;
import de.fraunhofer.isst.configmanager.petrinet.model.TransitionImpl;
import de.fraunhofer.isst.configmanager.petrinet.simulator.StepGraph;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Generator for GraphViz representations as DOT Strings.
 * Images can be generated from the given String using GraphViz (https://graphviz.org/)
 * or by pasting the String into GraphViz online (https://dreampuf.github.io/GraphvizOnline)
 */
@UtilityClass
public class GraphVizGenerator {

    /**
     * Generate a GraphViz Dot String representation for the given {@link PetriNet}.
     *
     * @param petriNet The PetriNet for which the Graph representation should be built.
     * @return a DOT String, used for visualizing the PetriNet with GraphViz
     */
    public static String generateGraphVizWithContext(final PetriNet petriNet) {
        final var s = new StringBuilder();
        s.append("digraph graphname {");

        for (final var node : petriNet.getNodes()) {
            if (node instanceof TransitionImpl) {
                //transitions will be drawn as boxes
                s.append(String.format("%d [shape=box, label=\"%s\"];", node.getID().hashCode(), contextInfo((TransitionImpl) node)));
            } else {
                //nodes will be drawn as circles and coloured red, if there have markers
                s.append(String.format("%d[label=\"%s\"", node.getID().hashCode(), node.getID()));

                if (((PlaceImpl) node).getMarkers() > 0) {
                    s.append(", color=red");
                }
                s.append("];");
                s.append(String.format("%d[label=\"%s\"];", node.getID().hashCode(), node.getID()));
            }
        }

        for (final var arc : petriNet.getArcs()) {
            //a directed edge will be drawn for every arc
            s.append(String.format("%d -> %d;", arc.getSource().getID().hashCode(), arc.getTarget().getID().hashCode()));
        }

        s.append("}");
        return s.toString();
    }

    /**
     * Write transitions context to string.
     *
     * @param transition a petrinet transition
     * @return transitions context as string
     */
    private static String contextInfo(final TransitionImpl transition) {
        final var contextObj = transition.getContext();
        final var read = contextObj.getRead().toString();
        final var write = contextObj.getWrite().toString();
        final var erase = contextObj.getErase().toString();
        final var context = contextObj.getContext().toString();
        return String.format("name=%s; write=%s; read=%s; erase=%s; context=%s", transition.getID(), write, read, erase, context);
    }

    /**
     * Generate a GraphViz Dot String representation for the given {@link PetriNet}.
     *
     * @param petriNet The PetriNet for which the Graph representation should be built.
     * @return a DOT String, used for visualizing the PetriNet with GraphViz
     */
    public static String generateGraphViz(final PetriNet petriNet) {
        final var s = new StringBuilder();
        s.append("digraph graphname {");

        for (final var node : petriNet.getNodes()) {
            if (node instanceof TransitionImpl) {
                //transitions will be drawn as boxes
                s.append(String.format("%d [shape=box, label=\"name=%s\"];", node.getID().hashCode(), node.getID()));
            } else {
                //nodes will be drawn as circles and coloured red, if there have markers
                s.append(String.format("%d[label=\"%s\"", node.getID().hashCode(), node.getID()));

                if (((PlaceImpl) node).getMarkers() > 0) {
                    s.append(", color=red");
                }
                s.append("];");
                s.append(String.format("%d[label=\"%s\"];", node.getID().hashCode(), node.getID()));
            }
        }

        for (final var arc : petriNet.getArcs()) {
            //a directed edge will be drawn for every arc
            s.append(String.format("%d -> %d;", arc.getSource().getID().hashCode(), arc.getTarget().getID().hashCode()));
        }

        s.append("}");
        return s.toString();
    }

    /**
     * Generate a GraphViz Dot String representation for the given {@link StepGraph}
     * The StepGraph for which the Graph representation should be built.
     * @param stepGraph the graph used
     * @return a DOT String, used for visualizing the StepGraph with GraphViz.
     */
    public static String generateGraphViz(final StepGraph stepGraph) {
        final var someId = String.valueOf(stepGraph.getSteps().stream().findAny().get().getNodes().stream().findAny().get().getID().hashCode());
        final var graphArcs = new ArrayList<GraphvizArc>();
        final var s = new StringBuilder();
        final var idMap = new HashMap<PetriNet, Integer>();

        var i = 0;

        s.append("digraph graphname {");
        //the StepGraph will be a compound graph from all PetriNet subgraphs it contains
        s.append("compound=true;");


        for (final var petriNet : stepGraph.getSteps()) {
            //generate the graph for every PetriNet in the StepGraph
            var petriString = generateGraphViz(petriNet);
            //the PetriNet Graphs will be subgraphs and have different names
            petriString = petriString.replace("digraph", "subgraph");
            petriString = petriString.replace("graphname", "cluster" + i);

            for (final var node : petriNet.getNodes()) {
                //nodes must have unique names too (or DOT will draw them as the same node)
                petriString = petriString.replace(String.valueOf(node.getID().hashCode()), node.getID().hashCode() + String.valueOf(i));
            }

            s.append(petriString);
            idMap.put(petriNet, i);
            i++;
        }

        for (final var arc : stepGraph.getArcs()) {
            //build a GraphvizArc for each NetArc in the StepGraph
            graphArcs.add(new GraphvizArc(idMap.get(arc.getSource()), idMap.get(arc.getTarget())));
        }

        for (final var arc : graphArcs) {
            //draw the GraphVizArcs as directed edges between the PetriNet Subgraphs
            s.append(String.format("%s%d -> %s%d[ltail=cluster%d,lhead=cluster%d];", someId, arc.getSource(), someId, arc.getTarget(), arc.getSource(), arc.getTarget()));
        }

        s.append("}");
        return s.toString();
    }

    /**
     * Utility Subclass, representing Petri Net Arcs.
     */
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class GraphvizArc {
        @Getter
        @Setter
        int source;

        @Getter
        @Setter
        int target;
    }
}
