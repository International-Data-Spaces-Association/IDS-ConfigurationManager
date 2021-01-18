package de.fraunhofer.isst.configmanager.configmanagement.service;


import de.fraunhofer.isst.configmanager.configmanagement.entities.config.RepresentationEndpointObject;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.RepresentationEndpointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

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

    /**
     * This method returns for an specific endpoint id the value from the map.
     *
     * @param endpointId id of the endpoint
     * @return representation uri
     */
    public URI getRepresentationId(URI endpointId) {

        List<RepresentationEndpointObject> list = representationEndpointRepository.findAll();

        for (RepresentationEndpointObject object : list) {
            if (object.getMap().containsKey(endpointId)) {
                return object.getMap().get(endpointId);
            }
        }
        return null;
    }

}
