package de.fraunhofer.isst.configmanager.petrinet.builder;

import de.fraunhofer.isst.configmanager.petrinet.model.PetriNet;
import de.fraunhofer.isst.configmanager.petrinet.model.PlaceImpl;
import de.fraunhofer.isst.configmanager.petrinet.model.TransitionImpl;
import de.fraunhofer.isst.configmanager.petrinet.simulator.StepGraph;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Generator for GraphViz representations as DOT Strings.
 * Images can be generated from the given String using GraphViz (https://graphviz.org/)
 * or by pasting the String into GraphViz online (https://dreampuf.github.io/GraphvizOnline)
 */
public class GraphVizGenerator {
    
    /**
     * Generate a GraphViz Dot String representation for the given {@link PetriNet}
     *
     * @param petriNet The PetriNet for which the Graph representation should be built.
     * @return a DOT String, used for visualizing the PetriNet with GraphViz
     */
    public static String generateGraphViz(PetriNet petriNet){
        StringBuilder s = new StringBuilder();
        s.append("digraph graphname {");
        for(var node : petriNet.getNodes()){
            if(node instanceof TransitionImpl){
                //transitions will be drawn as boxes
                s.append(node.getID().hashCode() + " [shape=box, label=\"" + node.getID() + "\"];");
            }else {
                //nodes will be drawn as circles and coloured red, if there have markers
                s.append(node.getID().hashCode() + "[label=\"" + node.getID() + "\"");
                if(((PlaceImpl) node).getMarkers() > 0){
                    s.append(", color=red");
                }
                s.append("];");
                s.append(node.getID().hashCode() + "[label=\"" + node.getID() + "\"];");
            }
        }
        for(var arc : petriNet.getArcs()){
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
    public static String generateGraphViz(StepGraph stepGraph){
        var someId = String.valueOf(stepGraph.getSteps().stream().findAny().get().getNodes().stream().findAny().get().getID().hashCode());
        var graphArcs = new ArrayList<GraphvizArc>();
        StringBuilder s = new StringBuilder();
        s.append("digraph graphname {");
        //the StepGraph will be a compound graph from all PetriNet subgraphs it contains
        s.append("compound=true;");
        var i = 0;
        var idMap = new HashMap<PetriNet, Integer>();
        for(var petriNet : stepGraph.getSteps()){
            //generate the graph for every PetriNet in the StepGraph
            var petriString = generateGraphViz(petriNet);
            //the PetriNet Graphs will be subgraphs and have different names
            petriString = petriString.replace("digraph", "subgraph");
            petriString = petriString.replace("graphname", "cluster"+i);
            for(var node : petriNet.getNodes()){
                //nodes must have unique names too (or DOT will draw them as the same node)
                petriString = petriString.replace(String.valueOf(node.getID().hashCode()), node.getID().hashCode()+String.valueOf(i));
            }
            s.append(petriString);
            idMap.put(petriNet, i);
            i++;
        }
        for(var arc : stepGraph.getArcs()){
            //build a GraphvizArc for each NetArc in the StepGraph
            graphArcs.add(new GraphvizArc(idMap.get(arc.getSource()), idMap.get(arc.getTarget())));
        }
        for(var arc : graphArcs){
            //draw the GraphVizArcs as directed edges between the PetriNet Subgraphs
            s.append(someId + arc.getSource() + " -> " + someId + arc.getTarget() + "[ltail=cluster"+arc.getSource()+",lhead=cluster"+arc.getTarget()+"];");
        }
        s.append("}");
        return s.toString();
    }
    
    /**
     * Utility Subclass, representing Petri Net Arcs
     */
    public static class GraphvizArc{
        private int source, target;

        public GraphvizArc(int source, int target){
            this.source = source;
            this.target = target;
        }

        public int getSource(){
            return source;
        }

        public int getTarget(){
            return target;
        }

        public void setSource(int source){
            this.source = source;
        }

        public void setTarget(int target){
            this.target = target;
        }
    }
}
