package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.AppRoute;
import de.fraunhofer.iais.eis.AppRouteImpl;
import de.fraunhofer.iais.eis.BaseConnector;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.DigitalContent;
import de.fraunhofer.iais.eis.Language;
import de.fraunhofer.iais.eis.RepresentationImpl;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceBuilder;
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.iais.eis.ResourceImpl;
import de.fraunhofer.iais.eis.RouteStep;
import de.fraunhofer.iais.eis.RouteStepImpl;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configlists.EndpointInformationRepository;
import de.fraunhofer.isst.configmanager.configmanagement.entities.endpointinfo.EndpointInformation;
import de.fraunhofer.isst.configmanager.util.CalenderUtil;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Service class for managing resources.
 */
@Service
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ResourceService {

    transient ConfigModelService configModelService;
    transient EndpointService endpointService;
    transient EndpointInformationRepository endpointInformationRepository;
    transient DefaultConnectorClient client;

    @Autowired
    public ResourceService(final ConfigModelService configModelService,
                           final EndpointService endpointService,
                           final EndpointInformationRepository endpointInformationRepository,
                           final DefaultConnectorClient client) {
        this.configModelService = configModelService;
        this.endpointService = endpointService;
        this.endpointInformationRepository = endpointInformationRepository;
        this.client = client;
    }

    /**
     * Gets the {@link Resource} of a given resource ID.
     *
     * @param resourceId of the resource
     * @return resource
     */
    public Resource getResource(final URI resourceId) {
        try {
            return getResources().stream()
                    .dropWhile(res -> !res.getId().equals(resourceId))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * This method updates the content of the resource with the given parameters.
     *
     * @param title           title of the resource
     * @param description     description of the resource
     * @param language        the language
     * @param keywords        the keywords
     * @param version         the version of the resource
     * @param standardlicense the license of the resource
     * @param publisher       the publisher of the resource
     * @param resourceImpl    the resource implementation class to set the parameters
     */
    public void updateResourceContent(final String title, final String description,
                                      final String language, final List<String> keywords,
                                      final String version, final String standardlicense,
                                      final String publisher, final ResourceImpl resourceImpl) {
        if (title != null) {
            resourceImpl.setTitle(Util.asList(new TypedLiteral(title)));
        }
        if (description != null) {
            resourceImpl.setDescription(Util.asList(new TypedLiteral(description)));
        }
        if (language != null) {
            resourceImpl.setLanguage(Util.asList(Language.valueOf(language)));
        }
        if (keywords != null) {
            final ArrayList<TypedLiteral> keys = new ArrayList<>();
            for (var keyword : keywords) {
                keys.add(new TypedLiteral(keyword));
            }
            resourceImpl.setKeyword(keys);
        }
        if (version != null) {
            resourceImpl.setVersion(version);
        }
        if (standardlicense != null) {
            resourceImpl.setStandardLicense(URI.create(standardlicense));
        }
        if (publisher != null) {
            resourceImpl.setPublisher(URI.create(publisher));
        }
    }

    /**
     * This method returns a list of all resources from the connector.
     *
     * @return list of resources from the connector
     */
    public List<Resource> getResources() {
        final ArrayList<Resource> resources = new ArrayList<>();

        BaseConnector baseConnector = null;
        try {
            baseConnector = client.getSelfDeclaration();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        if (baseConnector != null && baseConnector.getResourceCatalog() != null) {
            for (var resourceCatalog : baseConnector.getResourceCatalog()) {
                if (resourceCatalog.getOfferedResource() != null) {
                    resources.addAll(resourceCatalog.getOfferedResource());
                }
            }
        }
        return resources;
    }

    /**
     * This method returns all offered resources of a connector as plain json String
     *
     * @return list of resources from the connector
     */
    public String getOfferedResourcesAsJsonString() {
        try {
            return client.getOfferedResourcesAsJsonString();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * This method returns all requested resources of a connector as plain json String
     *
     * @return list of resources from the connector
     */
    public String getRequestedResourcesAsJsonString() {
        try {
            return client.getRequestedResourcesAsJsonString();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * This method updates the resource contract with the given parameters
     *
     * @param resourceId    id of the resource
     * @param contractOffer the contract offer which will be updated
     */
    //TODO recursively update in all subroutes
    public void updateResourceContractInAppRoute(final URI resourceId,
                                                 final ContractOffer contractOffer) {
        // Update resource representation in app route
        if (configModelService.getConfigModel().getAppRoute() == null) {
            log.info("---- Could not find any app route");
        } else {
            for (var appRoute : configModelService.getConfigModel().getAppRoute()) {
                if (appRoute.getHasSubRoute() != null) {
                    for (var routeStep : appRoute.getHasSubRoute()) {
                        if (routeStep.getAppRouteOutput() != null) {
                            for (var resource : routeStep.getAppRouteOutput()) {
                                if (resourceId.equals(resource.getId())) {
                                    final var resourceImpl = (ResourceImpl) resource;
                                    resourceImpl.setContractOffer(Util.asList(contractOffer));
                                    log.info("---- Updated resource representation in the app " +
                                            "route");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This method returns from a resource the contract offer
     *
     * @param resourceId id of the resource
     * @return contract offer
     */
    public ContractOffer getResourceContract(final URI resourceId) {
        for (var resource : getResources()) {
            if (resourceId.equals(resource.getId()) && resource.getContractOffer().get(0) != null) {
                return resource.getContractOffer().get(0);
            }
        }
        return null;
    }

    /**
     * @param representationId id of the representation
     * @return representation implementation
     */
    public RepresentationImpl getResourceRepresentationInCatalog(final URI representationId) {
        return (RepresentationImpl) getResources()
                .stream()
                .map(DigitalContent::getRepresentation)
                .flatMap(Collection::stream)
                .filter(representation -> representation.getId().equals(representationId))
                .findAny()
                .orElse(null);
    }

    /**
     * @param resourceId       id of the resource
     * @param representationId id of the representation to delete
     */
    public void deleteResourceRepresentationFromAppRoute(final URI resourceId,
                                                         final URI representationId) {
        if (configModelService.getConfigModel().getAppRoute() == null) {
            log.info("---- Could not find any app route to delete the resource");
        } else {
            for (var route : configModelService.getConfigModel().getAppRoute()) {
                if (route == null) {
                    continue;
                }
                if (route.getAppRouteOutput() != null) {
                    for (var resource : route.getAppRouteOutput()) {
                        if (resource.getRepresentation() != null) {
                            resource.getRepresentation().removeIf(representation ->
                                    representation.getId().equals(representationId)
                            );
                        }
                    }
                }
                if (route.getHasSubRoute() == null) {
                    continue;
                }
                for (var subRoute : route.getHasSubRoute()) {
                    deleteRepresentationFromSubRoutes(subRoute, new ArrayList<>(), resourceId,
                            representationId);
                }
            }
        }
        configModelService.saveState();
    }

    /**
     * Delete occurrence of a resource representation with resourceID and representationID from
     * all SubRoutes
     *
     * @param current          current Node in AppRoute
     * @param visited          already visited AppRoutes
     * @param resourceId       ID of the Resource for which the representation should be deleted
     * @param representationId ID of the Representation to delete
     */
    private void deleteRepresentationFromSubRoutes(final RouteStep current,
                                                   final List<RouteStep> visited,
                                                   final URI resourceId,
                                                   final URI representationId) {
        if (current == null) {
            return;
        }
        if (current.getAppRouteOutput() != null) {
            for (var resource : current.getAppRouteOutput()) {
                if (resource.getRepresentation() != null) {
                    resource.getRepresentation().removeIf(representation ->
                            representation.getId().equals(representationId)
                    );
                }
            }
        }
        if (current.getHasSubRoute() == null) {
            return;
        }
        for (var subRoute : current.getHasSubRoute()) {
            if (!visited.contains(subRoute)) {
                visited.add(current);
                deleteFromSubRoutes(subRoute, visited, resourceId);
            }
        }
    }

    /**
     * @param resourceId id of the resource
     */
    public void deleteResourceFromAppRoute(final URI resourceId) {
        if (configModelService.getConfigModel().getAppRoute() == null) {
            log.info("---- Could not find any app route to delete the resource");
        } else {
            for (var route : configModelService.getConfigModel().getAppRoute()) {
                if (route == null) {
                    continue;
                }
                if (route.getAppRouteOutput() != null) {
                    route.getAppRouteOutput().removeIf(resource -> resource.getId().equals(resourceId));
                }
                if (route.getHasSubRoute() == null) {
                    continue;
                }
                for (var subRoute : route.getHasSubRoute()) {
                    deleteFromSubRoutes(subRoute, new ArrayList<>(), resourceId);
                }
            }
        }
        configModelService.saveState();
    }

    /**
     * Delete occurrence of a resource with resourceID from all SubRoutes
     *
     * @param current    current Node in AppRoute
     * @param visited    already visited AppRoutes
     * @param resourceId ID of the Resource to delete
     */
    private void deleteFromSubRoutes(final RouteStep current, final List<RouteStep> visited,
                                     final URI resourceId) {
        if (current == null) {
            return;
        }
        if (current.getAppRouteOutput() != null) {
            current.getAppRouteOutput().removeIf(resource -> resource.getId().equals(resourceId));
        }
        if (current.getHasSubRoute() == null) {
            return;
        }
        for (var subRoute : current.getHasSubRoute()) {
            if (!visited.contains(subRoute)) {
                visited.add(current);
                deleteFromSubRoutes(subRoute, visited, resourceId);
            }
        }
    }

    /**
     * @param title           title of the resource
     * @param description     description of the resource
     * @param language        language of the resource
     * @param keywords        keywords for the resource
     * @param version         version of the resource
     * @param standardlicense standard license for the resource
     * @param publisher       the publisher of the resource
     * @return resource implementation
     */
    public ResourceImpl createResource(final String title, final String description,
                                       final String language, final List<String> keywords,
                                       final String version, final String standardlicense,
                                       final String publisher) {

        final ArrayList<TypedLiteral> keys = new ArrayList<>();
        for (var keyword : keywords) {
            keys.add(new TypedLiteral(keyword));
        }

        // Create the resource with the given parameters
        return (ResourceImpl) new ResourceBuilder()
                ._title_(Util.asList(new TypedLiteral(title)))
                ._description_(Util.asList(new TypedLiteral(description)))
                ._language_(Util.asList(Language.valueOf(language)))
                ._keyword_(keys)
                ._version_(version)
                ._standardLicense_(URI.create(standardlicense))
                ._publisher_(URI.create(publisher))
                ._created_(CalenderUtil.getGregorianNow())
                ._modified_(CalenderUtil.getGregorianNow())
                .build();
    }

    public ResourceImpl updateResource(final URI resourceId, final String title,
                                       final String description, final String language,
                                       final List<String> keywords, final String version,
                                       final String standardlicense, final String publisher) {
        //Get a Resource and update if it exists
        for (var resource : getResources()) {
            if (resource.getId().equals(resourceId)) {
                final var resImpl = (ResourceImpl) resource;
                updateResourceContent(title, description, language, keywords, version,
                        standardlicense,
                        publisher, resImpl);
                return resImpl;
            }
        }
        return null;
    }

    /**
     * @param newResource new Resource old version should be replaced with
     */
    //TODO update recursively in all SubRoutes
    public void updateResourceInAppRoute(final ResourceImpl newResource) {
        // Update the resource in the app route
        if (configModelService.getConfigModel().getAppRoute() == null) {
            log.info("---- Could not find any app route to update the resource");
        } else {
            for (var appRoute : configModelService.getConfigModel().getAppRoute()) {
                if (appRoute.getHasSubRoute() != null) {
                    for (var routeStep : appRoute.getHasSubRoute()) {
                        if (routeStep.getAppRouteOutput() != null) {
                            for (var resource : routeStep.getAppRouteOutput()) {
                                if (newResource.getId().equals(resource.getId())) {
                                    final ArrayList<Resource> output =
                                            (ArrayList<Resource>) routeStep.getAppRouteOutput();
                                    output.remove(resource);
                                    output.add(newResource);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This method updates a backend connection
     *
     * @param resourceId id of the resource
     * @param endpointId id of the endpoint
     */
    public void updateBackendConnection(final URI resourceId, final URI endpointId) {
        if (configModelService.getConfigModel().getAppRoute() != null) {
            RouteStepImpl foundRouteStep = null;
            AppRouteImpl appRouteImpl = null;
            for (var appRoute : configModelService.getConfigModel().getAppRoute()) {
                for (var routeStep : appRoute.getHasSubRoute()) {
                    for (var resource : routeStep.getAppRouteOutput()) {
                        if (resourceId.equals(resource.getId())) {
                            appRouteImpl = (AppRouteImpl) appRoute;
                            foundRouteStep = (RouteStepImpl) routeStep;
                            break;
                        }
                    }
                }
            }

            // Set app route start and subroute start to the updated endpoint
            if (appRouteImpl != null && foundRouteStep != null) {
                final var endpoint = endpointService.getGenericEndpoint(endpointId);
                if (endpoint != null) {
                    appRouteImpl.setAppRouteStart(Util.asList(endpoint));
                    foundRouteStep.setAppRouteStart(Util.asList(endpoint));
                }
            }
        }

        // Set first entry of endpoint informations to the new endpoint
        if (endpointInformationRepository.findAll().size() > 0) {
            final var endpointInfo = endpointInformationRepository.findAll().get(0);
            endpointInfo.setEndpointId(endpointId.toString());
            endpointInformationRepository.saveAndFlush(endpointInfo);
        } else {
            final var endpointInformation = new EndpointInformation();
            endpointInformation.setEndpointId(endpointId.toString());
            endpointInformationRepository.saveAndFlush(endpointInformation);
        }
    }

    /**
     * This method returns the resource if it is exists in an app route
     *
     * @param resourceId id of the resource
     * @return resource
     */
    //TODO search in all SubRoutes
    public Resource getResourceInAppRoute(final URI resourceId) {

        return configModelService.getConfigModel().getAppRoute().stream()
                .map(AppRoute::getHasSubRoute)
                .flatMap(Collection::stream)
                .map(AppRoute::getAppRouteOutput)
                .flatMap(Collection::stream)
                .filter(resource -> resource.getId().equals(resourceId))
                .findAny().orElse(null);
    }

    /**
     * This method returns a list of requested resources
     *
     * @return resources
     */
    public List<Resource> getRequestedResources() {
        final ArrayList<Resource> resources = new ArrayList<>();

        BaseConnector baseConnector = null;
        try {
            baseConnector = client.getSelfDeclaration();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        if (baseConnector != null && baseConnector.getResourceCatalog() != null) {
            for (var resourceCatalog : baseConnector.getResourceCatalog()) {
                if (resourceCatalog.getRequestedResource() != null) {
                    resources.addAll(resourceCatalog.getRequestedResource());
                }
            }
        }
        return resources;
    }
}
