package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.net.URI;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation class of the {@link Place} interface.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlaceImpl implements Place {

    transient URI id;
    int markers;

    @JsonIgnore
    transient Set<Arc> sourceArcs;

    @JsonIgnore
    transient Set<Arc> targetArcs;

    public PlaceImpl(final URI id) {
        this.id = id;
        this.sourceArcs = new HashSet<>();
        this.targetArcs = new HashSet<>();
        this.markers = 0;
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
        return Transition.class.isAssignableFrom(other.getClass());
    }

    @Override
    public Node deepCopy() {
        final var copy = new PlaceImpl(this.getID());
        copy.setMarkers(this.getMarkers());
        return copy;
    }

    @Override
    public int getMarkers() {
        return markers;
    }

    @Override
    public void setMarkers(final int markers) {
        this.markers = markers;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final var place = (PlaceImpl) o;

        return markers == place.markers && Objects.equals(id, place.id);
    }

    public boolean equalsExceptMarking(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final var place = (PlaceImpl) o;
        return Objects.equals(id, place.id);
    }

}
