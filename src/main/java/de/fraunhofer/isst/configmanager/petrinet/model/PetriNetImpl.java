package de.fraunhofer.isst.configmanager.petrinet.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.net.URI;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation class of the {@link PetriNet} interface.
 */
public class PetriNetImpl implements PetriNet, HasId {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    private URI id;
    private Set<Node> nodes;
    private Set<Arc> arcs;
    
    public PetriNetImpl(URI id, Set<Node> nodes, Set<Arc> arcs) {
        this.id = id;
        this.nodes = nodes;
        this.arcs = arcs;
    }
    
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
        var nodeCopy = new HashSet<Node>();
        for (var node : nodes) {
            nodeCopy.add(node.deepCopy());
        }
        var arcCopy = new HashSet<Arc>();
        for (var arc : arcs) {
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
     * Get a node by its id (if it exists)
     * @param id the ID of the Node to search for
     * @param nodes a Set of Nodes
     * @return the node with the given id (if it exists)
     */
    private static Node nodeById(URI id, Set<Node> nodes) {
        for (var node : nodes) {
            if (node.getID().equals(id)) return node;
        }
        return null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PetriNetImpl petriNet = (PetriNetImpl) o;
        var eq1 = Objects.equals(id, petriNet.id);
        var eq2 = nodes.stream().map(s -> petriNet.nodes.stream().filter(n -> n.getID().equals(s.getID())).anyMatch(n -> n.equals(s))).reduce(true, (a, b) -> a && b);
        var eq3 = arcs.stream().map(s -> petriNet.arcs.stream().anyMatch(n -> n.equals(s))).reduce(true, (a, b) -> a && b);
        return eq1 && eq2 && eq3;
    }
    
}
