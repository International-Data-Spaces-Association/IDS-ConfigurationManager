package de.fraunhofer.isst.configmanager.connector.clients;

import okhttp3.Response;

import java.io.IOException;
import java.net.URI;

public interface DefaultBrokerClient {

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
    Response unregisterAtBroker(String brokerURI) throws IOException;

    /**
     * Send a Resource update request to a target broker.
     *
     * @param brokerUri  URI of the Broker
     * @param resourceID ID of the Resource that will be created
     * @return Response of the target Connector
     * @throws IOException when serializing of the Resource, or sending of the request fails
     */
    Response updateResourceAtBroker(String brokerUri, URI resourceID) throws IOException;

    /**
     * Send a resource deletion request to a target broker.
     *
     * @param brokerUri  URI of the Broker
     * @param resourceID ID of the Resource to delete
     * @return Response of the target Connector
     * @throws IOException when an error occurs while sending the request
     */
    Response deleteResourceAtBroker(String brokerUri, URI resourceID) throws IOException;
}
