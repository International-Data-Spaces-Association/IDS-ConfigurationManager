package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.EndpointInformationRepository;
import de.fraunhofer.isst.configmanager.configmanagement.entities.endpointInfo.EndpointInformation;
import de.fraunhofer.isst.configmanager.util.CalenderUtil;
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
public class ResourceService {

    private transient final ConfigModelService configModelService;
    private transient final EndpointService endpointService;
    private transient final EndpointInformationRepository endpointInformationRepository;
    private transient final DefaultConnectorClient client;

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
    public void updateResourceContent(String title, String description, String language, List<String> keywords,
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
    public List<Resource> getResources() {
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
     * @return true, if resource contract is updated
     */
    //TODO recursively update in all subroutes
    public void updateResourceContractInAppRoute(URI resourceId, ContractOffer contractOffer) {
        // Update resource representation in app route
        if (configModelService.getConfigModel().getAppRoute() == null) {
            log.info("---- Could not find any app route");
        } else {
            for (AppRoute appRoute : configModelService.getConfigModel().getAppRoute()) {
                if (appRoute.getHasSubRoute() != null) {
                    for (RouteStep routeStep : appRoute.getHasSubRoute()) {
                        if (routeStep.getAppRouteOutput() != null) {
                            for (Resource resource : routeStep.getAppRouteOutput()) {
                                if (resourceId.equals(resource.getId())) {
                                    var resourceImpl = (ResourceImpl) resource;
                                    resourceImpl.setContractOffer(Util.asList(contractOffer));
                                    log.info("---- Updated resource representation in the app route");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return;
    }

    /**
     * This method returns from a resource the contract offer
     *
     * @param resourceId id of the resource
     * @return contract offer
     */
    public ContractOffer getResourceContract(URI resourceId) {
        for (Resource resource : getResources()){
            if(resourceId.equals(resource.getId())){
                if(resource.getContractOffer().get(0) != null) {
                    return resource.getContractOffer().get(0);
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
        return (RepresentationImpl) getResources()
                .stream()
                .map(DigitalContent::getRepresentation)
                .flatMap(Collection::stream)
                .filter(representation -> representation.getId().equals(representationId))
                .findAny()
                .orElse(null);
    }

    /**
     * @param resourceId id of the resource
     * @param representationId id of the representation to delete
     * @return true, if resource is deleted
     */
    public void deleteResourceRepresentationFromAppRoute(URI resourceId, URI representationId) {
        if (configModelService.getConfigModel().getAppRoute() == null) {
            log.info("---- Could not find any app route to delete the resource");
        } else {
            for(var route : configModelService.getConfigModel().getAppRoute()){
                if(route == null) continue;
                if(route.getAppRouteOutput() != null){
                    for(var resource : route.getAppRouteOutput()){
                        if(resource.getRepresentation() != null)
                            resource.getRepresentation().removeIf(representation ->
                                    representation.getId().equals(representationId)
                            );
                    }
                }
                if(route.getHasSubRoute() == null) continue;
                for(var subRoute : route.getHasSubRoute()){
                    deleteRepresentationFromSubRoutes(subRoute, new ArrayList<>(), resourceId, representationId);
                }
            }
        }
        configModelService.saveState();
        return;
    }

    /**
     * Delete occurrence of a resource representation with resourceID and representationID from all SubRoutes
     *
     * @param current current Node in AppRoute
     * @param visited already visited AppRoutes
     * @param resourceId ID of the Resource for which the representation should be deleted
     * @param representationId ID of the Representation to delete
     */
    private void deleteRepresentationFromSubRoutes(RouteStep current, List<RouteStep> visited, URI resourceId, URI representationId){
        if(current == null) return;
        if (current.getAppRouteOutput() != null) {
            for(var resource : current.getAppRouteOutput()){
                if(resource.getRepresentation() != null)
                    resource.getRepresentation().removeIf(representation ->
                            representation.getId().equals(representationId)
                    );
            }
        }
        if(current.getHasSubRoute() == null) return;
        for(var subRoute : current.getHasSubRoute()){
            if(!visited.contains(subRoute)){
                visited.add(current);
                deleteFromSubRoutes(subRoute, visited, resourceId);
            }
        }
    }

    /**
     * @param resourceId id of the resource
     * @return true, if resource is deleted
     */
    public void deleteResourceFromAppRoute(URI resourceId) {
        if (configModelService.getConfigModel().getAppRoute() == null) {
            log.info("---- Could not find any app route to delete the resource");
        } else {
            for(var route : configModelService.getConfigModel().getAppRoute()){
                if(route == null) continue;
                if(route.getAppRouteOutput() != null) route.getAppRouteOutput().removeIf(resource -> resource.getId().equals(resourceId));
                if(route.getHasSubRoute() == null) continue;
                for(var subRoute : route.getHasSubRoute()){
                    deleteFromSubRoutes(subRoute, new ArrayList<>(), resourceId);
                }
            }
        }
        configModelService.saveState();
        return;
    }

    /**
     * Delete occurrence of a resource with resourceID from all SubRoutes
     *
     * @param current current Node in AppRoute
     * @param visited already visited AppRoutes
     * @param resourceId ID of the Resource to delete
     */
    private void deleteFromSubRoutes(RouteStep current, List<RouteStep> visited, URI resourceId){
        if(current == null) return;
        if (current.getAppRouteOutput() != null) current.getAppRouteOutput().removeIf(resource -> resource.getId().equals(resourceId));
        if(current.getHasSubRoute() == null) return;
        for(var subRoute : current.getHasSubRoute()){
            if(!visited.contains(subRoute)){
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
    public ResourceImpl createResource(String title, String description, String language, List<String> keywords,
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

    public ResourceImpl updateResource(URI resourceId, String title, String description, String language,
                                       List<String> keywords, String version, String standardlicense, String publisher) {
        //Get a Resource and update if it exists
        for (Resource resource : getResources()){
            if(resource.getId().equals(resourceId)){
                ResourceImpl resImpl = (ResourceImpl) resource;
                updateResourceContent(title, description, language, keywords, version, standardlicense,
                        publisher, resImpl);
                return resImpl;
            }
        }
        return null;
    }
    /**
     * @param newResource new Resource old version should be replaced with
     * @return resource implementation
     */
    //TODO update recursively in all SubRoutes
    public void updateResourceInAppRoute(ResourceImpl newResource) {
        // Update the resource in the app route
        if (configModelService.getConfigModel().getAppRoute() == null) {
            log.info("---- Could not find any app route to update the resource");
        } else {
            for (AppRoute appRoute : configModelService.getConfigModel().getAppRoute()) {
                if (appRoute.getHasSubRoute() != null) {
                    for (RouteStep routeStep : appRoute.getHasSubRoute()) {
                        if (routeStep.getAppRouteOutput() != null) {
                            for (Resource resource : routeStep.getAppRouteOutput()) {
                                if (newResource.getId().equals(resource.getId())) {
                                    ArrayList<Resource> output = (ArrayList<Resource>) routeStep.getAppRouteOutput();
                                    output.remove(resource);
                                    output.add(newResource);
                                }
                            }
                        }
                    }
                }
            }
        }
        return;
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
    //TODO search in all SubRoutes
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
     * @return resources
     */
    public List<Resource> getRequestedResources() {
        ArrayList<Resource> resources = new ArrayList<>();

        BaseConnector baseConnector = null;
        try {
            baseConnector = client.getSelfDeclaration();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        if (baseConnector != null && baseConnector.getResourceCatalog() != null) {
            for (ResourceCatalog resourceCatalog : baseConnector.getResourceCatalog()) {
                if (resourceCatalog.getRequestedResource() != null) {
                    resources.addAll(resourceCatalog.getRequestedResource());
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
}
