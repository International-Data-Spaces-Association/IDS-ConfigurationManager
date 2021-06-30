/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.isst.configmanager.petrinet.model;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.net.URI;
import java.util.*;

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
        Map<URI, Node> nodeClones = new HashMap<>();
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

    private boolean nodesEqual(Set<Node> otherNodes){
        return nodes.stream().map(s -> otherNodes.stream().filter(n -> n.getID().equals(s.getID())).anyMatch(n -> n.equals(s))).reduce(true, (a, b) -> a && b);
    }

    private boolean arcsEqual(Set<Arc> otherArcs){
        return arcs.stream().map(s -> otherArcs.stream().anyMatch(n -> n.equals(s))).reduce(true, (a, b) -> a && b);
    }
}
