package de.fraunhofer.isst.configmanager.api.service;

import de.fraunhofer.iais.eis.ConnectorDeployMode;
import de.fraunhofer.iais.eis.ConnectorStatus;
import de.fraunhofer.iais.eis.Language;
import de.fraunhofer.iais.eis.LogLevel;

import de.fraunhofer.isst.configmanager.connector.dataspaceconnector.model.BackendSource;
import de.fraunhofer.isst.configmanager.model.config.BrokerStatus;
import de.fraunhofer.isst.configmanager.model.routedeploymethod.DeployMethod;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.json.JSONException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The class can be used to define auxiliary methods that are needed again and again.
 */
@Service
@Transactional
public class UtilService {

    /**
     * The method returns for a given enum name all enum values.
     *
     * @param enumName name of the enum
     * @return enums as string
     */
    public String getSpecificEnum(final String enumName) {
        final var jsonArray = new JSONArray();
        JSONArray sortedJsonArray = null;
        final var name = enumName.toLowerCase();

        if (name.contains("loglevel")) {
            final var logLevels = LogLevel.values();
            for (LogLevel logLevel : logLevels) {
                var jsonObject = new JSONObject();
                jsonObject.put("originalName", logLevel.name());
                jsonObject.put("displayName", logLevel.getLabel().get(0).getValue());
                jsonArray.add(jsonObject);
            }
            sortedJsonArray = sortJsonArray(jsonArray);
        }
        if (name.contains("connectorstatus")) {
            final var connectorStatuses = ConnectorStatus.values();
            for (ConnectorStatus connectorStatus : connectorStatuses) {
                var jsonObject = new JSONObject();
                jsonObject.put("originalName", connectorStatus.name());
                jsonObject.put("displayName", connectorStatus.getLabel().get(0).getValue());
                jsonArray.add(jsonObject);
            }
            sortedJsonArray = sortJsonArray(jsonArray);
        }
        if (name.contains("connectordeploymode")) {
            final var connectorDeployModes = ConnectorDeployMode.values();
            for (ConnectorDeployMode connectorDeployMode : connectorDeployModes) {
                var jsonObject = new JSONObject();
                jsonObject.put("originalName", connectorDeployMode.name());
                jsonObject.put("displayName", connectorDeployMode.getLabel().get(0).getValue());
                jsonArray.add(jsonObject);
            }
            sortedJsonArray = sortJsonArray(jsonArray);
        }
        if (name.contains("language")) {
            final var languages = Language.values();
            for (Language language : languages) {
                var jsonObject = new JSONObject();
                jsonObject.put("originalName", language.name());
                if ("LT".equals(language.name())) {
                    jsonObject.put("displayName", language.getLabel().get(1).getValue());
                } else jsonObject.put("displayName", language.getLabel().get(0).getValue());
                jsonArray.add(jsonObject);
            }
            sortedJsonArray = sortJsonArray(jsonArray);
        }
        if (name.contains("sourcetype")) {
            final var sourceTypes = BackendSource.Type.values();
            for (BackendSource.Type sourceType : sourceTypes) {
                var jsonObject = new JSONObject();
                jsonObject.put("displayName", sourceType.name());
                jsonArray.add(jsonObject);
            }
            sortedJsonArray = sortJsonArray(jsonArray);
        }
        if (name.contains("deploymethod")) {
            final var deployMethods = DeployMethod.values();
            for (DeployMethod deployMethod : deployMethods) {
                var jsonObject = new JSONObject();
                jsonObject.put("displayName", deployMethod.name());
                jsonArray.add(jsonObject);
            }
            sortedJsonArray = sortJsonArray(jsonArray);
        }
        if (name.contains("brokerstatus")) {
            final var brokerStatuses = BrokerStatus.values();
            for (BrokerStatus brokerStatus : brokerStatuses) {
                var jsonObject = new JSONObject();
                jsonObject.put("displayName", brokerStatus.name());
                jsonArray.add(jsonObject);
            }
            sortedJsonArray = sortJsonArray(jsonArray);
        }
        return sortedJsonArray.toJSONString();
    }

    /**
     * @param jsonArray json array to be sorted
     * @return sorted json array
     */
    private JSONArray sortJsonArray(JSONArray jsonArray) {
        List<JSONObject> jsonObjects = new ArrayList<>();
        JSONArray sortedJsonArray = new JSONArray();

        for (Object o : jsonArray) {
            jsonObjects.add((JSONObject) o);
        }
        jsonObjects.sort(new Comparator<>() {
            private static final String KEY_NAME = "displayName";

            @Override
            public int compare(JSONObject a, JSONObject b) {
                String str1 = "";
                String str2 = "";
                try {
                    str1 = (String) a.get(KEY_NAME);
                    str2 = (String) b.get(KEY_NAME);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return str1.compareTo(str2);
            }
        });

        for (int i = 0; i < jsonArray.size(); i++) {
            sortedJsonArray.add(i, jsonObjects.get(i));
        }
        return sortedJsonArray;
    }
}
