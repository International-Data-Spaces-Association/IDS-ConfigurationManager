package de.fraunhofer.isst.configmanager.petrinet.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Set;

/**
 * Interface for Nodes. Nodes can be either {@link Transition} or {@link Place}.
 *
 * Places are regular Nodes, while Transitions decide,
 * which Steps can be made in the PetriNet.
 */
@JsonSubTypes({@JsonSubTypes.Type(Transition.class), @JsonSubTypes.Type(Place.class)})
@JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME, property = "@type")
public interface Node extends HasId {

    /**
     * @return get all {@link Arc}, where this node is the source (this -> other)
     */
    Set<Arc> getSourceArcs();

    /**
     * @return get all {@link Arc}, where this node is the target (other -> this)
     */
    Set<Arc> getTargetArcs();

    /**
     * @param other another node
     * @return true if this node has a different type (eg. other=place, this=transition)
     */
    boolean isComplementOf(Node other);

    /*
     * create a deep copy of the Node.
     * @return a deep copy of the Node
     */
    Node deepCopy();
}
