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
package de.fraunhofer.isst.configmanager.connector.clients;

import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.Resource;
import okhttp3.Response;

import java.io.IOException;
import java.net.URI;

public interface DefaultResourceClient {

    /**
     * This method returns the uuid of a resource and the resource.
     *
     * @param accessURL  url of the connector
     * @param resourceId id of the resource
     * @return map from the id of the resource and the resource itself
     */
    String getRequestedResource(String accessURL, String resourceId) throws IOException;


    /**
     * Send a Resource update Request to a target Connector.
     *
     * @param resourceID ID of the Resource that will be created
     * @param resource   Resource to create
     * @return Response of the target Connector
     * @throws IOException when serializing of the Resource, or sending of the request fails
     */
    Response updateResource(URI resourceID, Resource resource) throws IOException;

    /**
     * Send a resource creation request to a target connector.
     *
     * @param resource Resource that will be created
     * @return Response of the target Connector
     * @throws IOException when serializing of the Resource, or sending of the request fails
     */
    String registerResource(Resource resource) throws IOException;

    /**
     * Send a resource deletion request to a target connector.
     *
     * @param resourceID ID of the Resource to delete
     * @return Response of the target Connector
     * @throws IOException when an error occurs while sending the request
     */
    String deleteResource(URI resourceID) throws IOException;

    /**
     * Send a resource representation creation request to the connector.
     *
     * @param resourceID     ID of the Resource for which the representation is registered
     * @param representation representation to be registered
     * @param endpointId ID of endpoint of the resource
     * @return Response of the target Connector
     * @throws IOException when an error occurs while sending the request
     */
    String registerResourceRepresentation(String resourceID, Representation representation,
                                          String endpointId) throws IOException;

    /**
     * Send a resource representation update request to a connector.
     *
     * @param resourceID       ID of the Resource for which the representation is updated
     * @param representationID ID of the representation to be updated
     * @param representation   representation to be updated
     * @param endpointId        ID of endpoint of the resource
     * @return Response of the target Connector
     * @throws IOException when an error occurs while sending the request
     */
    String updateResourceRepresentation(String resourceID, String representationID,
                                        Representation representation, String endpointId) throws IOException;

    /**
     * Updates a resource contract at a connector.
     *
     * @param resourceID ID of the Resource for which the contract is updated
     * @param contract   contract to be created
     * @return Response of the target Connector
     * @throws IOException when an error occurs while sending the request
     */
    String updateResourceContract(String resourceID, String contract) throws IOException;
}
