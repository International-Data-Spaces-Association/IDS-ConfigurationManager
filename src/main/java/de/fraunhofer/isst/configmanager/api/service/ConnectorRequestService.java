/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.isst.configmanager.api.service;

import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultResourceClient;
import de.fraunhofer.isst.configmanager.data.util.QueryInput;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service class for managing external connector requests.
 */
@Slf4j
@Service
@Transactional
public class ConnectorRequestService {

    private final transient DefaultResourceClient resourceClient;
    private final transient DefaultConnectorClient connectorClient;

    @Autowired
    public ConnectorRequestService(final DefaultResourceClient resourceClient,
                                   final DefaultConnectorClient connectorClient) {
        this.resourceClient = resourceClient;
        this.connectorClient = connectorClient;
    }

    /**
     * This method returns from the connector the requested resources.
     *
     * @param recipientId id of the recipient
     * @return list of resources
     */
    public List<Resource> requestResourcesFromConnector(final URI recipientId) {
        try {
            final var connector = connectorClient.getBaseConnector(recipientId.toString(), "");
            if (connector != null && connector.getResourceCatalog() != null) {

                final List<Resource> resourceList = new ArrayList<>();
                for (final var resourceCatalog : connector.getResourceCatalog()) {
                    if (resourceCatalog != null && resourceCatalog.getOfferedResource() != null) {
                        resourceList.addAll(resourceCatalog.getOfferedResource());
                    }
                }
                return resourceList;
            } else {
                if (log.isInfoEnabled()) {
                    log.info("---- [ConnectorRequestService requestResourcesFromConnector] Could not determine the resources of the connector");
                }
                return null;
            }
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
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
    public String requestResource(final URI recipientId, final URI requestedResourceId) {
        String customResponse;

        try {
            final var response = resourceClient.getRequestedResource(recipientId.toString(), requestedResourceId.toString());
            if (response != null) {
                final var splitBody = response.split("\n", 2);
                final var validationKey = splitBody[0].substring(12);
                final var resource = splitBody[1].substring(10);
                customResponse = "Validation Key: " + validationKey + "\nResource: " + resource;
            } else {
                if (log.isInfoEnabled()) {
                    log.info("---- [ConnectorRequestService requestResource] Could not determine resource");
                }
                return null;
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
        return customResponse;
    }

    /**
     * This method requests the id of the contract agreement.
     *
     * @param recipientId         id of the recipient
     * @param requestedArtifactId id of the requested artifact
     * @param contractOffer       contact offer for the requested resource
     * @return string, contract agreement id
     */
    public String requestContractAgreement(final String recipientId,
                                           final String requestedArtifactId,
                                           final String contractOffer) {

        try {
            return connectorClient.requestContractAgreement(recipientId, requestedArtifactId, contractOffer);
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Requests data from an external connector by building an ArtifactRequestMessage.
     *
     * @param recipientId         The target connector uri
     * @param requestedArtifactId The requested artifact uri
     * @param contractId          The URI of the contract agreement
     * @param key                 a {@link java.util.UUID} object
     * @param queryInput          the query to fetch data from backend systems
     * @return response of data request
     */
    public Response requestData(final URI recipientId,
                                final URI requestedArtifactId,
                                final URI contractId,
                                final UUID key,
                                final QueryInput queryInput) {

        try {
            return connectorClient.requestData(recipientId.toString(), requestedArtifactId.toString(), contractId.toString(),
                    key.toString(), queryInput);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
