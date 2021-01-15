package de.fraunhofer.isst.configmanager.configmanagement.entities.config;


import javax.persistence.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity class for caching the resource representation id associated with the endpoint id.
 */
@Entity
public class RepresentationEndpointObject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ElementCollection
    private Map<URI, URI> map = new HashMap<>();

    public RepresentationEndpointObject() {
    }

    public Map<URI, URI> getMap() {
        return map;
    }

    public void put(URI endpointId, URI representationId) {
        map.put(endpointId, representationId);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
