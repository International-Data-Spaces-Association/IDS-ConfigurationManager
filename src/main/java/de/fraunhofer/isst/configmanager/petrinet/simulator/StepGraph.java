package de.fraunhofer.isst.configmanager.petrinet.simulator;

import de.fraunhofer.isst.configmanager.petrinet.model.PetriNet;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * Graph containing every Step a Petri Net can make in its execution
 */
@Getter
public class StepGraph {

    public StepGraph(PetriNet initial){
        this.initial = initial;
        steps = new HashSet<>();
        arcs = new HashSet<>();
    }

    private PetriNet initial;

    /**
     * Each Step a PetriNet can make is represented as a PetriNet
     */
    private Set<PetriNet> steps;

    /**
     * Arc which Steps are reachable from given Steps
     */
    private Set<NetArc> arcs;
}
