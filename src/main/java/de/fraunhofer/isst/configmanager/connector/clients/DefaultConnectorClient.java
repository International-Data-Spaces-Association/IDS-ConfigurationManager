package de.fraunhofer.isst.configmanager.connector.clients;

import de.fraunhofer.iais.eis.BaseConnector;
import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.isst.configmanager.connector.dataspaceconnector.model.ResourceRepresentation;
import okhttp3.Response;

import java.io.IOException;
import java.net.URI;

/**
 * The interface DefaultConnectorClient defines methods that are implemented to make
 * configurations for the dataspace connector.
 * The implementations of the ConfigManager are oriented according to the structure of the
 * dataspace connectors.
 */
public interface DefaultConnectorClient {

    /**
     * This methods tries to connect to the  public connector endpoint.
     *
     * @throws IOException if connection fails
     */
    void getConnectorStatus() throws IOException;

    /**
     * The method helps to update connector in the broker. For this only the id of the
     * corresponding broker is necessary.
     *
     * @param brokerURI URI of the broker to update/register
     * @return Response of the update/register request of the connector
     * @throws IOException when sending the request fails
     */
    Response updateAtBroker(String brokerURI) throws IOException;

    /**
     * The method removes the connector from the corresponding broker. For this only the id of
     * the broker is necessary.
     *
     * @param brokerURI URI of the broker to unregister
     * @return Response of the unregister request of the connector
     * @throws IOException when sending the request fails
     */
    String unregisterAtBroker(String brokerURI) throws IOException;

    /**
     * The method returns the current configuration model.
     *
     * @return the current configuration model
     * @throws IOException if request fails
     */
    ConfigurationModel getConfiguration() throws IOException;


    /**
     * The boolean method helps to send the current configuration model to the target connector.
     *
     * @param configurationModel current configuration model that is sent to the target Connector
     * @return true if connector accepted configuration
     * @throws IOException when request cannot be sent
     */
    boolean sendConfiguration(String configurationModel) throws IOException;


    /**
     * This method returns the self declaration of a connector.
     *
     * @param accessURL  url of the connector
     * @param resourceId id of the resource
     * @return base connector
     */
    BaseConnector getBaseConnector(String accessURL, String resourceId) throws IOException;

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
     * Send a Resource update request to a target broker.
     *
     * @param brokerUri  URI of the Broker
     * @param resourceID ID of the Resource that will be created
     * @return Response of the target Connector
     * @throws IOException when serializing of the Resource, or sending of the request fails
     */
    String updateResourceAtBroker(String brokerUri, URI resourceID) throws IOException;

    /**
     * Send a resource deletion request to a target broker.
     *
     * @param brokerUri  URI of the Broker
     * @param resourceID ID of the Resource to delete
     * @return Response of the target Connector
     * @throws IOException when an error occurs while sending the request
     */
    String deleteResourceAtBroker(String brokerUri, URI resourceID) throws IOException;

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
     * Returns the policy pattern for a given string.
     *
     * @param policy string, representing a policy
     * @return policy pattern
     * @throws IOException when an error occurs while sending the request
     */
    String getPolicyPattern(String policy) throws IOException;


    /**
     * Returns the self declaration of a connector.
     *
     * @return base connector
     * @throws IOException when an error occurs while sending the request
     */
    BaseConnector getSelfDeclaration() throws IOException;

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

    /**
     * Sends a contract request to a connector by building an ContractRequestMessage.
     *
     * @param recipientId         id of the recipient
     * @param requestedArtifactId id of the requested artifact
     * @param contractOffer       contract offer for the requested resource
     * @return string, contract agreement id
     */
    String requestContractAgreement(String recipientId, String requestedArtifactId, String contractOffer) throws IOException;
}
