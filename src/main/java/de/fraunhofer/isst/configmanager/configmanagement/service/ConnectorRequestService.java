package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.BaseConnector;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing external connector requests.
 */
@Service
public class ConnectorRequestService {

    private final static Logger LOGGER = LoggerFactory.getLogger(AppRouteService.class);
    private final DefaultConnectorClient client;

    @Autowired
    public ConnectorRequestService(DefaultConnectorClient client) {
        this.client = client;
    }

    /**
     * This method returns from the connector the requested resources.
     *
     * @param recipientId id of the recipient
     * @return list of resources
     */
    public List<Resource> requestResourcesFromConnector(URI recipientId) {
        try {
            BaseConnector connector = client.getBaseConnector(recipientId.toString(), "");
            if (connector != null && connector.getResourceCatalog() != null) {

                List<Resource> resourceList = new ArrayList<>();
                for (ResourceCatalog resourceCatalog : connector.getResourceCatalog()) {
                    if (resourceCatalog != null && resourceCatalog.getOfferedResource() != null) {
                        resourceList.addAll(resourceCatalog.getOfferedResource());
                    }
                }
                return resourceList;
            } else {
                LOGGER.info("Could not determine the resources of the connector");
                return null;
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * This method gets the resource from the client using the connector uri und requested resource uri.
     *
     * @param recipientId   id of the recipient
     * @param requestedResourceId id of the requested resource
     * @return resource
     */
    public Resource requestResource(URI recipientId, URI requestedResourceId) {

        try {
            Resource resource = client.getResource(recipientId.toString(), requestedResourceId.toString());
            if (resource != null) {
                return resource;
            } else {
                LOGGER.info("Could not determine resource");
                return null;
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * This method creates a custom resource list with specified attributes
     *
     * @param resources list of resources
     * @return json array of resources
     */
    public JSONArray createResourceList(List<Resource> resources) {

        var jsonArray = new JSONArray();

        for (Resource resource : resources) {
            var jsonObject = new JSONObject();

            jsonObject.put("resourceId", resource.getId().toString());
            if (resource.getTitle() != null) {
                jsonObject.put("title", resource.getTitle().get(0).getValue());
            }
            if (resource.getDescription() != null) {
                jsonObject.put("description", resource.getDescription().get(0).getValue());
            }
            if (resource.getLanguage() != null) {
                jsonObject.put("language", resource.getLanguage().get(0).getLabel().get(0).getValue());
            }
            if (resource.getKeyword() != null) {
                jsonObject.put("keyword", resource.getKeyword());
            }
            if (resource.getVersion() != null) {
                jsonObject.put("version", resource.getVersion());
            }
            if (resource.getStandardLicense() != null) {
                jsonObject.put("standardlicense", resource.getStandardLicense().toString());
            }
            if (resource.getPublisher() != null) {
                jsonObject.put("publisher", resource.getPublisher().toString());
            }
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }
}
