package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation class of the {@link PetriNet} interface.
 */
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PetriNetImpl implements PetriNet, HasId {

    transient URI id;
    transient Set<Node> nodes;
    transient Set<Arc> arcs;

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
        final Map<URI, Node> nodeClones = new HashMap<>();
        for (final var node : nodes) {
            nodeClones.put(node.getID(), node.deepCopy());
        }
        nodeCopy.addAll(nodeClones.values());

        final var arcCopy = new HashSet<Arc>();
        for (final var arc : arcs) {
            arcCopy.add(
                    new ArcImpl(
                            (Node) nodeClones.get(arc.getSource().getID()),
                            (Node) nodeClones.get(arc.getTarget().getID())
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
        return Objects.equals(id, petriNet.id) && arcsEqual(petriNet.arcs) && nodesEqual(petriNet.nodes);
    }

    private boolean nodesEqual(final Set<Node> otherNodes) {
        return nodes.stream().map(s -> otherNodes.stream().filter(n -> n.getID().equals(s.getID())).anyMatch(n -> n.equals(s))).reduce(true, (a, b) -> a && b);
    }

    private boolean arcsEqual(final Set<Arc> otherArcs) {
        return arcs.stream().map(s -> otherArcs.stream().anyMatch(n -> n.equals(s))).reduce(true, (a, b) -> a && b);
    }
}
