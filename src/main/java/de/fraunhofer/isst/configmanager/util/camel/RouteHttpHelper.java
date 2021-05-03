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

@Component
public class RouteHttpHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteHttpHelper.class);

    @Value("${camel.application.url}")
    private String camelApplicationUrl;

    @Value("${camel.application.username}")
    private String camelApplicationUsername;

    @Value("${camel.application.password}")
    private String camelApplicationPassword;

    @Value("${camel.application.path.routes}")
    private String camelApplicationRoutesPath;

    private final OkHttpClient httpClient = OkHttpUtils.getUnsafeOkHttpClient();

    /**
     * Sends an XML route to the Camel application specified in application.properties as a file.
     *
     * @param xml the XML route
     */
    public void sendRouteFileToCamelApplication(String xml) {
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
                        response.body().string());
            }

        } catch (IOException e) {
            LOGGER.error("Error sending file to Camel: {}", e.getMessage());
        }
    }

    /**
     * Deletes a route with the given ID at the Camel application specified in
     * application.propeties.
     *
     * @param routeId ID of the route to delete
     */
    public void deleteRouteAtCamelApplication(String routeId) {
        String url = camelApplicationUrl + camelApplicationRoutesPath + "/" + routeId;

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
                        response.body().string());
            }
        } catch (IOException e) {
            LOGGER.error("Error deleting route at Camel: {}", e.getMessage());
        }
    }

}
