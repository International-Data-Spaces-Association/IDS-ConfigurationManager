package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.simulator;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.PetriNet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

/**
 * Graph containing every Step a Petri Net can make in its execution.
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StepGraph {
    PetriNet initial;

    /**
     * Each Step a PetriNet can make is represented as a PetriNet.
     */
    Set<PetriNet> steps;

    /**
     * Arc which Steps are reachable from given Steps.
     */
    Set<NetArc> arcs;

    public StepGraph(final PetriNet initial) {
        this.initial = initial;
        steps = new HashSet<>();
        arcs = new HashSet<>();
    }
}
