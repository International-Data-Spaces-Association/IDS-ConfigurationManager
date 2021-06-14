package de.fraunhofer.isst.configmanager.extensions.apps.util;

import de.fraunhofer.iais.eis.AppEndpointType;
import de.fraunhofer.iais.eis.CustomMediaTypeBuilder;
import de.fraunhofer.iais.eis.Language;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import lombok.experimental.UtilityClass;
import net.minidev.json.JSONObject;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility class which can be used to define helper methods.
 */
@UtilityClass
public class AppEndpointBuilder {

    /**
     * This method creates with the given parameters a JSON message.
     *
     * @param key   the key of the json object
     * @param value the value of the json object
     * @return json message
     */
    public static String jsonMessage(final String key, final String value) {
        final var jsonObect = new JSONObject();
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
    public static de.fraunhofer.iais.eis.AppEndpoint createAppEndpoint(final AppEndpointType appEndpointType,
                                                                       final BigInteger port,
                                                                       final String documentation,
                                                                       final String endpointInformation,
                                                                       final String accessURL,
                                                                       final String inboundPath,
                                                                       final String outboundPath,
                                                                       final Language language,
                                                                       final String mediaType,
                                                                       final String path) throws URISyntaxException {

        final var mediatype = new CustomMediaTypeBuilder()._filenameExtension_(mediaType).build();

        return new de.fraunhofer.iais.eis.AppEndpointBuilder()
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
    }
}
