package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Interface for Arcs. Arcs are the edges in petri nets and can only connect two Nodes of
 * different type.
 */
@JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME, property = "@type")
@JsonSubTypes({@JsonSubTypes.Type(ArcImpl.class)})
public interface Arc {
    /**
     * Getter for the source node of this arc: X -> ...
     * @return the source node of this arc
     */
    Node getSource();

    /**
     * Getter for the target node of this arc: ... -> X
     * @return the target node of this arc
     */
    Node getTarget();

    /**
     * Setter for the source node of this arc: X -> ...
     * @throws IllegalArgumentException if node has same type as the current target.
     * @param source node that will be set as source
     */
    void setSource(Node source);

    /**
     * Setter for the target node of this arc: ... -> X
     * @throws  IllegalArgumentException if node has same type as the current source.
     * @param target node that will be set as target
     */
    void setTarget(Node target);

}
