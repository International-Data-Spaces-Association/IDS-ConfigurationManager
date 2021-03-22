package de.fraunhofer.isst.configmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.configmanagement.entities.config.BrokerStatus;
import de.fraunhofer.isst.configmanager.configmanagement.entities.config.CustomBroker;
import de.fraunhofer.isst.configmanager.configmanagement.service.BrokerService;
import de.fraunhofer.isst.configmanager.util.Utility;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
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
@Tag(name = "Broker Management", description = "Endpoints for managing the brokers in the " +
        "configuration manager")
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BrokerUIController implements BrokerUIApi {
    transient BrokerService brokerService;
    transient DefaultConnectorClient client;
    transient ObjectMapper objectMapper;

    @Autowired
    public BrokerUIController(final BrokerService brokerService,
                              final DefaultConnectorClient client,
                              final ObjectMapper objectMapper) {
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
    public ResponseEntity<String> createBroker(final URI brokerUri, final String title) {
        log.info(">> POST /broker brokerUri: " + brokerUri + " title: " + title);

        final var brokerObject = brokerService.createCustomBroker(brokerUri, title);

        if (brokerObject != null) {
            return ResponseEntity.ok(Utility.jsonMessage("message", "Created a new broker with " +
                    "id: " + brokerUri));
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
    public ResponseEntity<String> updateBroker(final URI brokerId, final String title) {
        log.info(">> PUT /broker brokerId: " + brokerId + " title: " + title);

        if (brokerService.updateBroker(brokerId, title)) {
            final var jsonObject = new JSONObject();
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
    public ResponseEntity<String> deleteBroker(final URI brokerUri) {
        log.info(">> DELETE /broker brokerUri " + brokerUri);

        if (brokerService.deleteBroker(brokerUri)) {
            return ResponseEntity.ok(Utility.jsonMessage("message",
                    "Broker with ID: " + brokerUri + " is deleted"));
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
    public ResponseEntity<String> getBroker(final URI brokerId) {
        log.info(">> GET /broker brokerId: " + brokerId);

        final var broker = brokerService.getById(brokerId);

        if (broker != null) {
            try {
                return ResponseEntity.ok(objectMapper.writeValueAsString(broker));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
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
        log.info(">> GET /brokers");

        final var brokers = brokerService.getCustomBrokers();
        try {
            return new ResponseEntity<>(objectMapper.writeValueAsString(brokers), HttpStatus.OK);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
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
        log.info(">> GET /broker/list");

        final var brokerUris = brokerService.getAllBrokerUris();
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
    public ResponseEntity<String> registerConnector(final URI brokerUri) {
        log.info(">> POST /broker/register brokerUri: " + brokerUri);

        final var broker = brokerService.getById(brokerUri);
        final var jsonObject = new JSONObject();
        if (broker != null) {
            try {
                final var response = client.updateAtBroker(brokerUri.toString());
                if (!response.contains("RejectionMessage")) {
                    brokerService.sentSelfDescToBroker(brokerUri);
                    brokerService.setBrokerStatus(brokerUri, BrokerStatus.REGISTERED);
                    jsonObject.put("success", true);
                } else {
                    jsonObject.put("success", false);
                }
                return ResponseEntity.ok(jsonObject.toJSONString());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                jsonObject.put("success", false);
                return ResponseEntity.ok(jsonObject.toJSONString());
            }
        } else {
            return ResponseEntity.badRequest().body("Could not find the broker");
        }
    }

    /**
     * This method removes a connector at a specific broker.
     *
     * @param brokerUri uri of the broker
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> unregisterConnector(final URI brokerUri) {
        log.info(">> POST /broker/unregister brokerUri: " + brokerUri);

        final var broker = brokerService.getById(brokerUri);
        final var jsonObject = new JSONObject();
        if (broker != null) {
            try {
                final var response = client.unregisterAtBroker(brokerUri.toString());
                if (!response.contains("RejectionMessage")) {
                    brokerService.unregisteredAtBroker(brokerUri);
                    brokerService.setBrokerStatus(brokerUri, BrokerStatus.UNREGISTERED);
                    jsonObject.put("success", true);
                } else {
                    jsonObject.put("success", false);
                }
                return ResponseEntity.ok(jsonObject.toJSONString());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                jsonObject.put("success", false);
                return ResponseEntity.ok(jsonObject.toJSONString());
            }
        } else {
            return ResponseEntity.badRequest().body("Could not find the broker");
        }
    }

    /**
     * This method updates a connector at a specific broker.
     *
     * @param brokerUri uri of the broker
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateConnector(final URI brokerUri) {
        log.info(">> POST /broker/update brokerUri: " + brokerUri);

        final var broker = brokerService.getById(brokerUri);
        final var jsonObject = new JSONObject();
        if (broker != null) {
            try {
                final var response = client.updateAtBroker(brokerUri.toString());
                jsonObject.put("success", !response.contains("RejectionMessage"));
                if (!response.contains("RejectionMessage")) {
                    brokerService.sentSelfDescToBroker(brokerUri);
                }
                return ResponseEntity.ok(jsonObject.toJSONString());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return ResponseEntity.badRequest().body("Could not connect to the Connector!");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not find the broker with URI: " + brokerUri);
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
    public ResponseEntity<String> updateResourceAtBroker(final URI brokerUri,
                                                         final URI resourceId) {
        log.info(">> POST /broker/update/resource brokerUri: " + brokerUri + " resourceId: " + resourceId);

        return updateConnector(brokerUri);

//        var broker = brokerService.getById(brokerUri);
//        var jsonObject = new JSONObject();
//        if (broker != null) {
//            try {
//                String response = client.updateResourceAtBroker(brokerUri.toString(), resourceId);
//                if (response.contains("RejectionMessage") || response.equals("Could not load
//                resource.")
//                        || response.equals("The communication with the broker failed.")) {
//                    jsonObject.put("success", false);
//                } else {
//                    brokerService.setResourceAtBroker(brokerUri, resourceId);
//                    jsonObject.put("success", true);
//                }
//                return ResponseEntity.ok(jsonObject.toJSONString());
//            } catch (IOException e) {
//                logger.error(e.getMessage(), e);
//                jsonObject.put("success", false);
//                return ResponseEntity.ok(jsonObject.toJSONString());
//            }
//        } else {
//            return ResponseEntity.badRequest().body("Could not find the broker");
//        }
    }

    /**
     * This method deletes a {@link Resource} at a given broker URI.
     *
     * @param brokerUri  URI of the Broker the connector shall to connect to
     * @param resourceId the ID of the resource that shall be updated in the broker
     * @return HTTP response entity with the response as body string
     */
    @Override
    public ResponseEntity<String> deleteResourceAtBroker(final URI brokerUri,
                                                         final URI resourceId) {
        log.info(">> POST /broker/delete/resource brokerUri: " + brokerUri + " resourceId: " + resourceId);

        final var response = updateConnector(brokerUri);

        if (response.getStatusCode() != HttpStatus.BAD_REQUEST) {
            brokerService.deleteResourceAtBroker(brokerUri, resourceId);
        }

        return response;

//        var broker = brokerService.getById(brokerUri);
//        var jsonObject = new JSONObject();
//        if (broker != null) {
//            try {
//                String response = client.deleteResourceAtBroker(brokerUri.toString(), resourceId);
//                if (response.contains("RejectionMessage") || response.equals("Could not load
//                resource.")
//                        || response.equals("The communication with the broker failed.")) {
//                    jsonObject.put("success", false);
//                } else {
//                    brokerService.deleteResourceAtBroker(brokerUri, resourceId);
//                    jsonObject.put("success", true);
//                }
//                return ResponseEntity.ok(jsonObject.toJSONString());
//            } catch (IOException e) {
//                logger.error(e.getMessage(), e);
//                jsonObject.put("success", false);
//                return ResponseEntity.ok(jsonObject.toJSONString());
//            }
//        } else {
//            return ResponseEntity.badRequest().body("Could not find the broker");
//        }
    }

    /**
     * This method returns the register status for a resource
     *
     * @param resourceId id of the resource
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getRegisterStatusForResource(final URI resourceId) {
        log.info(">> GET /broker/resource/information resourceId: " + resourceId);

        final var jsonObjet = brokerService.getRegisStatusForResource(resourceId);
        if (jsonObjet == null) {
            return ResponseEntity.badRequest().body("Could not get registration status for " +
                    "resource");
        } else {
            return ResponseEntity.ok(jsonObjet.toJSONString());
        }
    }

}
