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
package de.fraunhofer.isst.configmanager.connector.dataspaceconnector.clients;

import de.fraunhofer.isst.configmanager.connector.clients.DefaultBrokerClient;
import de.fraunhofer.isst.configmanager.connector.dataspaceconnector.util.DispatchRequest;
import de.fraunhofer.isst.configmanager.connector.dataspaceconnector.util.ResourceMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConditionalOnExpression("${dataspace.connector.enabled:false}")
public class DataspaceBrokerClient extends AbstractDataspaceConnectorClient implements DefaultBrokerClient {

    public DataspaceBrokerClient(final ResourceMapper dataSpaceConnectorResourceMapper) {
        super(dataSpaceConnectorResourceMapper);
    }

    @Override
    public Response updateAtBroker(final String brokerURI) throws IOException {
        log.info(String.format(
                "---- [DataspaceBrokerClient updateAtBroker] updating connector %s at broker %s",
                dataSpaceConnectorHost,
                brokerURI));

        final var builder = getRequestBuilder();
        builder.url(new HttpUrl.Builder()
                .scheme(protocol)
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/broker/update")
                .addQueryParameter("broker", brokerURI)
                .build());
        builder.post(RequestBody.create(brokerURI, okhttp3.MediaType.parse("text/html")));
        builder.header("Authorization",
                Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));

        final var request = builder.build();

        return DispatchRequest.sendToDataspaceConnector(request);
    }

    @Override
    public Response unregisterAtBroker(final String brokerURI) throws IOException {
        if (log.isInfoEnabled()) {
            log.info(String.format(
                    "---- [DataspaceBrokerClient unregisterAtBroker] unregistering connector %s at broker %s",
                    dataSpaceConnectorHost,
                    brokerURI));
        }

        final var builder = getRequestBuilder();
        builder.url(new HttpUrl.Builder()
                .scheme(protocol)
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/broker/unregister")
                .addQueryParameter("broker", brokerURI)
                .build());
        builder.post(RequestBody.create(brokerURI, okhttp3.MediaType.parse("text/html")));
        builder.header("Authorization", Credentials.basic(dataSpaceConnectorApiUsername,
                dataSpaceConnectorApiPassword));

        final var request = builder.build();

        return DispatchRequest.sendToDataspaceConnector(request);
    }

    @Override
    public Response updateResourceAtBroker(final String brokerUri, final URI resourceID) throws IOException {
        if (log.isInfoEnabled()) {
            log.info(String.format("---- [DataspaceBrokerClient updateResourceAtBroker] updating resource at Broker %s", brokerUri));
        }

        final var path = resourceID.getPath();
        final var idStr = path.substring(path.lastIndexOf('/') + 1);
        final var resourceUUID = UUID.fromString(idStr);

        final var builder = getRequestBuilder();
        builder.url(new HttpUrl.Builder()
                .scheme(protocol)
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/broker/update/" + resourceUUID)
                .addQueryParameter("broker", brokerUri)
                .build());
        builder.post(RequestBody.create(new byte[0], null));
        builder.header("Authorization",
                Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));
        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful() && log.isWarnEnabled()) {
           log.warn("---- [DataspaceBrokerClient updateResourceAtBroker] Updating Resource at Broker failed!");
        }

        return response;
    }

    @Override
    public Response deleteResourceAtBroker(final String brokerUri, final URI resourceID) throws IOException {
        if (log.isInfoEnabled()) {
            log.info(String.format("---- [DataspaceBrokerClient deleteResourceAtBroker] Deleting resource %s at Broker %s", resourceID, brokerUri));
        }

        final var path = resourceID.getPath();
        final var idStr = path.substring(path.lastIndexOf('/') + 1);
        final var builder = getRequestBuilder();
        builder.url(new HttpUrl.Builder()
                .scheme(protocol)
                .host(dataSpaceConnectorHost)
                .port(dataSpaceConnectorPort)
                .addPathSegments("admin/api/broker/remove/" + idStr)
                .addQueryParameter("broker", brokerUri)
                .build());
        builder.post(RequestBody.create(new byte[0], null));
        builder.header("Authorization",
                Credentials.basic(dataSpaceConnectorApiUsername, dataSpaceConnectorApiPassword));

        final var request = builder.build();
        final var response = DispatchRequest.sendToDataspaceConnector(request);

        if (!response.isSuccessful() && log.isWarnEnabled()) {
            log.warn("---- [DataspaceBrokerClient deleteResourceAtBroker] Deleting Resource at Broker failed!");
        }

        return response;
    }

}
