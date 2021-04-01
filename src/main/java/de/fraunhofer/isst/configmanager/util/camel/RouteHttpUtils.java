package de.fraunhofer.isst.configmanager.util.camel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class RouteHttpUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteHttpUtils.class);

    private static String camelApplicationUrl;

    private static String camelApplicationRoutesPath;

//    private static String camelApplicationBeansPath;

    private static RestTemplate restTemplate = new RestTemplate();

    @Value("${camel.application.url}")
    public void setCamelApplicationUrl(String value) {
        camelApplicationUrl = value;
    }

    @Value("${camel.application.path.routes}")
    public void setCamelApplicationRoutesPath(String value) {
        camelApplicationRoutesPath = value;
    }

//    @Value("${camel.application.path.beans}")
//    public void setCamelApplicationBeansPath(String value) {
//        camelApplicationBeansPath = value;
//    }

    /**
     * Sends an XML route to the Camel application specified in application.properties as a file.
     *
     * @param xml the XML route
     */
    public static void sendRouteFileToCamelApplication(String xml) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource byteArrayResource = new ByteArrayResource(xml.getBytes()) {
            @Override
            public String getFilename(){
                return "route.xml";
            }
        };
        body.add("file", byteArrayResource);

        String url = camelApplicationUrl + camelApplicationRoutesPath;
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error("Error sending file to Camel: {}, {}", response.getStatusCodeValue(), response.getBody());
        }
    }

    /**
     * Deletes a route with the given ID at the Camel application specified in
     * application.propeties.
     *
     * @param routeId ID of the route to delete
     */
    public static void deleteRouteAtCamelApplication(String routeId) {
        String url = camelApplicationUrl + camelApplicationRoutesPath + "/" + routeId;
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error("Error deleting route at Camel: {}, {}", response.getStatusCodeValue(), response.getBody());
        }
    }

}
