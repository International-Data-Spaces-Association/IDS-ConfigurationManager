package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.EndpointInformationRepository;
import de.fraunhofer.isst.configmanager.configmanagement.entities.endpointInfo.EndpointInformation;
import de.fraunhofer.isst.configmanager.util.CalenderUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ResourceService {

    private final ConfigModelService configModelService;
    private final EndpointService endpointService;
    private final EndpointInformationRepository endpointInformationRepository;
    private final DefaultConnectorClient client;

    @Autowired
    public ResourceService(ConfigModelService configModelService, EndpointService endpointService,
                           EndpointInformationRepository endpointInformationRepository,
                           DefaultConnectorClient client) {
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
    public Resource getResource(URI resourceId) {
        try {
            return configModelService.getConfigModel().getConnectorDescription().getResourceCatalog().stream()
                    .map(ResourceCatalog::getOfferedResource)
                    .flatMap(Collection::stream)
                    .dropWhile(res -> !res.getId().equals(resourceId))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
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
    public void updateResourceContent(String title, String description, String language, ArrayList<String> keywords,
                                      String version, String standardlicense, String publisher, ResourceImpl resourceImpl) {
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
            ArrayList<TypedLiteral> keys = new ArrayList<>();
            for (String keyword : keywords) {
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
    public ArrayList<Resource> getResources() {
        ArrayList<Resource> resources = new ArrayList<>();

        BaseConnector baseConnector = null;
        try {
            baseConnector = client.getSelfDeclaration();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        if (baseConnector != null && baseConnector.getResourceCatalog() != null) {
            for (ResourceCatalog resourceCatalog : baseConnector.getResourceCatalog()) {
                if (resourceCatalog.getOfferedResource() != null) {
                    resources.addAll(resourceCatalog.getOfferedResource());
                }
            }
        }
//        for (ResourceCatalog resourceCatalog : configModelService.getConfigModel()
//                .getConnectorDescription().getResourceCatalog()) {
//            if (resourceCatalog != null && resourceCatalog.getOfferedResource() != null) {
//                for (Resource resource : resourceCatalog.getOfferedResource()) {
//                    if (resource != null) {
//                        resources.add(resource);
//                    }
//                }
//            }
//        }
        return resources;
    }

    /**
     * This method updates the resource contract with the given parameters
     *
     * @param resourceId    id of the resource
     * @param contractOffer the contract offer which will be updated
     * @return true, if resource contract is updated
     */
    public boolean updateResourceContract(URI resourceId, ContractOffer contractOffer) {
        boolean updated = false;
        // Update resource representation in resource catalog
        for (ResourceCatalog resourceCatalog : configModelService.getConfigModel()
                .getConnectorDescription().getResourceCatalog()) {
            if (resourceCatalog.getOfferedResource() != null) {
                for (Resource resource : resourceCatalog.getOfferedResource()) {
                    if (resourceId.equals(resource.getId())) {
                        var resourceImpl = (ResourceImpl) resource;
                        resourceImpl.setContractOffer(Util.asList(contractOffer));
                        updated = true;
                        log.info("Updated resource representation in the resource catalog");
                        break;
                    }
                }
            }
        }

        // Update resource representation in app route
        if (configModelService.getConfigModel().getAppRoute() == null) {
            log.info("Could not find any app route");
        } else {
            for (AppRoute appRoute : configModelService.getConfigModel().getAppRoute()) {
                if (appRoute.getHasSubRoute() != null) {
                    for (RouteStep routeStep : appRoute.getHasSubRoute()) {
                        if (routeStep.getAppRouteOutput() != null) {
                            for (Resource resource : routeStep.getAppRouteOutput()) {
                                if (resourceId.equals(resource.getId())) {
                                    var resourceImpl = (ResourceImpl) resource;
                                    resourceImpl.setContractOffer(Util.asList(contractOffer));
                                    updated = true;
                                    log.info("Updated resource representation in the app route");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return updated;
    }

    /**
     * This method returns from a resource the contract offer
     *
     * @param resourceId id of the resource
     * @return contract offer
     */
    public ContractOffer getResourceContract(URI resourceId) {
        for (ResourceCatalog resourceCatalog : configModelService.getConfigModel()
                .getConnectorDescription().getResourceCatalog()) {
            if (resourceCatalog.getOfferedResource() != null) {
                for (Resource resource : resourceCatalog.getOfferedResource()) {
                    if (resourceId.equals(resource.getId())) {
                        if (resource.getContractOffer().get(0) != null) {
                            return resource.getContractOffer().get(0);
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param representationId id of the representation
     * @return representation implementation
     */
    public RepresentationImpl getResourceRepresentationInCatalog(URI representationId) {
        return (RepresentationImpl) configModelService.getConfigModel()
                .getConnectorDescription().getResourceCatalog().stream()
                .map(ResourceCatalog::getOfferedResource)
                .flatMap(Collection::stream)
                .map(DigitalContent::getRepresentation)
                .flatMap(Collection::stream)
                .filter(representation -> representation.getId().equals(representationId))
                .findAny()
                .orElse(null);
    }

    /**
     * @param resourceId       id of the resource
     * @param representationId id of the representation
     * @return true, if representation is deleted
     */
    public boolean deleteResourceRepresentation(URI resourceId, URI representationId) {
        var deleted = false;

        // Delete representation in catalog
        for (ResourceCatalog resourceCatalog : configModelService.getConfigModel().getConnectorDescription().getResourceCatalog()) {
            if (resourceCatalog != null) {
                var resource = resourceCatalog.getOfferedResource().stream()
                        .filter(resource1 -> resource1.getId().equals(resourceId)).findAny().orElse(null);
                if (resource != null) {
                    deleted |= resource.getRepresentation()
                            .removeIf(representation -> representation.getId().equals(representationId));
                }
            }
        }

        // Delete representation in app route if exists
        if (configModelService.getConfigModel().getAppRoute() == null) {
            log.info("No app route found to delete the resource representation");
        } else {
            Resource foundresource = null;
            for (AppRoute appRoute : configModelService.getConfigModel().getAppRoute()) {
                if (appRoute.getHasSubRoute() != null) {
                    for (RouteStep routeStep : appRoute.getHasSubRoute()) {
                        if (routeStep.getAppRouteOutput() != null) {
                            for (Resource resource : routeStep.getAppRouteOutput()) {
                                if (resourceId.equals(resource.getId())) {
                                    foundresource = resource;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (foundresource != null) {
                deleted |= foundresource.getRepresentation()
                        .removeIf(representation -> representation.getId().equals(representationId));
            }

        }
        return deleted;
    }

    /**
     * @param resourceId id of the resource
     * @return true, if resource is deleted
     */
    public boolean deleteResource(URI resourceId) {

        boolean deleted = configModelService.getConfigModel().getConnectorDescription().getResourceCatalog()
                .stream()
                .map(ResourceCatalog::getOfferedResource)
                .map(resources -> resources.removeIf(resource -> resource.getId().equals(resourceId)))
                .reduce(false, (a, b) -> a || b);

        if (configModelService.getConfigModel().getAppRoute() == null) {
            log.info("Could not find any app route to delete the resource");
        } else {
            deleted |= configModelService.getConfigModel().getAppRoute().stream()
                    .map(AppRoute::getHasSubRoute)
                    .flatMap(Collection::stream)
                    .map(RouteStep::getAppRouteOutput)
                    .map(resources -> resources != null && resources.removeIf(resource -> resource.getId().equals(resourceId)))
                    .reduce(false, (a, b) -> a || b);
        }
        configModelService.saveState();
        return deleted;
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
    public ResourceImpl createResource(String title, String description, String language, ArrayList<String> keywords,
                                       String version, String standardlicense, String publisher) {

        ArrayList<TypedLiteral> keys = new ArrayList<>();
        for (String keyword : keywords) {
            keys.add(new TypedLiteral(keyword));
        }
        // Create the resource with the given parameters
        Resource resource = new ResourceBuilder()
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
        var resourceImpl = (ResourceImpl) resource;
        return resourceImpl;
    }

    /**
     * @param resourceId      id of the resource
     * @param title           title of the resource
     * @param description     description of the resource
     * @param language        language of the resource
     * @param keywords        keywords for the resource
     * @param version         version of the resource
     * @param standardlicense standard license for the resource
     * @param publisher       the publisher of the resource
     * @return resource implementation
     */
    public ResourceImpl updateResource(URI resourceId, String title, String description, String language,
                                       ArrayList<String> keywords, String version, String standardlicense, String publisher) {
        var resourceImpl = (ResourceImpl) getResources()
                .stream().filter(resource -> resource.getId().equals(resourceId)).findAny().orElse(null);

        // Update the resource with the given parameters
        if (resourceImpl != null) {
            updateResourceContent(title, description, language, keywords, version, standardlicense,
                    publisher, resourceImpl);
        }

//        // Update the resource in the app route
//        if (configModelService.getConfigModel().getAppRoute() == null) {
//            log.info("Could not find any app route to update the resource");
//        } else {
//            ResourceImpl resourceImplApp = null;
//            for (AppRoute appRoute : configModelService.getConfigModel().getAppRoute()) {
//                if (appRoute.getHasSubRoute() != null) {
//                    for (RouteStep routeStep : appRoute.getHasSubRoute()) {
//                        if (routeStep.getAppRouteOutput() != null) {
//                            for (Resource resource : routeStep.getAppRouteOutput()) {
//                                if (resourceId.equals(resource.getId())) {
//                                    resourceImplApp = (ResourceImpl) resource;
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            if (resourceImplApp != null) {
//                updateResourceContent(title, description, language, keywords, version, standardlicense,
//                        publisher, resourceImplApp);
//            }
//        }
        return resourceImpl;
    }

    /**
     * This method updates a backend connection
     *
     * @param resourceId id of the resource
     * @param endpointId id of the endpoint
     */
    public void updateBackendConnection(URI resourceId, URI endpointId) {
        if (configModelService.getConfigModel().getAppRoute() != null) {
            RouteStepImpl foundRouteStep = null;
            AppRouteImpl appRouteImpl = null;
            for (AppRoute appRoute : configModelService.getConfigModel().getAppRoute()) {
                for (RouteStep routeStep : appRoute.getHasSubRoute()) {
                    for (Resource resource : routeStep.getAppRouteOutput()) {
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
                var endpoint = endpointService.getGenericEndpoint(endpointId);
                if (endpoint != null) {
                    appRouteImpl.setAppRouteStart(Util.asList(endpoint));
                    foundRouteStep.setAppRouteStart(Util.asList(endpoint));
                }
            }
        }

        // Set first entry of endpoint informations to the new endpoint
        if (endpointInformationRepository.findAll().size() > 0) {
            var endpointInfo = endpointInformationRepository.findAll().get(0);
            endpointInfo.setEndpointId(endpointId.toString());
            endpointInformationRepository.saveAndFlush(endpointInfo);
        } else {
            var endpointInformation = new EndpointInformation();
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
    public Resource getResourceInAppRoute(URI resourceId) {

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
     * @param baseConnector the base connector
     * @return resources
     */
    public List<Resource> getRequestedResources(BaseConnector baseConnector) {

        List<Resource> resourceList = new ArrayList<>();
        if (baseConnector.getResourceCatalog() != null) {
            for (ResourceCatalog resourceCatalog : baseConnector.getResourceCatalog()) {
                if (resourceCatalog.getRequestedResource() != null) {
                    for (Resource resource : resourceCatalog.getRequestedResource()) {
                        resourceList.add(resource);
                    }
                }
            }
        }
        return resourceList;
    }
}
