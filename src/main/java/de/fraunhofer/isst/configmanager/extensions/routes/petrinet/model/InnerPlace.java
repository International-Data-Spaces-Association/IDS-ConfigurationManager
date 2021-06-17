package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model;

import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.util.Objects;

/**
 * Used for inner places of unfolded transitions (has a originalTrans field to access the original transition which
 * was unfolded).
 */
@Getter
@Setter
public class InnerPlace extends PlaceImpl {

    /**
     * Original Transition, which was unfolded to create the InnerPlace.
     */
    private Transition originalTrans;

    public InnerPlace(final URI id, final Transition originalTrans) {
        super(id);
        this.originalTrans = originalTrans;
    }

    @Override
    public Node deepCopy() {
        final var copy = new InnerPlace(this.getID(), this.originalTrans);
        copy.setMarkers(this.getMarkers());
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

        final var place = (InnerPlace) o;

        return originalTrans.equals(place.originalTrans) && getMarkers() == place.getMarkers() && Objects.equals(getID(), place.getID());
    }
}
