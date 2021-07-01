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
