package de.fraunhofer.isst.configmanager.petrinet.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class ContextObject {

    private List<String> context;
    private String read;
    private String write;
    private String erase;
    private TransType type;

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

    public enum TransType {
        APP,
        CONTROL
    }
}
