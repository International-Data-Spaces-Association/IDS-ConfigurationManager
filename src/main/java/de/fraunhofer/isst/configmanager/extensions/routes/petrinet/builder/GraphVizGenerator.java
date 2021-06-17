package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.builder;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.PetriNet;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.PlaceImpl;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.TransitionImpl;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.simulator.StepGraph;
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
    public static String generateGraphViz(final PetriNet petriNet) {
        final var s = new StringBuilder();
        s.append("digraph graphname {");

        for (final var node : petriNet.getNodes()) {
            if (node instanceof TransitionImpl) {
                //transitions will be drawn as boxes
                s.append(node.getID().hashCode() + " [shape=box, label=\"" + node.getID() + "\"];");
            } else {
                //nodes will be drawn as circles and coloured red, if there have markers
                s.append(node.getID().hashCode() + "[label=\"" + node.getID() + "\"");

                if (((PlaceImpl) node).getMarkers() > 0) {
                    s.append(", color=red");
                }
                s.append("];");
                s.append(node.getID().hashCode() + "[label=\"" + node.getID() + "\"];");
            }
        }

        for (final var arc : petriNet.getArcs()) {
            //a directed edge will be drawn for every arc
            s.append(arc.getSource().getID().hashCode() + " -> " + arc.getTarget().getID().hashCode() + ";");
        }

        s.append("}");
        return s.toString();
    }

    /**
     * Generate a GraphViz Dot String representation for the given {@link StepGraph}
     * The StepGraph for which the Graph representation should be built.
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
            s.append(someId + arc.getSource() + " -> " + someId + arc.getTarget() + "[ltail=cluster" + arc.getSource() + ",lhead=cluster" + arc.getTarget() + "];");
        }

        s.append("}");
        return s.toString();
    }

    /**
     * Utility Subclass, representing Petri Net Arcs.
     */
    public static class GraphvizArc {
        private int source;
        private int target;

        public GraphvizArc(final int source, final int target) {
            this.source = source;
            this.target = target;
        }

        public int getSource() {
            return source;
        }

        public int getTarget() {
            return target;
        }

        public void setSource(final int source) {
            this.source = source;
        }

        public void setTarget(final int target) {
            this.target = target;
        }
    }
}
