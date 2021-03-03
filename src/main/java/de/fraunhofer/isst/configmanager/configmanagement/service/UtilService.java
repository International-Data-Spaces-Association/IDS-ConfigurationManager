package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model.BackendSource;
import de.fraunhofer.isst.configmanager.configmanagement.entities.config.BrokerStatus;
import de.fraunhofer.isst.configmanager.configmanagement.entities.routeDeployMethod.DeployMethod;
import net.minidev.json.JSONArray;
import org.springframework.stereotype.Service;

/**
 * The class can be used to define auxiliary methods that are needed again and again.
 */
@Service
public class UtilService {

    /**
     * The method returns for a given enum name all enum values.
     *
     * @param enumName name of the enum
     * @return enums as string
     */
    public String getSpecificEnum(String enumName) {

        JSONArray jsonArray = new JSONArray();

        String name = enumName.toLowerCase();

        if (name.contains("loglevel")) {
            LogLevel[] logLevels = de.fraunhofer.iais.eis.LogLevel.values();
            for (int i = 0; i < logLevels.length; i++) {
                jsonArray.add(i, logLevels[i].name());
            }
        }
        if (name.contains("connectorstatus")) {
            ConnectorStatus[] connectorStatuses = de.fraunhofer.iais.eis.ConnectorStatus.values();
            for (int i = 0; i < connectorStatuses.length; i++) {
                jsonArray.add(i, connectorStatuses[i].name());
            }
        }
        if (name.contains("connectordeploymode")) {
            ConnectorDeployMode[] connectorDeployModes = de.fraunhofer.iais.eis.ConnectorDeployMode.values();
            for (int i = 0; i < connectorDeployModes.length; i++) {
                jsonArray.add(i, connectorDeployModes[i].name());
            }
        }
        if (name.contains("securityprofile")) {
            SecurityProfile[] securityProfiles = de.fraunhofer.iais.eis.SecurityProfile.values();
            for (int i = 0; i < securityProfiles.length; i++) {
                jsonArray.add(i, securityProfiles[i].name());
            }
        }
        if (name.contains("language")) {
            Language[] languages = de.fraunhofer.iais.eis.Language.values();
            for (int i = 0; i < languages.length; i++) {
                jsonArray.add(i, languages[i].name());
            }
        }
        if (name.contains("sourcetype")) {
            BackendSource.Type[] sourceTypes = BackendSource.Type.values();
            for (int i = 0; i < sourceTypes.length; i++) {
                jsonArray.add(i, sourceTypes[i].name());
            }
        }
        if (name.contains("deploymethod")) {
            DeployMethod[] deployMethods = DeployMethod.values();
            for (int i = 0; i < deployMethods.length; i++) {
                jsonArray.add(i, deployMethods[i].name());
            }
        }
        if (name.contains("brokerstatus")) {
            BrokerStatus[] brokerStatuses = BrokerStatus.values();
            for (int i = 0; i < brokerStatuses.length; i++) {
                jsonArray.add(i, brokerStatuses[i].name());
            }
        }
        return jsonArray.toJSONString();
    }
}
