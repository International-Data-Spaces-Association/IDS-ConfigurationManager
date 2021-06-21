package de.fraunhofer.isst.configmanager.extensions.routes.api.service;

import de.fraunhofer.iais.eis.AppRoute;
import de.fraunhofer.iais.eis.AppRouteImpl;
import de.fraunhofer.iais.eis.RouteStep;
import de.fraunhofer.isst.configmanager.extensions.configuration.api.service.ConnectorConfigurationService;
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
public class RouteService {

    transient ConnectorConfigurationService configModelService;
    transient EndpointService endpointService;

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
}
