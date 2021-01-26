package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.CustomAppRepository;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.EndpointInformationRepository;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.RouteDeployMethodRepository;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customApp.CustomApp;
import de.fraunhofer.isst.configmanager.configmanagement.entities.endpointInfo.EndpointInformation;
import de.fraunhofer.isst.configmanager.configmanagement.entities.routeDeployMethod.RouteDeployMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Service class for managing app routes in the configuration manager.
 */
@Service
public class AppRouteService {

    private final static Logger logger = LoggerFactory.getLogger(AppRouteService.class);

    private final ConfigModelService configModelService;
    private final BackendConnectionService backendConnectionService;
    private final ResourceService resourceService;

    private final RouteDeployMethodRepository routeDeployMethodRepository;
    private final EndpointInformationRepository endpointInformationRepository;
    private final CustomAppRepository customAppRepository;

    @Autowired
    public AppRouteService(ConfigModelService configModelService,
                           RouteDeployMethodRepository routeDeployMethodRepository,
                           EndpointInformationRepository endpointInformationRepository,
                           CustomAppRepository customAppRepository,
                           BackendConnectionService backendConnectionService,
                           ResourceService resourceService) {
        this.configModelService = configModelService;
        this.routeDeployMethodRepository = routeDeployMethodRepository;
        this.endpointInformationRepository = endpointInformationRepository;
        this.customAppRepository = customAppRepository;
        this.backendConnectionService = backendConnectionService;
        this.resourceService = resourceService;
    }

    /**
     * This method creates an app route
     *
     * @return app route
     */
    public AppRoute createAppRoute() {

        var configModelImpl = (ConfigurationModelImpl) configModelService.getConfigModel();

        if (configModelService.getConfigModel().getAppRoute() == null) {
            configModelImpl.setAppRoute(new ArrayList<>());
        }
        ArrayList<AppRoute> appRoutes = (ArrayList<AppRoute>) configModelImpl.getAppRoute();
        List<RouteDeployMethod> routeDeployMethod = routeDeployMethodRepository.findAll();
        String deployMethod;
        if (routeDeployMethod.isEmpty()) {
            deployMethod = "custom";
        } else {
            deployMethod = routeDeployMethod.get(0).getDeployMethod().toString();
        }
        AppRoute appRoute = new AppRouteBuilder()._routeDeployMethod_(deployMethod).build();
        appRoutes.add(appRoute);
        configModelImpl.setAppRoute(appRoutes);
        configModelService.saveState();

        return appRoute;

    }

    /**
     * This method updates the app route.
     *
     * @param routeId if of the app route
     * @return true, if app route is updated
     */
    public boolean updateAppRoute(URI routeId) {

        boolean updated = false;

        var appRouteImpl = getAppRouteImpl(routeId);

        if (appRouteImpl != null) {
            appRouteImpl.setAppRouteBroker(null);
            appRouteImpl.setAppRouteStart(null);
            appRouteImpl.setAppRouteEnd(null);
            appRouteImpl.setAppRouteOutput(null);
            appRouteImpl.setHasSubRoute(null);
            appRouteImpl.setRouteConfiguration(null);
            appRouteImpl.setRouteDescription(null);

            List<RouteDeployMethod> routeDeployMethod = routeDeployMethodRepository.findAll();
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

    public boolean deleteAppRoute(URI routeId) {

        boolean deleted = false;

        var appRoute = getAppRoute(routeId);
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
    public AppRoute getAppRoute(URI routeId) {

        return configModelService.getConfigModel().getAppRoute()
                .stream().filter(appRoute1 -> appRoute1.getId().equals(routeId)).findAny().orElse(null);
    }

    /**
     * @return list of app routes
     */
    public List<AppRoute> getAppRoutes() {

        if (configModelService.getConfigModel().getAppRoute() == null) {
            ConfigurationModelImpl configurationModel = (ConfigurationModelImpl) configModelService.getConfigModel();
            configurationModel.setAppRoute(new ArrayList<>());
            configModelService.saveState();

        }
        return new ArrayList<>(configModelService.getConfigModel().getAppRoute());
    }

//    /**
//     * This method creates an app route with the given parameters.
//     *
//     * @param routeId   id of the app route
//     * @param accessUrl the access url for the app route
//     * @param username  username for the authentication
//     * @param password  password for the authentication
//     * @return endpoint
//     */
//    public Endpoint createAppRouteStart(URI routeId, String accessUrl, String username, String password) {
//
//        if (routeId != null) {
//            var appRouteImpl = getAppRouteImpl(routeId);
//
//            if (appRouteImpl != null) {
//                if (appRouteImpl.getAppRouteStart() == null) {
//                    appRouteImpl.setAppRouteStart(new ArrayList<>());
//                }
//                ArrayList<Endpoint> appRoutes = (ArrayList<Endpoint>) appRouteImpl.getAppRouteStart();
//                Endpoint endpoint = createEndpoint(accessUrl, username, password);
//                appRoutes.add(endpoint);
//                appRouteImpl.setAppRouteStart(appRoutes);
//                configModelService.saveState();
//                return endpoint;
//            }
//        }
//        return null;
//    }

//    /**
//     * This method creates for the subroute start an endpoint with the given parameters.
//     * The special feature here is that the start endpoint of the subroute is the same
//     * as the start point of the app route.
//     *
//     * @param routeId     id of the app route
//     * @param routeStepId id of the subroute
//     * @param accessUrl   access url of the subroute endpoint
//     * @param username    username for the authentication
//     * @param password    password for the authentication
//     * @return endpoint
//     */
//    public Endpoint createSubrouteStart(URI routeId, URI routeStepId, String accessUrl, String username, String password) {
//        Endpoint endpoint = createEndpoint(accessUrl, username, password);
//
//        if (routeId != null) {
//            var appRouteImpl = getAppRouteImpl(routeId);
//
//            if (appRouteImpl != null) {
//
//                if (appRouteImpl.getHasSubRoute() == null) {
//                    RouteStep routeStep = new RouteStepBuilder()._routeDeployMethod_("custom").build();
//                    var routeStepImpl = (RouteStepImpl) routeStep;
//                    routeStepImpl.setAppRouteStart(new ArrayList<>());
//
//                    ArrayList<Endpoint> subRouteStartEndpoint = (ArrayList<Endpoint>) routeStepImpl.getAppRouteStart();
//                    subRouteStartEndpoint.add(endpoint);
//                    routeStepImpl.setAppRouteStart(subRouteStartEndpoint);
//
//                    appRouteImpl.setHasSubRoute(Util.asList(routeStepImpl));
//                    configModelService.saveState();
//                    return endpoint;
//                } else {
//                    var routeStepImpl = getSubrouteImpl(routeStepId, appRouteImpl);
//
//                    if (routeStepImpl != null) {
//
//                        if (routeStepImpl.getAppRouteStart() == null) {
//                            routeStepImpl.setAppRouteStart(new ArrayList<>());
//                        }
//                        ArrayList<Endpoint> subRouteStartEndpoint = (ArrayList<Endpoint>) routeStepImpl.getAppRouteStart();
//                        int index = appRouteImpl.getHasSubRoute().indexOf(routeStepImpl);
//                        if (index == 0) {
//                            subRouteStartEndpoint.add(endpoint);
//                        } else {
//                            subRouteStartEndpoint.add(new AppEndpointBuilder()._accessURL_(URI.create(accessUrl))
//                                    ._appEndpointType_(AppEndpointType.OUTPUT_ENDPOINT).build());
//                        }
//                        routeStepImpl.setAppRouteStart(subRouteStartEndpoint);
//                        configModelService.saveState();
//                        return endpoint;
//                    }
//                }
//            }
//        }
//        return null;
//    }

    /**
     * This method creates an endpoint with the given parameters.
     *
     * @param accessUrl access url of the endpoint
     * @param username  username for the authentication
     * @param password  password for the authentication
     * @return endpoint
     */
    private Endpoint createEndpoint(String accessUrl, String username, String password) {

        GenericEndpoint endpoint = new GenericEndpointBuilder()._accessURL_(URI.create(accessUrl)).build();

        var endpointImpl = (GenericEndpointImpl) endpoint;

        if (username != null && password != null) {
            endpointImpl.setGenericEndpointAuthentication(
                    new BasicAuthenticationBuilder()._authUsername_(username)._authPassword_(password).build());
        }

        return endpoint;
    }
//
//    /**
//     * This method returns the start endpoint from the app route.
//     *
//     * @param routeId    id of the app route
//     * @param endpointId id of the endpoint
//     * @return endpoint
//     */
//    public Endpoint getAppRouteStart(URI routeId, URI endpointId) {
//
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//            return appRouteImpl.getAppRouteStart().stream().filter(endpoint -> endpoint.getId().equals(endpointId))
//                    .findAny().orElse(null);
//        }
//        return null;
//    }
//
//    /**
//     * This method returns the start endpoint from a subroute.
//     *
//     * @param routeId     id of the app route
//     * @param routeStepId id of the subroute
//     * @param endpointId  id of the endpoint
//     * @return endpoint
//     */
//    public Endpoint getSubrouteStart(URI routeId, URI routeStepId, URI endpointId) {
//
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//            var routeImpl = getSubrouteImpl(routeStepId, appRouteImpl);
//
//            if (routeImpl != null) {
//                return routeImpl.getAppRouteStart().stream().filter(endpoint -> endpoint.getId().equals(endpointId))
//                        .findAny().orElse(null);
//            }
//        }
//        return null;
//    }
//
//    /**
//     * This method deletes the start endpoint from an app route.
//     *
//     * @param routeId    id of the app route
//     * @param endpointId id of the endpoint
//     * @return true, if endpoint is deleted
//     */
//    public boolean deleteAppRouteStart(URI routeId, URI endpointId) {
//
//        boolean deleted = false;
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//            deleted = appRouteImpl.getAppRouteStart().removeIf(endpoint -> endpoint.getId().equals(endpointId));
//            configModelService.saveState();
//        }
//        return deleted;
//    }
//
//    /**
//     * This method deletes the start endpoint from a subroute.
//     *
//     * @param routeId     id of the app route
//     * @param routeStepId id of the subroute
//     * @param endpointId  id of the endpoint
//     * @return true, if start endpoint is deleted from the subroute
//     */
//    public boolean deleteSubrouteStart(URI routeId, URI routeStepId, URI endpointId) {
//
//        boolean deleted = false;
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//            var routeStepImpl = getSubrouteImpl(routeStepId, appRouteImpl);
//
//            if (routeStepImpl != null) {
//                deleted = routeStepImpl.getAppRouteStart().removeIf(endpoint -> endpoint.getId().equals(endpointId));
//                configModelService.saveState();
//            }
//        }
//        return deleted;
//    }
//
//    /**
//     * This method updates the start endpoint from an app route.
//     *
//     * @param routeId    id of the app route
//     * @param endpointId id of the endpoint
//     * @param accessUrl  access url from the endpoint
//     * @param username   username for the authentication
//     * @param password   password for the authentication
//     * @return true, if start endpoint is updated
//     */
//    public boolean updateAppRouteStart(URI routeId, URI endpointId, String accessUrl, String username, String password) {
//
//        boolean updated = false;
//
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        updated = isUpdated(endpointId, accessUrl, username, password, updated, appRouteImpl);
//        configModelService.saveState();
//        return updated;
//
//    }
//
//    /**
//     * This method updates the start endpoint from a subroute
//     *
//     * @param routeId     id of the app route
//     * @param routeStepId id of the subroute
//     * @param endpointId  id of the endpoint
//     * @param accessUrl   access url from endpoint
//     * @param username    username for the authentication
//     * @param password    password for the authentication
//     * @return true, if start endpoint is updated
//     */
//    public boolean updateSubrouteStart(URI routeId, URI routeStepId, URI endpointId,
//                                       String accessUrl, String username, String password) {
//
//        boolean updated = false;
//
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//
//            var routeStepImpl = getSubrouteImpl(routeStepId, appRouteImpl);
//
//            updated = isUpdated(endpointId, accessUrl, username, password, updated, routeStepImpl);
//        }
//        configModelService.saveState();
//        return updated;
//    }
//
//    /**
//     * This method creates a subroute for the app route.
//     *
//     * @param routeId           id of the app route
//     * @param routeDeployMethod route deploy method of the subroute
//     * @return subroute
//     */
//    public RouteStep createSubroute(URI routeId, String routeDeployMethod) {
//
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//
//            if (appRouteImpl.getHasSubRoute() == null) {
//                appRouteImpl.setHasSubRoute(new ArrayList<>());
//            }
//            ArrayList<RouteStep> routeSteps = (ArrayList<RouteStep>) appRouteImpl.getHasSubRoute();
//            RouteStep routeStep = new RouteStepBuilder()._routeDeployMethod_(routeDeployMethod).build();
//
//            /*
//             * Sets automatically the start endpoint of the subroute.
//             * It distinguishes if it is the first subroute or not.
//             * The first subroute gets as start endpoint the start endpoint of the app route if it is set.
//             * All other subroutes get as start endpoint the end point of the previous subroute if it is set.
//             */
//            if (routeSteps.size() >= 1) {
//                RouteStep previous = appRouteImpl.getHasSubRoute().get(appRouteImpl.getHasSubRoute().size() - 1);
//
//                if (previous != null) {
//                    if (previous.getAppRouteEnd() != null) {
//                        Endpoint customEndpoint = new AppEndpointBuilder()
//                                ._accessURL_(previous.getAppRouteEnd().get(0).getAccessURL())
//                                ._appEndpointType_(AppEndpointType.OUTPUT_ENDPOINT).build();
//                        var routeStepImpl = (RouteStepImpl) routeStep;
//                        routeStepImpl.setAppRouteStart(Util.asList(customEndpoint));
//                    }
//                    routeSteps.add(routeStep);
//                    appRouteImpl.setHasSubRoute(routeSteps);
//                    configModelService.saveState();
//                    return routeStep;
//                }
//            } else {
//                var routeStepImpl = (RouteStepImpl) routeStep;
//
//                if (appRouteImpl.getAppRouteStart() != null) {
//                    routeStepImpl.setAppRouteStart(Util.asList(appRouteImpl.getAppRouteStart().get(0)));
//                }
//                routeSteps.add(routeStepImpl);
//                appRouteImpl.setHasSubRoute(routeSteps);
//                configModelService.saveState();
//                return routeStepImpl;
//            }
//        }
//        return null;
//    }
//
//    /**
//     * This method updates the subroute with the given parameters.
//     *
//     * @param routeId           id of the app route
//     * @param routeStepId       id of the subroute
//     * @param routeDeployMethod route deploy method of the subroute
//     * @return true, if subroute is updated
//     */
//    public boolean updateSubroute(URI routeId, URI routeStepId, String routeDeployMethod) {
//
//        boolean updated = false;
//
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//
//            var routeStepImpl = getSubrouteImpl(routeStepId, appRouteImpl);
//
//            if (routeStepImpl != null) {
//                if (routeDeployMethod != null) {
//                    routeStepImpl.setRouteDeployMethod(routeDeployMethod);
//                    configModelService.saveState();
//                    updated = true;
//                }
//            }
//        }
//        return updated;
//    }
//

    /**
     * This method returns the specific soubroute.
     *
     * @param routeId     id of the app route
     * @param routeStepId id of the subroute
     * @return subroute
     */
    public RouteStep getSubroute(URI routeId, URI routeStepId) {

        var appRouteImpl = getAppRouteImpl(routeId);

        if (appRouteImpl != null) {

            return getSubrouteImpl(routeStepId, appRouteImpl);
        }
        return null;
    }
//
//    /**
//     * This method deletes a subroute.
//     *
//     * @param routeId     id of the app route
//     * @param routeStepId id of the subroute
//     * @return true, if subroute is deleted
//     */
//    public boolean deleteSubroute(URI routeId, URI routeStepId) {
//
//        boolean deleted = false;
//
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//            deleted = appRouteImpl.getHasSubRoute().removeIf(routeStep -> routeStep.getId().equals(routeStepId));
//            configModelService.saveState();
//        }
//        return deleted;
//
//    }
//
//    /**
//     * This method creates an endpoint end for an app route
//     *
//     * @param routeId   id of the app route
//     * @param accessUrl access url from the endpoint
//     * @return endpoint
//     */
//    public Endpoint createAppRouteEnd(URI routeId, String accessUrl) {
//
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//            if (appRouteImpl.getAppRouteEnd() == null) {
//                appRouteImpl.setAppRouteEnd(new ArrayList<>());
//            }
//            ArrayList<Endpoint> endpoints = (ArrayList<Endpoint>) appRouteImpl.getAppRouteEnd();
//            Endpoint endpoint = new EndpointBuilder()._accessURL_(URI.create(accessUrl)).build();
//            endpoints.add(endpoint);
//
//            appRouteImpl.setAppRouteEnd(endpoints);
//            configModelService.saveState();
//            return endpoint;
//        }
//        return null;
//    }
//
//    /**
//     * This method creates an endpoint end for the subroute
//     *
//     * @param routeId     id of the app route
//     * @param routeStepId id of the subroute
//     * @param accessUrl   access url from the endpoint
//     * @return endpoint
//     */
//    public Endpoint createSubrouteEnd(URI routeId, URI routeStepId, String accessUrl) {
//
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//
//            var routeStepImpl = getSubrouteImpl(routeStepId, appRouteImpl);
//
//            if (routeStepImpl != null) {
//                if (routeStepImpl.getAppRouteEnd() == null) {
//                    routeStepImpl.setAppRouteEnd(new ArrayList<>());
//                }
//                ArrayList<Endpoint> endpoints = (ArrayList<Endpoint>) routeStepImpl.getAppRouteEnd();
//
//                Endpoint endpoint;
//                if (URI.create(accessUrl).equals(appRouteImpl.getAppRouteEnd().get(0).getAccessURL())) {
//                    endpoint = new ConnectorEndpointBuilder()._accessURL_(URI.create(accessUrl)).build();
//                    // Set endpoint in connector description
//                    BaseConnectorImpl baseConnector = (BaseConnectorImpl) configModelService.getConfigModel().getConnectorDescription();
//                    if (baseConnector != null) {
//                        if (baseConnector.getHasEndpoint() == null) {
//                            baseConnector.setHasEndpoint(Util.asList((ConnectorEndpoint) endpoint));
//                        }
//                        if (baseConnector.getHasEndpoint()
//                                .stream()
//                                .map(Endpoint::getAccessURL)
//                                .noneMatch(uri -> uri.equals(URI.create(accessUrl)))) {
//                            ArrayList<ConnectorEndpoint> connectorEndpoints = (ArrayList<ConnectorEndpoint>) baseConnector.getHasEndpoint();
//                            connectorEndpoints.add((ConnectorEndpoint) endpoint);
//                        }
//                    }
//                } else {
//                    endpoint = new AppEndpointBuilder()._accessURL_(URI.create(accessUrl))
//                            ._appEndpointType_(AppEndpointType.OUTPUT_ENDPOINT).build();
//                }
//                endpoints.add(endpoint);
//                routeStepImpl.setAppRouteEnd(endpoints);
//                configModelService.saveState();
//                return endpoint;
//            }
//        }
//        return null;
//    }
//
//    /**
//     * This method returns the endpoint end from an app route.
//     *
//     * @param routeId    id of the app route
//     * @param endpointId id of the endpoint
//     * @return endpoint
//     */
//    public Endpoint getAppRouteEnd(URI routeId, URI endpointId) {
//
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//            return appRouteImpl.getAppRouteEnd().stream().filter(endpoint -> endpoint.getId().equals(endpointId))
//                    .findAny().orElse(null);
//        }
//        return null;
//    }
//
//    /**
//     * This method returns the endpoint end from an subroute.
//     *
//     * @param routeId     id of the app route
//     * @param routeStepId id id of the subroute
//     * @param endpointId  id id of the endpoint
//     * @return endpoint
//     */
//    public Endpoint getSubrouteEnd(URI routeId, URI routeStepId, URI endpointId) {
//
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//
//            var routeStepImpl = getSubrouteImpl(routeStepId, appRouteImpl);
//
//            if (routeStepImpl != null) {
//                return routeStepImpl.getAppRouteEnd().stream().filter(endpoint -> endpoint.getId().equals(endpointId))
//                        .findAny().orElse(null);
//            }
//        }
//        return null;
//    }
//
//    /**
//     * This method deletes the endpoint end from an app route.
//     *
//     * @param routeId    id of the app route
//     * @param endpointId id of the endpoint
//     * @return true, if endpoint is deleted
//     */
//    public boolean deleteAppRouteEnd(URI routeId, URI endpointId) {
//
//        boolean deleted = false;
//
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//            deleted = appRouteImpl.getAppRouteEnd().removeIf(endpoint -> endpoint.getId().equals(endpointId));
//            configModelService.saveState();
//        }
//        return deleted;
//    }
//
//    /**
//     * This method deletes the endpoint end from an subroute.
//     *
//     * @param routeId     id of the app route
//     * @param routeStepId id of the subroute
//     * @param endpointId  id of the endpoint
//     * @return true, if endpoint is deleted from the subroute
//     */
//    public boolean deleteSubrouteEnd(URI routeId, URI routeStepId, URI endpointId) {
//
//        boolean deleted = false;
//
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//
//            var routeStepImpl = getSubrouteImpl(routeStepId, appRouteImpl);
//
//            if (routeStepImpl != null) {
//                deleted = routeStepImpl.getAppRouteEnd().removeIf(endpoint -> endpoint.getId().equals(endpointId));
//                configModelService.saveState();
//            }
//        }
//        return deleted;
//    }
//
//    /**
//     * This method updates the endpoint end from an app route.
//     *
//     * @param routeId    id of the app route
//     * @param endpointId id of the endpoint
//     * @param accessUrl  access url from endpoint
//     * @return true, if endpoint is updated
//     */
//    public boolean updateAppRouteEnd(URI routeId, URI endpointId, String accessUrl) {
//
//        boolean updated = false;
//
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//
//            var endPointImpl = (EndpointImpl) appRouteImpl.getAppRouteEnd().stream()
//                    .filter(endpoint -> endpoint.getId().equals(endpointId)).findAny().orElse(null);
//
//            if (endPointImpl != null) {
//                if (accessUrl != null) {
//                    endPointImpl.setAccessURL(URI.create(accessUrl));
//                    configModelService.saveState();
//                    updated = true;
//                }
//            }
//        }
//        return updated;
//    }
//
//    /**
//     * This method updates the endpoint end from a subroute.
//     *
//     * @param routeId     id of the app route
//     * @param routeStepId id of the subroute
//     * @param endpointId  id of the endpoint
//     * @param accessUrl   access url from an endpoint
//     * @return true, if endpoint is updated
//     */
//    public boolean updateSubrouteEnd(URI routeId, URI routeStepId, URI endpointId, String accessUrl) {
//
//        boolean updated = false;
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//
//            var routeStepImpl = getSubrouteImpl(routeStepId, appRouteImpl);
//
//            if (routeStepImpl != null) {
//                var endPointImpl = (EndpointImpl) routeStepImpl.getAppRouteEnd().stream()
//                        .filter(endpoint -> endpoint.getId().equals(endpointId)).findAny().orElse(null);
//
//                if (endPointImpl != null) {
//                    if (accessUrl != null) {
//                        endPointImpl.setAccessURL(URI.create(accessUrl));
//                        updated = true;
//                        configModelService.saveState();
//                    }
//                }
//            }
//        }
//        return updated;
//    }
//
//    /**
//     * This method returns the resource from an app route output.
//     *
//     * @param routeId    id of the app route
//     * @param resourceId id of the resource
//     * @return resource
//     */
//    public Resource getResourceFromAppRouteOutput(URI routeId, URI resourceId) {
//
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//            return appRouteImpl.getAppRouteOutput().stream()
//                    .filter(resource -> resource.getId().equals(resourceId)).findAny().orElse(null);
//        }
//        return null;
//    }
//
//    /**
//     * This method returns the resource from a subroute.
//     *
//     * @param routeId     id of the route
//     * @param routeStepId id of the subroute
//     * @param resourceId  id of the resource
//     * @return resource
//     */
//    public Resource getResourceFromSubrouteOutput(URI routeId, URI routeStepId, URI resourceId) {
//
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//
//            var routeStepImpl = getSubrouteImpl(routeStepId, appRouteImpl);
//
//            if (routeStepImpl != null) {
//                return routeStepImpl.getAppRouteOutput().stream()
//                        .filter(resource -> resource.getId().equals(resourceId)).findAny().orElse(null);
//            }
//        }
//        return null;
//    }
//
//    /**
//     * This method deletes a resource from an app route output.
//     *
//     * @param routeId    id of the app route
//     * @param resourceId id of the resource
//     * @return true, if resource is deleted
//     */
//    public boolean deleteResourceFromAppRouteOutput(URI routeId, URI resourceId) {
//
//        boolean deleted = false;
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//            deleted = appRouteImpl.getAppRouteOutput().removeIf(resource -> resource.getId().equals(resourceId));
//            configModelService.saveState();
//        }
//        return deleted;
//
//    }
//
//    /**
//     * This method deletes a resource from a subroute.
//     *
//     * @param routeId     id of the app route
//     * @param routeStepId id of the subroute
//     * @param resourceId  id of the resource
//     * @return true, if resource is updated
//     */
//    public boolean deleteResourceFromSubrouteOutput(URI routeId, URI routeStepId, URI resourceId) {
//
//        boolean deleted = false;
//        var appRouteImpl = getAppRouteImpl(routeId);
//
//        if (appRouteImpl != null) {
//
//            var routeStepImpl = getSubrouteImpl(routeStepId, appRouteImpl);
//
//            if (routeStepImpl != null) {
//                deleted = routeStepImpl.getAppRouteOutput().removeIf(resource -> resource.getId().equals(resourceId));
//                configModelService.saveState();
//            }
//        }
//        return deleted;
//    }

    /**
     * This method tries to update the endpoint with the given parameters.
     *
     * @param endpointId   id of the endpoint
     * @param accessUrl    access url from an endpoint
     * @param username     username for the authentication
     * @param password     password for the authentication
     * @param updated      boolean
     * @param appRouteImpl app route implementation
     * @return true, if endpoint is updated
     */
    private boolean isUpdated(URI endpointId, String accessUrl, String username, String password,
                              boolean updated, AppRoute appRouteImpl) {
        if (appRouteImpl != null) {

            var endpointImpl = (GenericEndpointImpl) appRouteImpl.getAppRouteStart().stream()
                    .filter(endpoint -> endpoint.getId().equals(endpointId)).findAny().orElse(null);

            if (endpointImpl != null) {
                BasicAuthentication basicAuthentication = endpointImpl.getGenericEndpointAuthentication();
                if (accessUrl != null) {
                    endpointImpl.setAccessURL(URI.create(accessUrl));
                    updated = true;
                }
                if (username != null) {
                    endpointImpl.setGenericEndpointAuthentication(
                            new BasicAuthenticationBuilder(basicAuthentication.getId())
                                    ._authPassword_(basicAuthentication.getAuthPassword())
                                    ._authUsername_(username).build());
                    updated = true;
                }
                if (password != null) {
                    endpointImpl.setGenericEndpointAuthentication(
                            new BasicAuthenticationBuilder(basicAuthentication.getId())
                                    ._authUsername_(basicAuthentication.getAuthUsername())
                                    ._authPassword_(password).build());
                    updated = true;
                }
            }
        }
        return updated;
    }

    /**
     * This method returns a specific app route with the given parameter.
     *
     * @param routeId id of the route
     * @return app route implementation
     */
    private AppRouteImpl getAppRouteImpl(URI routeId) {
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
    private RouteStepImpl getSubrouteImpl(URI routeStepId, AppRouteImpl appRouteImpl) {
        return (RouteStepImpl) appRouteImpl.getHasSubRoute().stream()
                .filter(routeStep -> routeStep.getId().equals(routeStepId)).findAny().orElse(null);
    }

    public RouteStep createAppRouteStep(URI routeId, URI startId, int startCoordinateX, int startCoordinateY,
                                        URI endID, int endCoordinateX, int endCoordinateY, URI resourceId) {

        // Create and save the endpoints of the route with the respective coordinates
        EndpointInformation startEndpointInformation =
                new EndpointInformation(routeId.toString(), startId.toString(), startCoordinateX, startCoordinateY);

        EndpointInformation endEndpointInformation =
                new EndpointInformation(routeId.toString(), endID.toString(), endCoordinateX, endCoordinateY);

        endpointInformationRepository.save(startEndpointInformation);
        endpointInformationRepository.save(endEndpointInformation);

        var appRouteImpl = getAppRouteImpl(routeId);
        if (appRouteImpl != null) {

            if (appRouteImpl.getHasSubRoute() == null) {
                appRouteImpl.setHasSubRoute(new ArrayList<>());
            }
            ArrayList<RouteStep> routeSteps = (ArrayList<RouteStep>) appRouteImpl.getHasSubRoute();
            // Determine endpoints
            Endpoint startEndpoint = getEndpoint(startId);
            Endpoint endpoint = getEndpoint(endID);

            // Get route deploy method for route step
            List<RouteDeployMethod> routeDeployMethod = routeDeployMethodRepository.findAll();
            String deployMethod;
            if (routeDeployMethod.isEmpty()) {
                deployMethod = "custom";
            } else {
                deployMethod = routeDeployMethod.get(0).getDeployMethod().toString();
            }

            // Create route step
            if (startEndpoint != null && endpoint != null) {
                Resource resource = resourceService.getResource(resourceId);
                RouteStep routeStep;
                if (resource != null) {
                    routeStep = new RouteStepBuilder()._routeDeployMethod_(deployMethod)
                            ._appRouteStart_(Util.asList(startEndpoint))
                            ._appRouteEnd_(Util.asList(endpoint))
                            ._appRouteOutput_(Util.asList(resource))
                            .build();
                } else {
                    logger.info("Subroute is created without Resource!!!");
                    routeStep = new RouteStepBuilder()._routeDeployMethod_(deployMethod)
                            ._appRouteStart_(Util.asList(startEndpoint))
                            ._appRouteEnd_(Util.asList(endpoint))
                            .build();
                }
                routeSteps.add(routeStep);
                configModelService.saveState();
                return routeStep;
            }
        }
        return null;
    }

    private Endpoint getEndpoint(URI endpointId) {

        // Search endpoint in the app repository
        List<CustomApp> customAppList = customAppRepository.findAll();
        if (customAppList.size() != 0) {
            var customApp = customAppList.stream()
                    .map(CustomApp::getAppEndpointList)
                    .flatMap(Collection::stream)
                    .filter(customAppEndpoint -> customAppEndpoint.getEndpoint().getId().equals(endpointId))
                    .findAny().orElse(null);
            if (customApp != null) {
                return customApp.getEndpoint();
            }
        }

        // Search endpoint in the backend repository
        if (backendConnectionService.getBackendConnections() != null) {
            return backendConnectionService.getBackendConnection(endpointId);
        }
        logger.info("No endpoint found in app repo and backend repo!!!");
        return null;
    }

    public EndpointInformation getEndpointInformation(URI routeId, URI endpointId) {
        List<EndpointInformation> endpointInformations = endpointInformationRepository.findAll();
        if (endpointInformations.size() != 0) {
            for (EndpointInformation endpointInformation : endpointInformations) {
                if (routeId.toString().equals(endpointInformation.getRouteId()) &&
                        endpointId.toString().equals(endpointInformation.getEndpointId())) {
                    return endpointInformation;
                }
            }
        }
        return null;

    }

    public String validateAppRoute(URI routeId) {

        String validationMessage = "";
        var appRouteImpl = getAppRouteImpl(routeId);
        if (configModelService.getConfigModel().getAppRoute() == null || appRouteImpl == null
                || appRouteImpl.getHasSubRoute() == null) {
            validationMessage = "Validation failed! Could not find any app route or sub routes to validate";
        }
        if (configModelService.getConfigModel().getAppRoute().size() == 1) {
            boolean validation = checkRouteFromGenericToIDSEndpoint(appRouteImpl) ||
                    checkRouteFromIDSToGenericEndpoint(appRouteImpl);
            if (validation) {
                validationMessage = "Validation successful! Route is complete and correct";
            } else {
                validationMessage = "Validation failed! Route is not complete and correct";
            }
        }
        if (configModelService.getConfigModel().getAppRoute().size() > 1) {

            for (AppRoute appRoute : configModelService.getConfigModel().getAppRoute()) {
                boolean validation = checkRouteFromGenericToIDSEndpoint(appRouteImpl) ||
                        checkRouteFromIDSToGenericEndpoint(appRouteImpl);
                if (validation) {
                    validationMessage = "Validation successful! Routes are complete and correct";
                } else {
                    validationMessage = "Validation failed! Routes are not complete and correct";
                }
            }

        }
        return validationMessage;

    }

    private boolean checkRouteFromGenericToIDSEndpoint(AppRouteImpl appRouteImpl) {
        if (appRouteImpl.getHasSubRoute() != null) {
            ArrayList<RouteStep> routeSteps = (ArrayList<RouteStep>) appRouteImpl.getHasSubRoute();

            if (routeSteps.get(0).getAppRouteStart().get(0).getClass() == GenericEndpoint.class &&
                    routeSteps.get(routeSteps.size() - 1).getAppRouteStart().get(0).getClass() == ConnectorEndpoint.class) {

                return checkEndpointLinkage(routeSteps);
            }
        }
        return false;
    }

    private boolean checkRouteFromIDSToGenericEndpoint(AppRouteImpl appRouteImpl) {

        if (appRouteImpl.getHasSubRoute() != null) {
            ArrayList<RouteStep> routeSteps = (ArrayList<RouteStep>) appRouteImpl.getHasSubRoute();

            if (routeSteps.get(0).getAppRouteStart().get(0).getClass() == ConnectorEndpoint.class &&
                    routeSteps.get(routeSteps.size() - 1).getAppRouteStart().get(0).getClass() == GenericEndpoint.class) {

                return checkEndpointLinkage(routeSteps);
            }
        }
        return false;
    }

    private boolean checkEndpointLinkage(ArrayList<RouteStep> routeSteps) {
        for (int i = 0; i < routeSteps.size() - 1; i++) {
            if (!routeSteps.get(i).getAppRouteStart().get(0).getAccessURL().equals(
                    routeSteps.get(i + 1).getAppRouteEnd().get(0).getAccessURL())) {
                return false;
            }
        }
        return true;
    }
}
