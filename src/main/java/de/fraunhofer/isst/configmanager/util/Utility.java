package de.fraunhofer.isst.configmanager.util;

import net.minidev.json.JSONObject;

public class Utility {

    public static String jsonMessage(String key, String value) {
        var jsonObect = new JSONObject();
        jsonObect.put(key, value);

        return jsonObect.toJSONString();
    }
}
