package de.fraunhofer.isst.configmanager.petrinet.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.net.URI;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation class of the {@link Transition} interface.
 */
public class TransitionImpl implements Transition {

    private transient URI id;

    @JsonIgnore
    private transient Set<Arc> sourceArcs;

    @JsonIgnore
    private transient Set<Arc> targetArcs;

    public TransitionImpl(URI id){
        this.id = id;
        this.sourceArcs = new HashSet<>();
        this.targetArcs = new HashSet<>();
    }

    @Override
    public URI getID() {
        return id;
    }

    @Override
    public Set<Arc> getSourceArcs() {
        return sourceArcs;
    }

    @Override
    public Set<Arc> getTargetArcs() {
        return targetArcs;
    }

    @Override
    public boolean isComplementOf(Node other) {
        return Place.class.isAssignableFrom(other.getClass());
    }

    @Override
    public Node deepCopy() {
        return new TransitionImpl(this.getID());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransitionImpl trans = (TransitionImpl) o;
        return Objects.equals(id, trans.id);
    }
}

