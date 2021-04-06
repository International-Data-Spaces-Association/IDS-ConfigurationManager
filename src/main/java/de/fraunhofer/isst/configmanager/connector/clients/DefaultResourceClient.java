package de.fraunhofer.isst.configmanager.connector.clients;

import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.isst.configmanager.connector.dataspaceconnector.model.ResourceRepresentation;

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
    Resource getRequestedResource(String accessURL, String resourceId) throws IOException;


    /**
     * Send a Resource update Request to a target Connector.
     *
     * @param resourceID ID of the Resource that will be created
     * @param resource   Resource to create
     * @return Response of the target Connector
     * @throws IOException when serializing of the Resource, or sending of the request fails
     */
    String updateResource(URI resourceID, Resource resource) throws IOException;

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
     * Send a resource representation deletion request to a connector.
     *
     * @param resourceID       ID of the Resource for which the representation is deleted
     * @param representationID ID of the Representation to delete
     * @return Response of the target Connector
     * @throws IOException when an error occurs while sending the request
     */
    String deleteResourceRepresentation(String resourceID, String representationID) throws IOException;

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
     * Updates a custom {@link ResourceRepresentation} at a connector.
     *
     * @param resourceID             ID of the Resource for which the representation is updated
     * @param representationID       ID of the representation to be updated
     * @param resourceRepresentation representation to be updated
     * @return Response of the target Connector
     * @throws IOException when an error occurs while sending the request
     */
    String updateCustomResourceRepresentation(String resourceID, String representationID,
                                              ResourceRepresentation resourceRepresentation) throws IOException;

    /**
     * Updates a resource contract at a connector.
     *
     * @param resourceID ID of the Resource for which the contract is updated
     * @param contract   contract to be created
     * @return Response of the target Connector
     * @throws IOException when an error occurs while sending the request
     */
    String updateResourceContract(String resourceID, String contract) throws IOException;

    /**
     * Returns the offered resources of the self declaration of a connector.
     *
     * @return json-string with all offered resources
     * @throws IOException when an error occurs while sending the request
     */
    String getOfferedResourcesAsJsonString() throws IOException;

    /**
     * Returns the requested resources of the self declaration of a connector.
     *
     * @return json-string with all requested resources
     * @throws IOException when an error occurs while sending the request
     */
    String getRequestedResourcesAsJsonString() throws IOException;

}
