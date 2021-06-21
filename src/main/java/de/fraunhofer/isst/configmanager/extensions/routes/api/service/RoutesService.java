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
package de.fraunhofer.isst.configmanager.extensions.routes.api.service;

import de.fraunhofer.iais.eis.AppRoute;
import de.fraunhofer.iais.eis.AppRouteImpl;
import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.iais.eis.GenericEndpoint;
import de.fraunhofer.iais.eis.RouteStep;
import de.fraunhofer.isst.configmanager.extensions.configuration.api.service.ConfigurationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;

/**
 * Service class for managing app routes in the configuration manager.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RoutesService {

    transient ConfigurationService configModelService;

    /**
     * This method creates an app route.
     *
     * @param description description of the app route
     * @return app route
     */
    public AppRoute createAppRoute(final String description) {
        //TODO: Save to DB
        return null;
    }

    /**
     * This method deletes an app route.
     *
     * @param routeId id of the app route
     * @return true, if app route is deleted
     */
    public boolean deleteAppRoute(final URI routeId) {
        //TODO: Delete in DB
        return true;
    }

    /**
     * This method returns an app route.
     *
     * @param routeId id of the app route
     * @return app route
     */
    public AppRoute getAppRoute(final URI routeId) {
        //TODO: Get from DB
        return null;
    }

    /**
     * @return list of app routes
     */
    public List<AppRoute> getAppRoutes() {
        //TODO: Get from DB
        return null;
    }

    /**
     * This method returns a specific app route with the given parameter.
     *
     * @param routeId id of the route
     * @return app route implementation
     */
    private AppRouteImpl getAppRouteImpl(final URI routeId) {
        //TODO: Get from DB
        return null;
    }

    public RouteStep createAppRouteStep(final URI routeId,
                                        final URI startId,
                                        final int startCoordinateX,
                                        final int startCoordinateY,
                                        final URI endID,
                                        final int endCoordinateX,
                                        final int endCoordinateY,
                                        final URI resourceId) {

        //TODO: Save in DB
        return null;
    }

    /**
     * This method returns an endpoint information.
     *
     * @param routeId    id of the route
     * @param endpointId id of the endpoint
     * @return endpoint information
     */
    public Object getEndpointInformation(final URI routeId, final URI endpointId) {
        //TODO: Get from DB
        return null;
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
