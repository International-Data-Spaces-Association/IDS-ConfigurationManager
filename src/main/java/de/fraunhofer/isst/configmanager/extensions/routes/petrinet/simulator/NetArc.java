package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.simulator;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.PetriNet;
import lombok.Getter;

import java.net.URI;

/**
 * Arc connecting Steps in a PetriNet execution inside the {@link StepGraph}.
 */
@Getter
public class NetArc {
    /**
     * PetriNet from which target is reachable, using a transition.
     */
    private PetriNet source;


    /**
     * PetriNet that can be reached from source, using a transition.
     */
    private PetriNet target;

    private URI usedTransition;

    public NetArc(final PetriNet source, final PetriNet target, final URI usedTransition) {
        this.source = source;
        this.target = target;
        this.usedTransition = usedTransition;
    }
}
