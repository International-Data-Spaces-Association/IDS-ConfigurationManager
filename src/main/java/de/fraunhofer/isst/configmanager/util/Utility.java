package de.fraunhofer.isst.configmanager.util;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import net.minidev.json.JSONObject;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility class which can be used to define helper methods.
 */
public class Utility {

    /**
     * This method creates with the given parameters a JSON message.
     *
     * @param key   the key of the json object
     * @param value the value of the json object
     * @return json message
     */
    public static String jsonMessage(String key, String value) {
        var jsonObect = new JSONObject();
        jsonObect.put(key, value);

        return jsonObect.toJSONString();
    }

    /**
     * This method creates an app endpoint for an app.
     *
     * @param appEndpointType     endpoint type
     * @param port                endpoint port
     * @param documentation       endpoint documentation
     * @param endpointInformation endpoint information
     * @param accessURL           access url of the endpoint
     * @param inboundPath         inbound path
     * @param outboundPath        outbound path
     * @param language            the language
     * @param mediaType           the media type
     * @param path                path
     * @return app endpoint
     * @throws URISyntaxException if uri can not be created
     */
    public static AppEndpoint createAppEndpoint(AppEndpointType appEndpointType,
                                                BigInteger port, String documentation,
                                                String endpointInformation, String accessURL,
                                                String inboundPath, String outboundPath,
                                                Language language, String mediaType,
                                                String path) throws URISyntaxException {

        MediaType mediatype = new CustomMediaTypeBuilder()._filenameExtension_(mediaType).build();

        AppEndpoint appEndpoint = new AppEndpointBuilder()
                ._appEndpointType_(appEndpointType)
                ._appEndpointPort_(port)
                ._endpointDocumentation_(Util.asList(new URI(documentation)))
                ._endpointInformation_(Util.asList(new TypedLiteral(endpointInformation)))
                ._accessURL_(URI.create(accessURL))
                ._inboundPath_(inboundPath)
                ._outboundPath_(outboundPath)
                ._language_(language)
                ._appEndpointMediaType_(mediatype)
                ._path_(path)
                .build();

        return appEndpoint;
    }
}
