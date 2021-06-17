package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Places are one type of node of a petri net. They can contain markers, which decide,
 * which transitions can be used to take a step forward in the PetriNet.
 */
@JsonSubTypes({@JsonSubTypes.Type(PlaceImpl.class)})
@JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME, property = "@type")
public interface Place extends Node {

    /**
     * Getter for the number of markers on this place node.
     * @return number of markers
     */
    int getMarkers();

    /**
     * Setter for markers on this place node.
     * @param markers the number of markers this place should have
     */
    void setMarkers(int markers);

}
