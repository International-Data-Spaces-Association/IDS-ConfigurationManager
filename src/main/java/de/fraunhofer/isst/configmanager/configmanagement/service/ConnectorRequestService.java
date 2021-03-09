package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Service class for managing external connector requests.
 */
@Service
public class ConnectorRequestService {

    private final static Logger LOGGER = LoggerFactory.getLogger(AppRouteService.class);
    private final DefaultConnectorClient client;

    @Autowired
    public ConnectorRequestService(DefaultConnectorClient client) {
        this.client = client;
    }

    public List<Resource> requestSelfDescription(URI recipientId) {

        try {
            BaseConnector connector = client.getBaseConnector(recipientId.toString(), "");
            if (connector != null && connector.getResourceCatalog() != null) {
                return (List<Resource>) connector.getResourceCatalog();
            } else {
                LOGGER.info("Could not determine the resources of the connector");
                return null;
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    public Resource requestResource(URI recipientId, URI reqResourceId) {

        try {
            Resource resource = client.getResource(recipientId.toString(), reqResourceId.toString());
            if (resource != null) {
                return resource;
            } else {
                LOGGER.info("Could not determine resource");
                return null;
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * This method creates a custom resource list with specified attributes
     *
     * @param resources list of resources
     * @return json array of resources
     */
    public JSONArray createResourceList(List<Resource> resources) {

        var jsonArray = new JSONArray();

        for (Resource resource : resources) {
            var jsonObject = new JSONObject();

            jsonObject.put("resourceId", resource.getId().toString());
            jsonObject.put("title", resource.getTitle().get(0).getValue());
            jsonObject.put("description", resource.getDescription().get(0).getValue());
            jsonObject.put("keyword", resource.getKeyword());
            jsonObject.put("version", resource.getVersion());
            jsonObject.put("standardlicense", resource.getStandardLicense().toString());
            jsonObject.put("publisher", resource.getPublisher().toString());

            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    /**
     * This method returns the content of a resource as json array.
     *
     * @param resource the resource
     * @return json array with resource content
     */
    public JSONArray getResourceContent(Resource resource) {

        // ToDO: Inquire how many artifacts and contractoffers there can be
        var jsonArray = new JSONArray();

        // Get artifact from resource
        if (resource.getRepresentation() != null) {
            var artifact = new JSONObject();
            for (Representation representation : resource.getRepresentation()) {
                if (representation != null && representation.getInstance() != null) {
                    for (RepresentationInstance representationInstance : representation.getInstance()) {
                        Artifact artifactTmp = (Artifact) representationInstance;
                        artifact.put(artifactTmp.getId().toString(), artifactTmp);
                    }
                }
            }
            jsonArray.add(artifact);
        }

        //Get contract from resource
        if (resource.getContractOffer() != null && resource.getContractOffer().size() > 0) {
            ContractOffer contractOffer = resource.getContractOffer().get(0);
            var contract = new JSONObject();
            contract.put(contractOffer.getId().toString(), contractOffer);
            jsonArray.add(contract);
        }
        return jsonArray;
    }
}
