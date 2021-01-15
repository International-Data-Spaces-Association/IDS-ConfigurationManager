package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Service class for managing resources.
 */
@Service
public class ResourceService {

    private final static Logger logger = LoggerFactory.getLogger(ConnectorService.class);
    private final ConfigModelService configModelService;
    private final DefaultConnectorClient defaultConnectorClient;

    @Autowired
    public ResourceService(ConfigModelService configModelService, DefaultConnectorClient defaultConnectorClient) {
        this.configModelService = configModelService;
        this.defaultConnectorClient = defaultConnectorClient;
    }

    /**
     * This method updates the given resource in the connector.
     *
     * @param connector the current connector which holds the resource
     * @param resource  resource to be replaced
     * @return true, if resource is replaced
     */
    public boolean replaceResource(Connector connector, Resource resource) {

        var containsId = connector.getResourceCatalog().stream().map(ResourceCatalog::getOfferedResource).flatMap(Collection::stream)
                .map(resource1 -> resource1.getId().equals(resource.getId()))
                .reduce(false, Boolean::logicalOr);

        if (containsId) {
            for (var catalog : connector.getResourceCatalog()) {
                var list = catalog.getOfferedResource().stream()
                        .map(resource1 -> resource1.getId().equals(resource.getId()) ? resource : resource1)
                        .collect(Collectors.toCollection(ArrayList::new));

                var catalogImpl = (ResourceCatalogImpl) catalog;
                catalogImpl.setOfferedResource(list);
                return true;
            }
            // Tries to update the resource in the dataspace conncetor.
            try {
                defaultConnectorClient.updateResource(resource.getId(), resource);
            } catch (IOException e) {
                logger.warn("Could not update resource at DataspaceConnector!", e.getMessage());
            }
        }

        return false;
    }

    /**
     * This method tries to delete the given resource in the connector
     *
     * @param connector  the current connector which holds the resource
     * @param resourceId if of the resource
     * @return true, if is deleted
     */
    public boolean deleteResource(Connector connector, URI resourceId) {
        try {
            defaultConnectorClient.deleteResource(resourceId);
        } catch (IOException e) {
            logger.warn("Could not delete resource at DataspaceConnector!", e.getMessage());
        }
        return connector.getResourceCatalog().stream()
                .map(ResourceCatalog::getOfferedResource)
                .map(resources -> resources.removeIf(resource -> resource.getId().equals(resourceId)))
                .reduce(false, (a, b) -> a || b);
    }

    /**
     * Gets the {@link Resource} of a given resource ID.
     *
     * @param resourceId of the resource
     * @return resource
     */
    public Resource getResource(URI resourceId) {
        // Dunno why, but without try catch a NullPointerException is throwing within the return statement when the
        // resourceId cannot be matched with a resource.
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

        for (ResourceCatalog resourceCatalog : configModelService.getConfigModel().getConnectorDescription().getResourceCatalog()) {
            if (resourceCatalog != null && resourceCatalog.getOfferedResource() != null) {
                for (Resource resource : resourceCatalog.getOfferedResource()) {
                    if (resource != null) {
                        resources.add(resource);
                    }
                }
            }
        }
        return resources;
    }
}
