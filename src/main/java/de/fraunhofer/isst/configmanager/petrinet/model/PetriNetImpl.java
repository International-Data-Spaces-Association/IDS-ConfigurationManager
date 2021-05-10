package de.fraunhofer.isst.configmanager.petrinet.model;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.net.URI;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation class of the {@link PetriNet} interface.
 */
@AllArgsConstructor
public class PetriNetImpl implements PetriNet, HasId {

    private transient URI id;
    private transient Set<Node> nodes;
    private transient Set<Arc> arcs;

    @Override
    public Set<Node> getNodes() {
        return nodes;
    }
    
    @Override
    public Set<Arc> getArcs() {
        return arcs;
    }
    
    @Override
    @SneakyThrows
    public PetriNet deepCopy() {
        final var nodeCopy = new HashSet<Node>();
        for (final var node : nodes) {
            nodeCopy.add(node.deepCopy());
        }

        final var arcCopy = new HashSet<Arc>();

        for (final var arc : arcs) {
            arcCopy.add(
                    new ArcImpl(
                            nodeById(arc.getSource().getID(), nodeCopy),
                            nodeById(arc.getTarget().getID(), nodeCopy)
                    )
            );
        }
        return new PetriNetImpl(this.id, nodeCopy, arcCopy);
    }
    
    @Override
    public URI getID() {
        return id;
    }
    
    /**
     * Get a node by its id (if it exists).
     * @param id the ID of the Node to search for
     * @param nodes a Set of Nodes
     * @return the node with the given id (if it exists)
     */
    private static Node nodeById(final URI id, final Set<Node> nodes) {
        for (final var node : nodes) {
            if (node.getID().equals(id)) {
                return node;
            }
        }
        return null;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final var petriNet = (PetriNetImpl) o;
        final var eq1 = Objects.equals(id, petriNet.id);
        final var eq2 = nodes.stream().map(s -> petriNet.nodes.stream().filter(n -> n.getID().equals(s.getID())).anyMatch(n -> n.equals(s))).reduce(true, (a, b) -> a && b);
        final var eq3 = arcs.stream().map(s -> petriNet.arcs.stream().anyMatch(n -> n.equals(s))).reduce(true, (a, b) -> a && b);

        return eq1 && eq2 && eq3;
    }
}
