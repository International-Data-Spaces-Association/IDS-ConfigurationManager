package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configlists.CustomGenericEndpointList;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configlists.CustomGenericEndpointRepository;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customgenericendpoint.CustomGenericEndpointObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Service class for managing generic endpoints
 */
@Service
@Slf4j
public class EndpointService {
    private transient final CustomGenericEndpointRepository customGenericEndpointRepository;
    private transient CustomGenericEndpointList customGenericEndpointList;

    @Autowired
    public EndpointService(CustomGenericEndpointRepository customGenericEndpointRepository) {

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
    public GenericEndpoint createGenericEndpoint(String accessURL, String username, String password) {
        // Create generic endpoint
        GenericEndpoint endpoint = new GenericEndpointBuilder()._accessURL_(URI.create(accessURL)).build();
        var endpointImpl = (GenericEndpointImpl) endpoint;
        if (username != null && password != null) {
            endpointImpl.setGenericEndpointAuthentication(new BasicAuthenticationBuilder()._authUsername_(username)
                    ._authPassword_(password).build());
        } else{
            log.info("---- No authentication was created because username and password were not entered.");
        }
        // Save the endpoint
        CustomGenericEndpointObject customGenericEndpointObject = new CustomGenericEndpointObject(endpoint);
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
        try {
            customGenericEndpointList = customGenericEndpointRepository.findAll().stream().findAny().get();
            return customGenericEndpointList.getEndpoints();
        } catch (NoSuchElementException e) {
            return new ArrayList<>();
        }
    }

    /**
     * @param id id of the generic endpoint
     * @return generic endpoint
     */
    public GenericEndpoint getGenericEndpoint(URI id) {
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
    public boolean deleteGenericEndpoint(URI id) {
        boolean deleted = customGenericEndpointList.getCustomGenericEndpointObjects()
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
    public boolean updateGenericEndpoint(URI id, String accessURL, String username, String password) {
        boolean updated = false;
        GenericEndpoint genericEndpointold = getGenericEndpoint(id);
        GenericEndpoint genericEndpointNew = getGenericEndpoint(id);

        if (genericEndpointNew != null) {
            var genericEndpointNewImpl = (GenericEndpointImpl) genericEndpointNew;
            if (accessURL != null) {
                genericEndpointNewImpl.setAccessURL(URI.create(accessURL));
            }
            BasicAuthentication basicAuthentication = genericEndpointNew.getGenericEndpointAuthentication();

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

        int index = this.getGenericEndpoints().indexOf(genericEndpointold);
        if (index != -1) {
            this.getGenericEndpoints().set(index, genericEndpointNew);
            customGenericEndpointList = customGenericEndpointRepository.saveAndFlush(customGenericEndpointList);
            updated = true;
        }
        return updated;
    }
}
