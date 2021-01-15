package de.fraunhofer.isst.configmanager.petrinet.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Transitions are Nodes, which decide if a step can be taken in the petri net.
 *
 * If all previous nodes of a transistion have a marker, those markers will be taken away
 * and every following node of a transition will get a marker:
 *
 * X -> T -> O  ==> O -> T -> X
 */
@JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME, property = "@type")
@JsonSubTypes({@JsonSubTypes.Type(TransitionImpl.class)})
public interface Transition extends Node{
}
