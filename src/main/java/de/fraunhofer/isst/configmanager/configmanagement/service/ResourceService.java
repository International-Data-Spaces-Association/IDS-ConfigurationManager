package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.Language;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.iais.eis.ResourceImpl;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Service class for managing resources.
 */
@Service
public class ResourceService {

    private final ConfigModelService configModelService;

    @Autowired
    public ResourceService(ConfigModelService configModelService) {
        this.configModelService = configModelService;
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

        for (ResourceCatalog resourceCatalog : configModelService.getConfigModel()
                .getConnectorDescription().getResourceCatalog()) {
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
