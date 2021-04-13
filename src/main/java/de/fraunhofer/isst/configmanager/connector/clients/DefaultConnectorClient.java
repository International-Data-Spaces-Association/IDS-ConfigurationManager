package de.fraunhofer.isst.configmanager.connector.clients;

import de.fraunhofer.iais.eis.BaseConnector;
import de.fraunhofer.iais.eis.ConfigurationModel;

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
}
