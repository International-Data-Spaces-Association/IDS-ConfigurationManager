package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.simulator;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.PetriNet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.net.URI;

/**
 * Arc connecting Steps in a PetriNet execution inside the {@link StepGraph}.
 */
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NetArc {
    /**
     * PetriNet from which target is reachable, using a transition.
     */
    PetriNet source;


    /**
     * PetriNet that can be reached from source, using a transition.
     */
    PetriNet target;

    URI usedTransition;
}
