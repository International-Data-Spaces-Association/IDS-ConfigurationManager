package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.communication.dataspaceconnector.DataSpaceConnectorResourceMapper;
import de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model.ResourceRepresentation;
import de.fraunhofer.isst.configmanager.configmanagement.entities.routeDeployMethod.DeployMethod;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;

/**
 * The class can be used to define auxiliary methods that are needed again and again.
 */
@Service
public class UtilService {

    private final EndpointService endpointService;
    private final DataSpaceConnectorResourceMapper dataSpaceConnectorResourceMapper;
    private final DefaultConnectorClient client;

    @Autowired
    public UtilService(EndpointService endpointService,
                       DataSpaceConnectorResourceMapper dataSpaceConnectorResourceMapper,
                       DefaultConnectorClient client) {

        this.endpointService = endpointService;
        this.dataSpaceConnectorResourceMapper = dataSpaceConnectorResourceMapper;
        this.client = client;
    }

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
            ResourceRepresentation.SourceType[] sourceTypes = ResourceRepresentation.SourceType.values();
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
        return jsonArray.toJSONString();
    }

    /**
     * Helper method to add accessUrl, username and password to a representation by its given ID
     * and update it in the Connector.
     *
     * @param endpointId     id of the endpoint
     * @param resourceId     id of the resource
     * @param representation representation
     * @return HTTP response entity with the response as body string
     */
    public ResponseEntity<String> addEndpointToConnectorRepresentation(URI endpointId, URI resourceId,
                                                                       Representation representation) {

        var endpoint = (GenericEndpoint) endpointService.getGenericEndpoints()
                .stream()
                .filter(endP -> endP.getId().equals(endpointId)).findAny().orElse(null);

        if (endpoint != null) {
            BasicAuthenticationImpl basicAuth =
                    (BasicAuthenticationImpl) endpoint.getGenericEndpointAuthentication();
            if (basicAuth != null) {
                String accessUrl = endpoint.getAccessURL().toString();
                String username = basicAuth.getAuthUsername();
                String password = basicAuth.getAuthPassword();

                ResourceRepresentation resourceRepresentation =
                        dataSpaceConnectorResourceMapper.mapCustomRepresentation(representation, accessUrl,
                                username, password);
                try {
                    // Try to add the new Representation object to the Connector
                    return ResponseEntity.ok(client.updateCustomResourceRepresentation(resourceId.toString(),
                            representation.getId().toString(), resourceRepresentation));
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(String.format("Could not parse response from Connector: %s", e.getMessage()));
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
