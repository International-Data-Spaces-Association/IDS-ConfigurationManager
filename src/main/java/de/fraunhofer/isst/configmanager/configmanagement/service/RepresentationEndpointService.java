package de.fraunhofer.isst.configmanager.configmanagement.service;


import de.fraunhofer.isst.configmanager.configmanagement.entities.config.RepresentationEndpointObject;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.RepresentationEndpointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;

/**
 * Service class to cache the resource representation associated with the endpoint
 */
@Service
public class RepresentationEndpointService {

    private final RepresentationEndpointRepository representationEndpointRepository;

    @Autowired
    public RepresentationEndpointService(RepresentationEndpointRepository representationEndpointRepository) {
        this.representationEndpointRepository = representationEndpointRepository;
    }

    /**
     * This method saves the representation endpoint object in the database.
     *
     * @param endpointId       id of the endpoint
     * @param representationId id of the representation
     */
    public void createRepresentationEndpoint(URI endpointId, URI representationId) {

        RepresentationEndpointObject representationEndpointObject = new RepresentationEndpointObject();
        representationEndpointObject.put(endpointId, representationId);
        representationEndpointRepository.save(representationEndpointObject);
    }
}
