package de.fraunhofer.isst.configmanager.util.json;

import lombok.experimental.UtilityClass;
import net.minidev.json.JSONObject;

/**
 * Utility class which can be used to define helper methods.
 */
@UtilityClass
public class JsonUtils {
    /**
     * This method creates with the given parameters a JSON message.
     *
     * @param key   the key of the json object
     * @param value the value of the json object
     * @return json message
     */
    public static String jsonMessage(final String key, final String value) {
        final var jsonObject = new JSONObject();
        jsonObject.put(key, value);

        return jsonObject.toJSONString();
    }
}
