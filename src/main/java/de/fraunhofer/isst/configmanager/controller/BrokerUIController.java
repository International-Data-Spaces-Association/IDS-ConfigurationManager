package de.fraunhofer.isst.configmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.configmanagement.entities.config.BrokerStatus;
import de.fraunhofer.isst.configmanager.configmanagement.entities.config.CustomBroker;
import de.fraunhofer.isst.configmanager.configmanagement.service.BrokerService;
import de.fraunhofer.isst.configmanager.configmanagement.service.ResourceService;
import de.fraunhofer.isst.configmanager.util.Utility;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * The controller class implements the BrokerAPI and offers the possibilities to manage
 * the brokers in the configuration manager.
 */
@RestController
@RequestMapping("/api/ui")
@Tag(name = "Broker Management", description = "Endpoints for managing the brokers in the configuration manager")
public class BrokerUIController implements BrokerUIApi {

    private final static Logger logger = LoggerFactory.getLogger(BrokerUIController.class);

    private final ResourceService resourceService;
    private final BrokerService brokerService;
    private final DefaultConnectorClient client;
    private final ObjectMapper objectMapper;

    @Autowired
    public BrokerUIController(ResourceService resourceService,
                              BrokerService brokerService,
                              DefaultConnectorClient client,
                              ObjectMapper objectMapper) {
        this.resourceService = resourceService;
        this.brokerService = brokerService;
        this.client = client;
        this.objectMapper = objectMapper;
    }

    /**
     * This method creates a broker with the given parameters.
     *
     * @param brokerUri uri of the broker
     * @param title     title of the broker
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> createBroker(URI brokerUri, String title) {

        CustomBroker brokerObject = brokerService.createCustomBroker(brokerUri, title);

        if (brokerObject != null) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Created a new broker with id: " + brokerUri));
        } else {
            return ResponseEntity.badRequest().body("Could not create a broker");
        }
    }

    /**
     * This method updates the broker with the given parameters.
     *
     * @param brokerId id of the broker
     * @param title    title of the broker
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateBroker(URI brokerId, String title) {
        if (brokerService.updateBroker(brokerId, title)) {
            var jsonObject = new JSONObject();
            jsonObject.put("message", "Updated the broker");
            jsonObject.put("brokerId", brokerId.toString());
            return ResponseEntity.ok(jsonObject.toJSONString());
        } else {
            return ResponseEntity.badRequest().body("Could not update the broker");
        }
    }

    /**
     * This method deletes the broker with the given id.
     *
     * @param brokerUri uri of the broker
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> deleteBroker(URI brokerUri) {
        if (brokerService.deleteBroker(brokerUri)) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Broker with ID: " + brokerUri + " is deleted"));
        } else {
            return ResponseEntity.badRequest().body("Could not delete the broker with the id:" + brokerUri);
        }
    }

    /**
     * This method returns a broker with the given id.
     *
     * @param brokerId id of the broker
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getBroker(URI brokerId) {
        CustomBroker broker = brokerService.getById(brokerId);

        if (broker != null) {
            try {
                return ResponseEntity.ok(objectMapper.writeValueAsString(broker));
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        return ResponseEntity.badRequest().body("Could not get the specific broker");
    }

    /**
     * This method returns a list of all brokers as string.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getAllBrokers() {
        List<CustomBroker> brokers = brokerService.getCustomBrokers();
        try {
            return new ResponseEntity<>(objectMapper.writeValueAsString(brokers), HttpStatus.OK);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * This method returns a list of broker uri's.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getAllBrokerUris() {
        List<URI> brokerUris = brokerService.getAllBrokerUris();
        if (brokerUris != null) {
            return ResponseEntity.ok(brokerUris.toString());
        } else {
            return ResponseEntity.badRequest().body("Could not return the uri list of brokers");
        }
    }

    /**
     * This method registers a connector with a specific broker.
     *
     * @param brokerUri uri of the broker
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> registerConnector(URI brokerUri) {
        var broker = brokerService.getById(brokerUri);
        if (broker != null) {
            try {
                brokerService.setBrokerStatus(brokerUri, BrokerStatus.REGISTERED);
                return ResponseEntity.ok(client.updateAtBroker(brokerUri.toString()));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(String.format("Could not parse response from Connector: %s", e.getMessage()));
            }
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * This method removes a connector at a specific broker.
     *
     * @param brokerUri uri of the broker
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> unregisterConnector(URI brokerUri) {
        var broker = brokerService.getById(brokerUri);
        if (broker != null) {
            try {
                brokerService.setBrokerStatus(brokerUri, BrokerStatus.UNREGISTERED);
                return ResponseEntity.ok(client.unregisterAtBroker(brokerUri.toString()));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(String.format("Could not parse response from Connector: %s", e.getMessage()));
            }
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * This method updates a connector at a specific broker.
     *
     * @param brokerUri uri of the broker
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateConnector(URI brokerUri) {
        var broker = brokerService.getById(brokerUri);
        if (broker != null) {
            try {
                return ResponseEntity.ok(client.updateAtBroker(brokerUri.toString()));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(String.format("Could not parse response from Connector: %s", e.getMessage()));
            }
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * This method updates a {@link Resource} at a given broker URI.
     *
     * @param brokerUri  URI of the Broker the connector shall to connect to
     * @param resourceId the ID of the resource that shall be updated in the broker
     * @return HTTP response entity with the response as body string
     */
    @Override
    public ResponseEntity<String> updateResourceAtBroker(URI brokerUri, URI resourceId) {
        var broker = brokerService.getById(brokerUri);

        if (broker != null) {
            try {
                brokerService.setResourceAtBroker(brokerUri, resourceId);
                return ResponseEntity.ok(client.updateResourceAtBroker(brokerUri.toString(), resourceId));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(String.format("Could not parse response from Connector: %s", e.getMessage()));
            }
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * This method deletes a {@link Resource} at a given broker URI.
     *
     * @param brokerUri  URI of the Broker the connector shall to connect to
     * @param resourceId the ID of the resource that shall be updated in the broker
     * @return HTTP response entity with the response as body string
     */
    @Override
    public ResponseEntity<String> deleteResourceAtBroker(URI brokerUri, URI resourceId) {
        var broker = brokerService.getById(brokerUri);

        if (broker != null) {
            try {
                brokerService.deleteResourceAtBroker(brokerUri, resourceId);
                return ResponseEntity.ok(client.deleteResourceAtBroker(brokerUri.toString(), resourceId));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(String.format("Could not parse response from Connector: %s", e.getMessage()));
            }
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<String> getRegisterStatusForResource(URI resourceId) {

        var jsonObjet = brokerService.getRegisStatusForResource(resourceId);

        if (jsonObjet == null) {
            return ResponseEntity.badRequest().body("Could not get registration status for resource");
        } else {
            return ResponseEntity.ok(jsonObjet.toJSONString());
        }
    }

}
