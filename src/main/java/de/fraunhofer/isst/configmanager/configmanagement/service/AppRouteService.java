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
    private final EndpointService endpointService;
    private final ResourceService resourceService;

    private final RouteDeployMethodRepository routeDeployMethodRepository;
    private final EndpointInformationRepository endpointInformationRepository;
    private final CustomAppRepository customAppRepository;

    @Autowired
    public AppRouteService(ConfigModelService configModelService,
                           RouteDeployMethodRepository routeDeployMethodRepository,
                           EndpointInformationRepository endpointInformationRepository,
                           CustomAppRepository customAppRepository,
                           EndpointService endpointService,
                           ResourceService resourceService) {
        this.configModelService = configModelService;
        this.routeDeployMethodRepository = routeDeployMethodRepository;
        this.endpointInformationRepository = endpointInformationRepository;
        this.customAppRepository = customAppRepository;
        this.endpointService = endpointService;
        this.resourceService = resourceService;
    }

    /**
     * This method creates an app route
     *
     * @param description description of the app route
     * @return app route
     */
    public AppRoute createAppRoute(String description) {

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
        AppRoute appRoute = new AppRouteBuilder()
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
    public boolean updateAppRoute(URI routeId, String description) {

        boolean updated = false;

        var appRouteImpl = getAppRouteImpl(routeId);

        if (appRouteImpl != null) {
            appRouteImpl.setAppRouteBroker(null);
            appRouteImpl.setAppRouteStart(null);
            appRouteImpl.setAppRouteEnd(null);
            appRouteImpl.setAppRouteOutput(null);
            appRouteImpl.setHasSubRoute(null);
            appRouteImpl.setRouteConfiguration(null);
            if (description != null) {
                appRouteImpl.setRouteDescription(description);
            } else {
                appRouteImpl.setRouteDescription(null);
            }

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

    /**
     * This method deletes an app route.
     *
     * @param routeId id of the app route
     * @return true, if app route is deleted
     */
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
        return (List<AppRoute>) configModelService.getConfigModel().getAppRoute();
    }

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

        RouteStep routeStep = null;
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

            // Set app route start and end
            if (routeSteps.size() == 0) {
                appRouteImpl.setAppRouteStart(Util.asList(startEndpoint));
            }
            appRouteImpl.setAppRouteEnd(Util.asList(endpoint));

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
                if (resource != null) {

                    // Set resource endpoint
                    if (configModelService.getConfigModel().getConnectorDescription().getHasEndpoint() == null
                            || configModelService.getConfigModel().getConnectorDescription().getHasEndpoint().isEmpty()) {

                        var baseConnectorImpl = (BaseConnectorImpl) configModelService.getConfigModel().getConnectorDescription();
                        baseConnectorImpl.setHasEndpoint(Util.asList(new ConnectorEndpointBuilder()
                                ._accessURL_(URI.create("http://api/ids/data")).build()));
                    }
                    var connectorEndpoint = configModelService.getConfigModel().getConnectorDescription()
                            .getHasEndpoint().get(0);
                    var resourceImpl = (ResourceImpl) resource;
                    resourceImpl.setResourceEndpoint(Util.asList(connectorEndpoint));

                    routeStep = new RouteStepBuilder()._routeDeployMethod_(deployMethod)
                            ._appRouteStart_(Util.asList(startEndpoint))
                            ._appRouteEnd_(Util.asList(endpoint))
                            ._appRouteOutput_(Util.asList(resourceImpl))
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
    private Endpoint getEndpoint(URI endpointId) {
        // Search endpoint in the app repository
        List<CustomApp> customAppList = customAppRepository.findAll();
        if (customAppList.size() != 0 && endpointId.toString().contains("appEndpoint")) {
            var customApp = customAppList.stream()
                    .map(CustomApp::getAppEndpointList)
                    .flatMap(Collection::stream)
                    .filter(customAppEndpoint -> customAppEndpoint.getEndpoint().getId().equals(endpointId))
                    .findAny().orElse(null);
            if (customApp != null) {
                return customApp.getEndpoint();
            }
        }
        // Search endpoint in the backend repository and in list of connector endpoints
        if (endpointService.getGenericEndpoints().size() != 0 && endpointId.toString().contains("genericEndpoint")) {
            GenericEndpoint genericEndpoint = endpointService.getGenericEndpoint(endpointId);
            if (genericEndpoint != null) return genericEndpoint;
        }
        if (configModelService.getConfigModel().getConnectorDescription().getHasEndpoint().size() != 0
                && endpointId.toString().contains("connectorEndpoint")) {
            return configModelService.getConfigModel().getConnectorDescription().getHasEndpoint()
                    .stream().filter(connectorEndpoint -> connectorEndpoint.getId().equals(endpointId))
                    .findAny().orElse(null);
        }
        return null;
    }

    /**
     * This method returns an endpoint information.
     *
     * @param routeId    id of the route
     * @param endpointId id of the endpoint
     * @return endpoint information
     */
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
    public boolean deleteAppRouteStep(URI routeId, URI routeStepId) {
        boolean deleted = false;
        var appRouteImpl = getAppRouteImpl(routeId);
        if (appRouteImpl != null) {
            deleted = appRouteImpl.getHasSubRoute().removeIf(routeStep -> routeStep.getId().equals(routeStepId));
            if (deleted) configModelService.saveState();
        }
        return deleted;
    }

    public String validateAppRoute(URI routeId) {
        String validationMessage = "";
        var appRouteImpl = getAppRouteImpl(routeId);
        if (configModelService.getConfigModel().getAppRoute() == null || appRouteImpl == null
                || appRouteImpl.getHasSubRoute() == null) {
            validationMessage = "Validation failed! Could not find any app route or route steps to validate";
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
                var appRouteImp = (AppRouteImpl) appRoute;
                boolean validation = checkRouteFromGenericToIDSEndpoint(appRouteImp) ||
                        checkRouteFromIDSToGenericEndpoint(appRouteImp);
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
            if (routeSteps.get(0).getAppRouteStart().get(0).getClass().getSimpleName().equals("GenericEndpointImpl") &&
                    routeSteps.get(routeSteps.size() - 1).getAppRouteEnd().get(0)
                            .getClass().getSimpleName().equals("ConnectorEndpointImpl")) {

                return checkEndpointLinkage(routeSteps);
            }
        }
        return false;
    }

    private boolean checkRouteFromIDSToGenericEndpoint(AppRouteImpl appRouteImpl) {
        if (appRouteImpl.getHasSubRoute() != null) {
            ArrayList<RouteStep> routeSteps = (ArrayList<RouteStep>) appRouteImpl.getHasSubRoute();

            if (routeSteps.get(0).getAppRouteStart().get(0).getClass().getSimpleName().equals("ConnectorEndpointImpl") &&
                    routeSteps.get(routeSteps.size() - 1).getAppRouteEnd().get(0)
                            .getClass().getSimpleName().equals("GenericEndpointImpl")) {

                return checkEndpointLinkage(routeSteps);
            }
        }
        return false;
    }

    private boolean checkEndpointLinkage(ArrayList<RouteStep> routeSteps) {
        for (int i = 0; i < routeSteps.size() - 1; i++) {
            if (routeSteps.get(i).getAppRouteStart().get(0) == null ||
                    routeSteps.get(i).getAppRouteEnd().get(0) == null) {
                return false;
            }
        }
        return true;
    }
}
