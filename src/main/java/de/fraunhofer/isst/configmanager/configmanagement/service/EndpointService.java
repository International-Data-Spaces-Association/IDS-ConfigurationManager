package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.CustomGenericEndpointList;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.CustomGenericEndpointRepository;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customGenericEndpoint.CustomGenericEndpointObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EndpointService {

    private final CustomGenericEndpointRepository customGenericEndpointRepository;
    private CustomGenericEndpointList customGenericEndpointList;

    @Autowired
    public EndpointService(CustomGenericEndpointRepository customGenericEndpointRepository) {

        this.customGenericEndpointRepository = customGenericEndpointRepository;
    }

    public GenericEndpoint createGenericEndpoint(String accessURL, String username, String password) {

        // Create generic endpoint
        GenericEndpoint endpoint = new GenericEndpointBuilder()._accessURL_(URI.create(accessURL)).build();
        var endpointImpl = (GenericEndpointImpl) endpoint;
        if (username != null && password != null) {
            endpointImpl.setGenericEndpointAuthentication(new BasicAuthenticationBuilder()._authUsername_(username)
                    ._authPassword_(password).build());
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

    public List<Endpoint> getGenericEndpoints() {
        try {
            customGenericEndpointList = customGenericEndpointRepository.findAll().stream().findAny().get();
            return customGenericEndpointList.getEndpoints();
        } catch (NoSuchElementException e) {
            return new ArrayList<>();
        }
    }

    public GenericEndpoint getGenericEndpoint(URI id) {

        customGenericEndpointList = customGenericEndpointRepository.findAll().stream().findAny().get();
        return (GenericEndpoint) this.customGenericEndpointList.getEndpoints().stream().filter(endpoint -> endpoint.getId().equals(id))
                .findAny().orElse(null);
    }

    public boolean deleteGenericEndpoint(URI id) {

        boolean deleted = customGenericEndpointList.getCustomGenericEndpointObjects()
                .removeIf(customGenericEndpointObject -> customGenericEndpointObject.getEndpoint().getId().equals(id));
        customGenericEndpointList = customGenericEndpointRepository.saveAndFlush(customGenericEndpointList);
        return deleted;
    }

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
            } else if (username != null && password == null) {
                genericEndpointNewImpl.setGenericEndpointAuthentication(
                        new BasicAuthenticationBuilder(basicAuthentication.getId())
                                ._authPassword_(basicAuthentication.getAuthPassword())
                                ._authUsername_(username).build());
            } else if (username == null && password != null) {
                genericEndpointNewImpl.setGenericEndpointAuthentication(
                        new BasicAuthenticationBuilder(basicAuthentication.getId())
                                ._authPassword_(password)
                                ._authUsername_(basicAuthentication.getAuthUsername()).build());
            } else {
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
