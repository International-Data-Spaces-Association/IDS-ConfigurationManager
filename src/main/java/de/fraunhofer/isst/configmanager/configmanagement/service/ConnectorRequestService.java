package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.BaseConnector;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
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
     * @param recipientId         id of the recipient
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
     * This method requests the id of the contract agreement
     *
     * @param recipientId         id of the recipient
     * @param requestedArtifactId id of the requested artifact
     * @param contractOffer       contact offer for the requested resource
     * @return string, contract acgreement id
     */
    public String requestContractAgreement(String recipientId, String requestedArtifactId, String contractOffer) {

        try {
            return client.requestContractAgreement(recipientId, requestedArtifactId, contractOffer);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
