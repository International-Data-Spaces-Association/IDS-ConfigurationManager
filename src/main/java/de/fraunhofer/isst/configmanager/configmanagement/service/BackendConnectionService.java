package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.isst.configmanager.configmanagement.entities.backendConnection.BackendConnectionObject;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.BackendConnectionList;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.BackendConnectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Service
public class BackendConnectionService {

    private final static Logger logger = LoggerFactory.getLogger(BackendConnectionService.class);
    private final BackendConnectionRepository backendConnectionRepository;
    private BackendConnectionList backendConnectionList;

    @Autowired
    public BackendConnectionService(BackendConnectionRepository backendConnectionRepository) {

        this.backendConnectionRepository = backendConnectionRepository;
    }

    public GenericEndpoint createBackendConnection(String accessURL, String username, String password) {

        // Create generic endpoint
        GenericEndpoint endpoint = new GenericEndpointBuilder()._accessURL_(URI.create(accessURL)).build();
        var endpointImpl = (GenericEndpointImpl) endpoint;
        if (username != null && password != null) {
            endpointImpl.setGenericEndpointAuthentication(new BasicAuthenticationBuilder()._authUsername_(username)
                    ._authPassword_(password).build());
        }

        // Save the endpoint
        BackendConnectionObject backendConnectionObject = new BackendConnectionObject(endpoint);
        if (backendConnectionRepository.count() == 0) {
            backendConnectionList = new BackendConnectionList();
        } else {
            backendConnectionList = backendConnectionRepository.findAll().stream().findAny().get();
        }
        backendConnectionList.getBackendConnectionObjects().add(backendConnectionObject);
        backendConnectionList = backendConnectionRepository.saveAndFlush(backendConnectionList);
        return endpoint;
    }

    public List<Endpoint> getBackendConnections() {
        return this.backendConnectionList.getEndpoints();
    }

    public GenericEndpoint getBackendConnection(URI id) {

        return (GenericEndpoint) this.backendConnectionList.getEndpoints().stream().filter(endpoint -> endpoint.getId().equals(id))
                .findAny().orElse(null);
    }

    public boolean deleteBackendConnection(URI id) {

        boolean deleted = false;
        GenericEndpoint genericEndpoint = getBackendConnection(id);
        if (genericEndpoint != null) {
            deleted = backendConnectionList.getBackendConnectionObjects().remove(genericEndpoint);
            backendConnectionList = backendConnectionRepository.saveAndFlush(backendConnectionList);
        }
        return deleted;
    }

    public boolean updateBackendConnection(URI id, String accessURL, String username, String password) {
        boolean updated = false;
        GenericEndpoint genericEndpointold = getBackendConnection(id);
        GenericEndpoint genericEndpointNew = getBackendConnection(id);

        if (genericEndpointNew != null) {
            var genericEndpointNewImpl = (GenericEndpointImpl) genericEndpointNew;
            if (accessURL != null) {
                genericEndpointNewImpl.setAccessURL(URI.create(accessURL));
            }
            BasicAuthentication basicAuthentication = genericEndpointNew.getGenericEndpointAuthentication();
            if (username != null) {
                genericEndpointNewImpl.setGenericEndpointAuthentication(
                        new BasicAuthenticationBuilder(basicAuthentication.getId())
                                ._authPassword_(basicAuthentication.getAuthPassword())
                                ._authUsername_(username).build());
            }
            if (password != null) {
                genericEndpointNewImpl.setGenericEndpointAuthentication(
                        new BasicAuthenticationBuilder(basicAuthentication.getId())
                                ._authPassword_(password)
                                ._authUsername_(basicAuthentication.getAuthPassword()).build());
            }
        }

        int index = this.getBackendConnections().indexOf(genericEndpointold);
        if (index != -1) {
            this.getBackendConnections().set(index, genericEndpointNew);
            backendConnectionList = backendConnectionRepository.saveAndFlush(backendConnectionList);
            updated = true;
        }
        return updated;
    }
}
