package de.fraunhofer.isst.configmanager.petrinet.model;

import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.util.Objects;

@Getter
@Setter
public class InnerPlace extends PlaceImpl{

    private Transition originalTrans;

    public InnerPlace(URI id, Transition originalTrans) {
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
