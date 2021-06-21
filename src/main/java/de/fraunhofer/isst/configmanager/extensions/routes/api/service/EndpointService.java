package de.fraunhofer.isst.configmanager.extensions.routes.api.service;

import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.iais.eis.GenericEndpoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;

/**
 * Service class for managing generic endpoints.
 */
@Service
@Transactional
public class EndpointService {
    /**
     * This method creates a generic endpoint with the given parameters.
     *
     * @param accessURL  access url of the endpoint
     * @param sourceType the source type of the representation
     * @param username   username for the authentication
     * @param password   password for the authentication
     * @return generic endpoint
     */
    public GenericEndpoint createGenericEndpoint(final URI accessURL,
                                                 final String sourceType,
                                                 final String username,
                                                 final String password) {
        //TODO: save in DB
        return null;
    }

    /**
     * @return list of generic endpoints
     */
    public List<Endpoint> getGenericEndpoints() {
        //TODO: get from DB
        return null;
    }

    /**
     * @param id id of the generic endpoint
     * @return generic endpoint
     */
    public GenericEndpoint getGenericEndpoint(final URI id) {
        //TODO: get from DB
        return null;
    }

    /**
     * @param id id of the generic endpoint
     * @return true, if generic endpoint is deleted
     */
    public boolean deleteGenericEndpoint(final URI id) {
        //TODO: delete from DB
        return true;
    }

    /**
     * This method updates a generic endpoint with the given parameters.
     *
     * @param id         id of the generic endpoint
     * @param accessURL  access url of the endpoint
     * @param sourceType the source type of the representation
     * @param username   username for the authentication
     * @param password   password for the authentication
     * @return true, if generic endpoint is updated
     */
    public boolean updateGenericEndpoint(final URI id,
                                         final URI accessURL,
                                         final String sourceType,
                                         final String username,
                                         final String password) {
        //TODO: save in DB
        return true;
    }
}
