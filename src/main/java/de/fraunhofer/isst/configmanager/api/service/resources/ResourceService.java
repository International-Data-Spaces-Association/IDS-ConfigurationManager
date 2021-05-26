package de.fraunhofer.isst.configmanager.api.service.resources;

import de.fraunhofer.iais.eis.Language;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceBuilder;
import de.fraunhofer.iais.eis.ResourceImpl;
import de.fraunhofer.iais.eis.RouteStep;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.api.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultResourceClient;
import de.fraunhofer.isst.configmanager.util.CalenderUtil;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing resources.
 */
@Slf4j
@Service
@Transactional
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ResourceService extends AbstractResourceService {

    transient DefaultResourceClient resourceClient;

    @Autowired
    public ResourceService(final ConfigModelService configModelService,
                           final DefaultConnectorClient connectorClient,
                           final DefaultResourceClient resourceClient) {
        super(configModelService, connectorClient);
        this.resourceClient = resourceClient;
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
                                      final String version, final URI standardlicense,
                                      final URI publisher, final ResourceImpl resourceImpl) {
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
            final var literal = new TypedLiteral();
            for (final var keyword : keywords) {
                literal.setValue(keyword);
                keys.add(literal);
            }
            resourceImpl.setKeyword(keys);
        }
        if (version != null) {
            resourceImpl.setVersion(version);
        }
        if (standardlicense != null) {
            resourceImpl.setStandardLicense(standardlicense);
        }
        if (publisher != null) {
            resourceImpl.setPublisher(publisher);
        }
    }

//    /**
//     * This method returns all offered resources of a connector as plain json String.
//     *
//     * @return list of resources from the connector
//     */
//    public String getOfferedResourcesAsJsonString() {
//        try {
//            return resourceClient.getOfferedResourcesAsJsonString();
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//            return null;
//        }
//    }

//    /**
//     * This method returns all requested resources of a connector as plain json String.
//     *
//     * @return list of resources from the connector
//     */
//    public String getRequestedResourcesAsJsonString() {
//        try {
//            return resourceClient.getRequestedResourcesAsJsonString();
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//            return null;
//        }
//    }

    /**
     * @param resourceId id of the resource
     */
    public void deleteResourceFromAppRoute(final URI resourceId) {
        if (configModelService.getConfigModel().getAppRoute() == null) {
            if (log.isInfoEnabled()) {
                log.info("---- [ResourceService deleteResourceFromAppRoute] Could not find any app route to delete the resource");
            }
        } else {
            final ArrayList<RouteStep> emptyList = new ArrayList<>();
            for (final var route : configModelService.getConfigModel().getAppRoute()) {
                if (route == null) {
                    continue;
                }
                if (route.getAppRouteOutput() != null) {
                    route.getAppRouteOutput().removeIf(resource -> resource.getId().equals(resourceId));
                }
                if (route.getHasSubRoute() == null) {
                    continue;
                }

                for (final var subRoute : route.getHasSubRoute()) {
                    deleteFromSubRoutes(subRoute, emptyList, resourceId);
                }
            }
        }
        configModelService.saveState();
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
                                       final String version, final URI standardlicense,
                                       final URI publisher) {

        final ArrayList<TypedLiteral> keys = new ArrayList<>();
        final var literal = new TypedLiteral();
        for (final var keyword : keywords) {
            literal.setValue(keyword);
            keys.add(literal);
        }

        // Create the resource with the given parameters
        return (ResourceImpl) new ResourceBuilder()
                ._title_(Util.asList(new TypedLiteral(title)))
                ._description_(Util.asList(new TypedLiteral(description)))
                ._language_(Util.asList(Language.valueOf(language)))
                ._keyword_(keys)
                ._version_(version)
                ._standardLicense_(standardlicense)
                ._publisher_(publisher)
                ._created_(CalenderUtil.getGregorianNow())
                ._modified_(CalenderUtil.getGregorianNow())
                .build();
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
     * @return updated resource
     */
    public ResourceImpl updateResource(final URI resourceId, final String title,
                                       final String description, final String language,
                                       final List<String> keywords, final String version,
                                       final URI standardlicense, final URI publisher) {
        //Get a Resource and update if it exists
        for (final var resource : getResources()) {
            if (resource.getId().equals(resourceId)) {
                final var resImpl = (ResourceImpl) resource;
                updateResourceContent(title, description, language, keywords, version,
                        standardlicense, publisher, resImpl);
                return resImpl;
            }
        }
        return null;
    }

    /**
     * @param newResource new resource old version should be replaced with
     */
    public void updateResourceInAppRoute(final ResourceImpl newResource) {
        // Update the resource in the app route
        if (configModelService.getConfigModel().getAppRoute() == null) {
            if (log.isInfoEnabled()) {
                log.info("---- [ResourceService updateResourceInAppRoute] Could not find any app route to update the resource");
            }
        } else {
            final ArrayList<RouteStep> emptyList = new ArrayList<>();
            for (final var appRoute : configModelService.getConfigModel().getAppRoute()) {
                if (appRoute == null) {
                    continue;
                }
                if (appRoute.getAppRouteOutput() != null) {
                    for (final var resource : appRoute.getAppRouteOutput()) {
                        if (newResource.getId().equals(resource.getId())) {
                            final ArrayList<Resource> output =
                                    (ArrayList<Resource>) appRoute.getAppRouteOutput();
                            output.remove(resource);
                            output.add(newResource);
                            if (log.isInfoEnabled()) {
                                log.info("---- [ResourceService updateResourceInAppRoute] Updated resource in app route");
                            }
                            break;
                        }
                    }
                }
                if (appRoute.getHasSubRoute() == null) {
                    continue;
                }
                for (final var subRoute : appRoute.getHasSubRoute()) {
                    updateResourceInSubroutes(subRoute, emptyList, newResource);
                }
            }
            configModelService.saveState();
        }
    }

    /**
     * @param routeStep   routestep
     * @param visited     list of route steps already managed
     * @param newResource new resource old version should be replaced with
     */
    private void updateResourceInSubroutes(final RouteStep routeStep,
                                           final List<RouteStep> visited,
                                           final ResourceImpl newResource) {
        if (routeStep == null) {
            return;
        }
        if (routeStep.getAppRouteOutput() != null) {
            for (final var resource : routeStep.getAppRouteOutput()) {
                if (newResource.getId().equals(resource.getId())) {
                    final ArrayList<Resource> output =
                            (ArrayList<Resource>) routeStep.getAppRouteOutput();
                    output.remove(resource);
                    output.add(newResource);
                    if (log.isInfoEnabled()) {
                        log.info("---- [ResourceService updateResourceInAppRoute] Updated resource in subroute");
                    }
                    break;
                }
            }
        }
        if (routeStep.getHasSubRoute() == null) {
            return;
        }
        for (final var subRoute : routeStep.getHasSubRoute()) {
            if (!visited.contains(subRoute)) {
                visited.add(routeStep);
                updateResourceInSubroutes(subRoute, visited, newResource);
            }
        }
    }

}
