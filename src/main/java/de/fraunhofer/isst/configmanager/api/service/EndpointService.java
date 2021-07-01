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
package de.fraunhofer.isst.configmanager.api.service;

import de.fraunhofer.iais.eis.BasicAuthenticationBuilder;
import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.iais.eis.GenericEndpoint;
import de.fraunhofer.iais.eis.GenericEndpointBuilder;
import de.fraunhofer.iais.eis.GenericEndpointImpl;
import de.fraunhofer.isst.configmanager.data.entities.CustomGenericEndpointList;
import de.fraunhofer.isst.configmanager.data.entities.CustomGenericEndpointObject;
import de.fraunhofer.isst.configmanager.data.repositories.CustomGenericEndpointRepository;
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
        final var endpoint = new GenericEndpointBuilder()._accessURL_(accessURL).build();
        final var endpointImpl = (GenericEndpointImpl) endpoint;

        endpointImpl.setProperty("ids:sourceType", sourceType);

        if (username != null && password != null) {
            endpointImpl
                    .setGenericEndpointAuthentication(
                            new BasicAuthenticationBuilder()._authUsername_(username)._authPassword_(password).build()
                    );
        } else {
            if (log.isInfoEnabled()) {
                log.info("---- [EndpointService createGenericEndpoint] No authentication was created because username and password were not entered.");
            }
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
            if (log.isInfoEnabled()) {
                log.info("---- [EndpointService getGenericEndpoints] Generic endpoints found: " + genericEndpoints.size());
            }
        } catch (NoSuchElementException e) {
            if (log.isInfoEnabled()) {
                log.info("---- [EndpointService getGenericEndpoints] No generic endpoints found!");
            }
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

        var updated = false;
        final var genericEndpointold = getGenericEndpoint(id);
        final var genericEndpointNew = new GenericEndpointBuilder(genericEndpointold.getId())
                ._accessURL_(genericEndpointold.getAccessURL())
                ._genericEndpointAuthentication_(genericEndpointold.getGenericEndpointAuthentication())
                .build();

        if (genericEndpointNew != null) {
            final var genericEndpointNewImpl = (GenericEndpointImpl) genericEndpointNew;

            if (sourceType != null) {
                genericEndpointNewImpl.setProperty("ids:sourceType", sourceType);
            }

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

        final var customGenericEndpoints = customGenericEndpointList.getCustomGenericEndpointObjects();

        for (var i = 0; i < customGenericEndpoints.size(); i++) {
            if (id.equals(customGenericEndpoints.get(i).getEndpoint().getId())) {
                customGenericEndpoints.set(i, new CustomGenericEndpointObject(genericEndpointNew));
                customGenericEndpointList = customGenericEndpointRepository.saveAndFlush(customGenericEndpointList);
                updated = true;
            }
        }
        return updated;
    }
}
