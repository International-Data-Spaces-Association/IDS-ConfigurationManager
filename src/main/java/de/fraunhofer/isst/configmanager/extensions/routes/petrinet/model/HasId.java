package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model;

import java.net.URI;

/**
 * Interface, implemented by every component of the PetriNet that has an ID.
 */
public interface HasId {
    /**
     * @return the ID of the Object
     */
    URI getID();
}
