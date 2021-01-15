package de.fraunhofer.isst.configmanager.petrinet.simulator;

import de.fraunhofer.isst.configmanager.petrinet.model.PetriNet;
import lombok.Getter;

/**
 * Arc connecting Steps in a PetriNet execution inside the {@link StepGraph}
 */
@Getter
public class NetArc {

    public NetArc(PetriNet source, PetriNet target){
        this.source = source;
        this.target = target;
    }

    /**
     * PetriNet from which target is reachable, using a transition.
     */
    private PetriNet source;


    /**
     * PetriNet that can be reached from source, using a transition.
     */
    private PetriNet target;

}
