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

    private transient ContextObject contextObject;

    @JsonIgnore
    private transient Set<Arc> sourceArcs;

    @JsonIgnore
    private transient Set<Arc> targetArcs;

    public TransitionImpl(final URI id) {
        this.id = id;
        this.sourceArcs = new HashSet<>();
        this.targetArcs = new HashSet<>();
    }

    public void setContextObject(final ContextObject contextObject) {
        this.contextObject = contextObject;
    }

    @Override
    public ContextObject getContext() {
        return contextObject;
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
    public boolean isComplementOf(final Node other) {
        return Place.class.isAssignableFrom(other.getClass());
    }

    @Override
    public Node deepCopy() {
        final var copy = new TransitionImpl(this.getID());
        if (this.contextObject != null) {
            copy.setContextObject(this.contextObject.deepCopy());
        }

        return copy;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final var trans = (TransitionImpl) o;

        return Objects.equals(id, trans.id) && Objects.equals(contextObject, trans.contextObject);
    }
}

