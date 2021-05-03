package de.fraunhofer.isst.configmanager.util.camel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import de.fraunhofer.isst.configmanager.util.OkHttpUtils;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Component for deploying and deleting Camel routes at the Camel application via HTTP.
 */
@Component
public class RouteHttpHelper {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RouteHttpHelper.class);

    /**
     * URL of the Camel application.
     */
    @Value("${camel.application.url}")
    private String camelApplicationUrl;

    /**
     * Username for the Camel application.
     */
    @Value("${camel.application.username}")
    private String camelApplicationUsername;

    /**
     * Password for the Camel application.
     */
    @Value("${camel.application.password}")
    private String camelApplicationPassword;

    /**
     * The Camel application's API path for managing routes.
     */
    @Value("${camel.application.path.routes}")
    private String camelApplicationRoutesPath;

    /**
     * The OkHttpClient for sending requests to the Camel application.
     */
    private final OkHttpClient httpClient = OkHttpUtils.getUnsafeOkHttpClient();

    /**
     * Sends an XML route to the Camel application specified in application.properties as a file.
     *
     * @param xml the XML route
     * @throws IOException if the HTTP request cannot be sent or the response status code is not 2xx
     */
    public void sendRouteFileToCamelApplication(final String xml) throws IOException {
        final var url = camelApplicationUrl + camelApplicationRoutesPath;

        final var body = new MultipartBody.Builder().addFormDataPart("file",
                "route.xml", RequestBody.create(xml.getBytes(StandardCharsets.UTF_8),
                        MediaType.parse("application/xml"))).build();

        final var request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", Credentials.basic(camelApplicationUsername,
                        camelApplicationPassword))
                .build();

        try {
            final var response = httpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                LOGGER.error("Error sending file to Camel: {}, {}", response.code(),
                        (response.body() != null ? response.body().string() : "No response body."));
                throw new IOException("Request for deploying route was unsuccessful with code "
                        + response.code());
            }

        } catch (IOException e) {
            LOGGER.error("Error sending file to Camel: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Deletes a route with the given ID at the Camel application specified in
     * application.properties.
     *
     * @param routeId ID of the route to delete
     * @throws IOException if the HTTP request cannot be sent or the response status code is not 2xx
     */
    public void deleteRouteAtCamelApplication(final String routeId) throws IOException {
        final var url = camelApplicationUrl + camelApplicationRoutesPath + "/" + routeId;

        final var request = new Request.Builder()
                .url(url)
                .delete()
                .header("Authorization", Credentials.basic(camelApplicationUsername,
                        camelApplicationPassword))
                .build();

        try {
            final var response = httpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                LOGGER.error("Error deleting route at Camel: {}, {}", response.code(),
                        (response.body() != null ? response.body().string() : "No response body."));
                throw new IOException("Request for deleting route was unsuccessful with code "
                        + response.code());
            }
        } catch (IOException e) {
            LOGGER.error("Error deleting route at Camel: {}", e.getMessage());
            throw e;
        }
    }

}
