package de.fraunhofer.isst.configmanager.api.service;

import de.fraunhofer.iais.eis.BasicAuthenticationBuilder;
import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.iais.eis.GenericEndpoint;
import de.fraunhofer.iais.eis.GenericEndpointBuilder;
import de.fraunhofer.iais.eis.GenericEndpointImpl;
import de.fraunhofer.isst.configmanager.model.configlists.CustomGenericEndpointList;
import de.fraunhofer.isst.configmanager.model.configlists.CustomGenericEndpointRepository;
import de.fraunhofer.isst.configmanager.model.customgenericendpoint.CustomGenericEndpointObject;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Service class for managing generic endpoints.
 */
@Slf4j
@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EndpointService {
    final transient CustomGenericEndpointRepository customGenericEndpointRepository;
    transient CustomGenericEndpointList customGenericEndpointList;

    @Autowired
    public EndpointService(final CustomGenericEndpointRepository customGenericEndpointRepository) {
        this.customGenericEndpointRepository = customGenericEndpointRepository;
    }

    /**
     * This method creates a generic endpoint with the given parameters.
     *
     * @param accessURL access url of the endpoint
     * @param username  username for the authentication
     * @param password  password for the authentication
     * @return generic endpoint
     */
    public GenericEndpoint createGenericEndpoint(final URI accessURL,
                                                 final String username,
                                                 final String password) {
        GenericEndpoint endpoint = null;
        endpoint = new GenericEndpointBuilder()._accessURL_(accessURL).build();
        final var endpointImpl = (GenericEndpointImpl) endpoint;

        if (username != null && password != null) {
            endpointImpl
                    .setGenericEndpointAuthentication(
                            new BasicAuthenticationBuilder()._authUsername_(username)._authPassword_(password).build()
                    );
        } else {
            log.info("---- [EndpointService createGenericEndpoint] No authentication was created because username and password were not entered.");
        }

        final var customGenericEndpointObject = new CustomGenericEndpointObject(endpoint);

        if (customGenericEndpointRepository.count() == 0) {
            customGenericEndpointList = new CustomGenericEndpointList();
        } else {
            customGenericEndpointList = customGenericEndpointRepository.findAll().stream().findAny().get();
        }

        customGenericEndpointList.getCustomGenericEndpointObjects().add(customGenericEndpointObject);
        customGenericEndpointList = customGenericEndpointRepository.saveAndFlush(customGenericEndpointList);
        return endpoint;
    }

    /**
     * @return list of generic endpoints
     */
    public List<Endpoint> getGenericEndpoints() {
        var genericEndpoints = new ArrayList<Endpoint>();

        try {
            customGenericEndpointList = customGenericEndpointRepository.findAll().stream().findAny().get();
            genericEndpoints = (ArrayList<Endpoint>) customGenericEndpointList.getEndpoints();
            log.info("---- [EndpointService getGenericEndpoints] Generic endpoints found: " + genericEndpoints.size());
        } catch (NoSuchElementException e) {
            log.info("---- [EndpointService getGenericEndpoints] No generic endpoints found!");
        }

        return genericEndpoints;
    }

    /**
     * @param id id of the generic endpoint
     * @return generic endpoint
     */
    public GenericEndpoint getGenericEndpoint(final URI id) {
        customGenericEndpointList = customGenericEndpointRepository.findAll().stream().findAny().get();

        return (GenericEndpoint) this.customGenericEndpointList.getEndpoints()
                .stream()
                .filter(endpoint -> endpoint.getId().equals(id))
                .findAny().orElse(null);
    }

    /**
     * @param id id of the generic endpoint
     * @return true, if generic endpoint is deleted
     */
    public boolean deleteGenericEndpoint(final URI id) {
        final boolean deleted = customGenericEndpointList
                .getCustomGenericEndpointObjects()
                .removeIf(customGenericEndpointObject -> customGenericEndpointObject.getEndpoint().getId().equals(id));

        customGenericEndpointList = customGenericEndpointRepository.saveAndFlush(customGenericEndpointList);

        return deleted;
    }

    /**
     * This method updates a generic endpoint with the given parameters.
     *
     * @param id        id of the generic endpoint
     * @param accessURL access url of the endpoint
     * @param username  username for the authentication
     * @param password  password for the authentication
     * @return true, if generic endpoint is updated
     */
    public boolean updateGenericEndpoint(final URI id,
                                         final URI accessURL,
                                         final String username,
                                         final String password) {

        var updated = false;
        final var genericEndpointold = getGenericEndpoint(id);
        final var genericEndpointNew = getGenericEndpoint(id);

        if (genericEndpointNew != null) {
            final var genericEndpointNewImpl = (GenericEndpointImpl) genericEndpointNew;

            if (accessURL != null) {
                genericEndpointNewImpl.setAccessURL(accessURL);
            }

            final var basicAuthentication = genericEndpointNew.getGenericEndpointAuthentication();

            if (username != null && password != null) {
                genericEndpointNewImpl.setGenericEndpointAuthentication(
                        new BasicAuthenticationBuilder(basicAuthentication.getId())
                                ._authPassword_(password)
                                ._authUsername_(username).build());
            } else if (username != null) {
                genericEndpointNewImpl.setGenericEndpointAuthentication(
                        new BasicAuthenticationBuilder(basicAuthentication.getId())
                                ._authPassword_(basicAuthentication.getAuthPassword())
                                ._authUsername_(username).build());
            } else if (password != null) {
                genericEndpointNewImpl.setGenericEndpointAuthentication(
                        new BasicAuthenticationBuilder(basicAuthentication.getId())
                                ._authPassword_(password)
                                ._authUsername_(basicAuthentication.getAuthUsername()).build());
            }
        }

        final var index = this.getGenericEndpoints().indexOf(genericEndpointold);

        if (index != -1) {
            this.getGenericEndpoints().set(index, genericEndpointNew);
            customGenericEndpointList = customGenericEndpointRepository.saveAndFlush(customGenericEndpointList);
            updated = true;
        }

        return updated;
    }
}
