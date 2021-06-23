package de.fraunhofer.isst.configmanager.petrinet.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

/**
 * Context of a transition (used for WFDU nets).
 */
@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContextObject {

    Set<String> context;
    Set<String> read;
    Set<String> write;
    Set<String> erase;
    TransType type;

   public ContextObject deepCopy() {
       return new ContextObject(context, read, write, erase, type);
   }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final var that = (ContextObject) o;

        return Objects.equals(context, that.context) && Objects.equals(read, that.read) && Objects.equals(write, that.write) && Objects.equals(erase, that.erase);
    }

    @Override
    public int hashCode() {
        return Objects.hash(new ArrayList<>(context), read, write, erase);
    }

    /**
     * Transition types (are they apps or control transitions for the petrinet?), only APP transitions have to be
     * unfolded for parallel checks.
     */
    public enum TransType {
        APP,
        CONTROL
    }
}
