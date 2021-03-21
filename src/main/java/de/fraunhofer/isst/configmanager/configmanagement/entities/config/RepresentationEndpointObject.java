package de.fraunhofer.isst.configmanager.configmanagement.entities.config;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entity class for caching the resource representation id associated with the endpoint id.
 */
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RepresentationEndpointObject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    long id;

    @ElementCollection
    transient Map<URI, URI> map = new ConcurrentHashMap<>();

    public Map<URI, URI> getMap() {
        return map;
    }

    public void put(final URI endpointId, final URI representationId) {
        map.put(endpointId, representationId);
    }
}
