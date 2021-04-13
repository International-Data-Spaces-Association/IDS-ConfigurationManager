package de.fraunhofer.isst.configmanager.api.service;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.api.service.resources.ResourceService;
import de.fraunhofer.isst.configmanager.model.configlists.CustomAppRepository;
import de.fraunhofer.isst.configmanager.model.configlists.EndpointInformationRepository;
import de.fraunhofer.isst.configmanager.model.configlists.RouteDeployMethodRepository;
import de.fraunhofer.isst.configmanager.model.customapp.CustomApp;
import de.fraunhofer.isst.configmanager.model.endpointinfo.EndpointInformation;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Service class for managing app routes in the configuration manager.
 */
@Slf4j
@Service
@Transactional
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AppRouteService {

    transient ConfigModelService configModelService;
    transient EndpointService endpointService;
    transient ResourceService resourceService;
    transient RouteDeployMethodRepository routeDeployMethodRepository;
    transient EndpointInformationRepository endpointInformationRepository;
    transient CustomAppRepository customAppRepository;

    @Autowired
    public AppRouteService(final ConfigModelService configModelService,
                           final RouteDeployMethodRepository routeDeployMethodRepository,
                           final EndpointInformationRepository endpointInformationRepository,
                           final CustomAppRepository customAppRepository,
                           final EndpointService endpointService,
                           final ResourceService resourceService) {
        this.configModelService = configModelService;
        this.routeDeployMethodRepository = routeDeployMethodRepository;
        this.endpointInformationRepository = endpointInformationRepository;
        this.customAppRepository = customAppRepository;
        this.endpointService = endpointService;
        this.resourceService = resourceService;
    }

    /**
     * This method creates an app route.
     *
     * @param description description of the app route
     * @return app route
     */
    public AppRoute createAppRoute(final String description) {

        final var configModelImpl = (ConfigurationModelImpl) configModelService.getConfigModel();

        if (configModelService.getConfigModel().getAppRoute() == null) {
            configModelImpl.setAppRoute(new ArrayList<>());
        }

        final var appRoutes = (ArrayList<AppRoute>) configModelImpl.getAppRoute();
        final var routeDeployMethod = routeDeployMethodRepository.findAll();
        String deployMethod;

        if (routeDeployMethod.isEmpty()) {
            deployMethod = "custom";
        } else {
            deployMethod = routeDeployMethod.get(0).getDeployMethod().toString();
        }

        final var appRoute = new AppRouteBuilder()
                ._routeDeployMethod_(deployMethod)
                ._routeDescription_(description)
                .build();

        appRoutes.add(appRoute);
        configModelImpl.setAppRoute(appRoutes);

        configModelService.saveState();

        return appRoute;
    }

    /**
     * This method updates the app route.
     *
     * @param routeId     if of the app route
     * @param description desciption of the app route
     * @return true, if app route is updated
     */
    public boolean updateAppRoute(final URI routeId, final String description) {
        boolean updated = false;

        final var appRouteImpl = getAppRouteImpl(routeId);

        if (appRouteImpl != null) {
            appRouteImpl.setAppRouteBroker(null);
            appRouteImpl.setAppRouteStart(null);
            appRouteImpl.setAppRouteEnd(null);
            appRouteImpl.setAppRouteOutput(null);
            appRouteImpl.setHasSubRoute(null);
            appRouteImpl.setRouteConfiguration(null);
            appRouteImpl.setRouteDescription(description);

            final var routeDeployMethod = routeDeployMethodRepository.findAll();

            String deployMethod;

            if (routeDeployMethod.isEmpty()) {
                deployMethod = "custom";
            } else {
                deployMethod = routeDeployMethod.get(0).getDeployMethod().toString();
            }

            appRouteImpl.setRouteDeployMethod(deployMethod);
            configModelService.saveState();
            updated = true;
        }

        return updated;
    }

    /**
     * This method deletes an app route.
     *
     * @param routeId id of the app route
     * @return true, if app route is deleted
     */
    public boolean deleteAppRoute(final URI routeId) {
        boolean deleted = false;

        final var appRoute = getAppRoute(routeId);

        if (appRoute != null) {
            deleted = configModelService.getConfigModel().getAppRoute().remove(appRoute);

            if (deleted) {
                configModelService.saveState();
            }
        }

        return deleted;
    }

    /**
     * This method returns an app route.
     *
     * @param routeId id of the app route
     * @return app route
     */
    public AppRoute getAppRoute(final URI routeId) {
        return configModelService.getConfigModel().getAppRoute()
                .stream().filter(appRoute1 -> appRoute1.getId().equals(routeId)).findAny().orElse(null);
    }

    /**
     * @return list of app routes
     */
    public List<AppRoute> getAppRoutes() {
        return (List<AppRoute>) configModelService.getConfigModel().getAppRoute();
    }

    /**
     * This method returns the specific soubroute.
     *
     * @param routeId     id of the app route
     * @param routeStepId id of the subroute
     * @return subroute
     */
    public RouteStep getSubroute(final URI routeId, final URI routeStepId) {
        RouteStepImpl routeStep = null;
        final var appRouteImpl = getAppRouteImpl(routeId);

        if (appRouteImpl != null) {
            routeStep = getSubrouteImpl(routeStepId, appRouteImpl);
        }

        return routeStep;
    }

    /**
     * This method returns a specific app route with the given parameter.
     *
     * @param routeId id of the route
     * @return app route implementation
     */
    private AppRouteImpl getAppRouteImpl(final URI routeId) {
        return (AppRouteImpl) configModelService.getConfigModel().getAppRoute()
                .stream().filter(appRoute -> appRoute.getId().equals(routeId)).findAny().orElse(null);
    }

    /**
     * This method returns a specific sub route with the given parameters.
     *
     * @param routeStepId  id of the subroute
     * @param appRouteImpl app route implementation
     * @return sub route implementation
     */
    private RouteStepImpl getSubrouteImpl(final URI routeStepId, final AppRouteImpl appRouteImpl) {
        return (RouteStepImpl) appRouteImpl.getHasSubRoute().stream()
                .filter(routeStep -> routeStep.getId().equals(routeStepId)).findAny().orElse(null);
    }

    public RouteStep createAppRouteStep(final URI routeId, final URI startId,
                                        final int startCoordinateX, final int startCoordinateY,
                                        final URI endID, final int endCoordinateX,
                                        final int endCoordinateY, final URI resourceId) {

        RouteStep routeStep = null;
        // Create and save the endpoints of the route with the respective coordinates
        final var startEndpointInformation =
                new EndpointInformation(routeId.toString(), startId.toString(), startCoordinateX,
                        startCoordinateY);

        final var endEndpointInformation =
                new EndpointInformation(routeId.toString(), endID.toString(), endCoordinateX,
                        endCoordinateY);

        endpointInformationRepository.save(startEndpointInformation);
        endpointInformationRepository.save(endEndpointInformation);

        final var appRouteImpl = getAppRouteImpl(routeId);
        if (appRouteImpl != null) {

            if (appRouteImpl.getHasSubRoute() == null) {
                appRouteImpl.setHasSubRoute(new ArrayList<>());
            }
            final var routeSteps = (ArrayList<RouteStep>) appRouteImpl.getHasSubRoute();

            // Determine endpoints
            final var startEndpoint = getEndpoint(startId);
            final var endpoint = getEndpoint(endID);

            // Set app route start and end
            if (routeSteps.isEmpty()) {
                appRouteImpl.setAppRouteStart(Util.asList(startEndpoint));
            }
            appRouteImpl.setAppRouteEnd(Util.asList(endpoint));

            // Get route deploy method for route step
            final var routeDeployMethod = routeDeployMethodRepository.findAll();

            String deployMethod;
            if (routeDeployMethod.isEmpty()) {
                deployMethod = "custom";
            } else {
                deployMethod = routeDeployMethod.get(0).getDeployMethod().toString();
            }

            // Create route step
            if (startEndpoint != null && endpoint != null) {
                final var resource = resourceService.getResource(resourceId);
                if (resource != null) {

                    // Set resource endpoint
                    if (configModelService.getConfigModel().getConnectorDescription().getHasEndpoint() == null
                            || configModelService.getConfigModel().getConnectorDescription().getHasEndpoint().isEmpty()) {

                        final var baseConnectorImpl =
                                (BaseConnectorImpl) configModelService.getConfigModel().getConnectorDescription();
                        baseConnectorImpl.setHasEndpoint(Util.asList(new ConnectorEndpointBuilder()
                                ._accessURL_(URI.create("http://api/ids/data")).build()));
                    }
                    final var connectorEndpoint =
                            configModelService.getConfigModel().getConnectorDescription()
                            .getHasEndpoint().get(0);
                    final var resourceImpl = (ResourceImpl) resource;
                    resourceImpl.setResourceEndpoint(Util.asList(connectorEndpoint));

                    routeStep = new RouteStepBuilder()._routeDeployMethod_(deployMethod)
                            ._appRouteStart_(Util.asList(startEndpoint))
                            ._appRouteEnd_(Util.asList(endpoint))
                            ._appRouteOutput_(Util.asList(resourceImpl))
                            .build();
                } else {
                    routeStep = new RouteStepBuilder()._routeDeployMethod_(deployMethod)
                            ._appRouteStart_(Util.asList(startEndpoint))
                            ._appRouteEnd_(Util.asList(endpoint))
                            .build();
                }
                routeSteps.add(routeStep);
                configModelService.saveState();
            }
        }
        return routeStep;
    }

    /**
     * This method returns an generic endpoint, app endpoint or a connector endpoint.
     *
     * @param endpointId id of the endpoint
     * @return endpoint
     */
    private Endpoint getEndpoint(final URI endpointId) {
        Endpoint endpoint = null;

        // Search endpoint in the app repository
        final var customAppList = customAppRepository.findAll();
        if (!customAppList.isEmpty() && endpointId.toString().contains("appEndpoint")) {
            final var customApp = customAppList.stream()
                    .map(CustomApp::getAppEndpointList)
                    .flatMap(Collection::stream)
                    .filter(customAppEndpoint -> customAppEndpoint.getEndpoint().getId().equals(endpointId))
                    .findAny().orElse(null);

            if (customApp != null) {
                endpoint = customApp.getEndpoint();
            }
        }
        // Search endpoint in the backend repository and in list of connector endpoints
        if (endpoint == null && endpointService.getGenericEndpoints().size() != 0 && endpointId.toString().contains("genericEndpoint")) {
            final var genericEndpoint = endpointService.getGenericEndpoint(endpointId);

            if (genericEndpoint != null) {
                endpoint = genericEndpoint;
            }
        }

        if (endpoint == null && configModelService.getConfigModel().getConnectorDescription().getHasEndpoint().size() != 0
                && endpointId.toString().contains("connectorEndpoint")) {

            endpoint = configModelService.getConfigModel().getConnectorDescription().getHasEndpoint()
                    .stream().filter(connectorEndpoint -> connectorEndpoint.getId().equals(endpointId))
                    .findAny().orElse(null);
        }

        return endpoint;
    }

    /**
     * This method returns an endpoint information.
     *
     * @param routeId    id of the route
     * @param endpointId id of the endpoint
     * @return endpoint information
     */
    public EndpointInformation getEndpointInformation(final URI routeId, final URI endpointId) {
        EndpointInformation returnEndpointInfo = null;
        final var endpointInformations = endpointInformationRepository.findAll();

        if (!endpointInformations.isEmpty()) {
            for (final var endpointInformation : endpointInformations) {
                if (routeId.toString().equals(endpointInformation.getRouteId())
                        && endpointId.toString().equals(endpointInformation.getEndpointId())) {
                    returnEndpointInfo = endpointInformation;
                }
            }
        }

        return returnEndpointInfo;
    }

    /**
     * @return all endpoint information
     */
    public List<EndpointInformation> getAllEndpointInfo() {
        return endpointInformationRepository.findAll();
    }

    /**
     * This method deletes a route step with the given parameters.
     *
     * @param routeId     id of the app route
     * @param routeStepId id of the route step
     * @return true, if route step is deleted
     */
    public boolean deleteAppRouteStep(final URI routeId, final URI routeStepId) {
        boolean deleted = false;
        final var appRouteImpl = getAppRouteImpl(routeId);
        if (appRouteImpl != null) {
            deleted =
                    appRouteImpl.getHasSubRoute().removeIf(routeStep -> routeStep.getId().equals(routeStepId));
            if (deleted) {
                configModelService.saveState();
            }
        }
        return deleted;
    }
}
