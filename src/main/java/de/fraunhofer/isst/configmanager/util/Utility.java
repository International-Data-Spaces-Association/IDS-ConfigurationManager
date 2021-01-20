package de.fraunhofer.isst.configmanager.util;

import de.fraunhofer.isst.configmanager.configmanagement.entities.customApp.CustomAppEndpoint;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customApp.CustomEndpointType;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customApp.CustomLanguage;
import net.minidev.json.JSONObject;

public class Utility {

    public static String jsonMessage(String key, String value) {
        var jsonObect = new JSONObject();
        jsonObect.put(key, value);

        return jsonObect.toJSONString();
    }

    public static CustomAppEndpoint createCustomApp(CustomEndpointType customEndpointType,
                                                    int port, String documentation,
                                                    String endpointInformation, String accessURL,
                                                    String inboundPath, String outboundPath,
                                                    CustomLanguage customLanguage, String mediaType,
                                                    String path) {
        CustomAppEndpoint customAppEndpoint = new CustomAppEndpoint();
        customAppEndpoint.setCustomEndpointType(customEndpointType);
        customAppEndpoint.setEndpointPort(port);
        customAppEndpoint.setEndpointDocumentation(documentation);
        customAppEndpoint.setEndpointInformation(endpointInformation);
        customAppEndpoint.setAccessURL(accessURL);
        customAppEndpoint.setInboundPath(inboundPath);
        customAppEndpoint.setOutboundPath(outboundPath);
        customAppEndpoint.setLanguage(customLanguage);
        customAppEndpoint.setMediaType(mediaType);
        customAppEndpoint.setPath(path);
        return customAppEndpoint;
    }
}
