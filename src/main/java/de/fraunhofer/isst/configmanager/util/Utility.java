package de.fraunhofer.isst.configmanager.util;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import net.minidev.json.JSONObject;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;

public class Utility {

    public static String jsonMessage(String key, String value) {
        var jsonObect = new JSONObject();
        jsonObect.put(key, value);

        return jsonObect.toJSONString();
    }

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
