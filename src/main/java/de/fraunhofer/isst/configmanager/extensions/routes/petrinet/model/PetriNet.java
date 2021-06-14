package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Set;

/**
 * PetriNet Interface, implemented by {@link PetriNetImpl}
 *
 * A PetriNet is a directed Graph, consisting of two types of {@link Node}:
 * {@link Place} and {@link Transition}, and Edges: {@link Arc}.
 *
 * Arcs can either connect Node->Transition or Transition->Node.
 */
@JsonSubTypes({@JsonSubTypes.Type(PetriNetImpl.class)})
@JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME, property = "@type")
public interface PetriNet {

    /**
     * Getter for the Nodes of the PetriNet.
     * @return Nodes of the PetriNet
     */
    Set<Node> getNodes();

    /**
     * Getter for the Arcs of the PetriNet.
     * @return Nodes of the PetriNet
     */
    Set<Arc> getArcs();

    /**
     * Create a copy of the PetriNet, copy its Nodes and Arcs in the process.
     * @return a deep copy of the current PetriNet
     */
    PetriNet deepCopy();

    /**
     * Equals Method for PetriNets.
     * @param other another object
     * @return true if this equals the given object
     */
    boolean equals(Object other);
}
