package de.fraunhofer.isst.configmanager.api.service;

import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultConnectorClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing external connector requests.
 */
@Slf4j
@Service
@Transactional
public class ConnectorRequestService {

    private final transient DefaultConnectorClient client;

    @Autowired
    public ConnectorRequestService(final DefaultConnectorClient client) {
        this.client = client;
    }

    /**
     * This method returns from the connector the requested resources.
     *
     * @param recipientId id of the recipient
     * @return list of resources
     */
    public List<Resource> requestResourcesFromConnector(final URI recipientId) {
        try {
            final var connector = client.getBaseConnector(recipientId.toString(), "");
            if (connector != null && connector.getResourceCatalog() != null) {

                final List<Resource> resourceList = new ArrayList<>();
                for (final var resourceCatalog : connector.getResourceCatalog()) {
                    if (resourceCatalog != null && resourceCatalog.getOfferedResource() != null) {
                        resourceList.addAll(resourceCatalog.getOfferedResource());
                    }
                }
                return resourceList;
            } else {
                log.info("Could not determine the resources of the connector");
                return null;
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
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
    public Resource requestResource(final URI recipientId, final URI requestedResourceId) {

        try {
            final var resource = client.getRequestedResource(recipientId.toString(), requestedResourceId.toString());
            if (resource != null) {
                return resource;
            } else {
                log.info("Could not determine resource");
                return null;
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
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
    public String requestContractAgreement(final String recipientId,
                                           final String requestedArtifactId,
                                           final String contractOffer) {

        try {
            return client.requestContractAgreement(recipientId, requestedArtifactId, contractOffer);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
