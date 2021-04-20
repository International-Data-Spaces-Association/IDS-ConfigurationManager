package de.fraunhofer.isst.configmanager.connector.clients;

import de.fraunhofer.iais.eis.BaseConnector;
import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.isst.configmanager.model.config.QueryInput;
import okhttp3.Response;

import java.io.IOException;

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
     * Sends a contract request to a connector by building an ContractRequestMessage.
     *
     * @param recipientId         id of the recipient
     * @param requestedArtifactId id of the requested artifact
     * @param contractOffer       contract offer for the requested resource
     * @return string, contract agreement id
     */
    String requestContractAgreement(String recipientId, String requestedArtifactId, String contractOffer) throws IOException;

    /**
     * Requests data from an external connector.
     *
     * @param recipientId         the target connector uri
     * @param requestedArtifactId the requested artifact uri
     * @param contractId          the URI of the contract agreement
     * @param key                 a unique validation key
     * @param queryInput          the query input to fetch data from the backend systems
     * @return requested data or error message
     */
    Response requestData(String recipientId, String requestedArtifactId, String contractId,
                         String key, QueryInput queryInput) throws IOException;
}
