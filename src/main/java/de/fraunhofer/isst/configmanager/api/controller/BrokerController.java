package de.fraunhofer.isst.configmanager.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.isst.configmanager.api.BrokerApi;
import de.fraunhofer.isst.configmanager.api.service.BrokerService;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultBrokerClient;
import de.fraunhofer.isst.configmanager.model.config.BrokerStatus;
import de.fraunhofer.isst.configmanager.util.Utility;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

/**
 * The api class implements the BrokerAPI and offers the possibilities to manage
 * the brokers in the configuration manager.
 */
@Slf4j
@RestController
@RequestMapping("/api/ui")
@Tag(name = "Broker Management", description = "Endpoints for managing the brokers in the configuration manager")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BrokerController implements BrokerApi {

    transient BrokerService brokerService;
    transient DefaultBrokerClient client;
    transient ObjectMapper objectMapper;

    @Autowired
    public BrokerController(final BrokerService brokerService,
                            final DefaultBrokerClient client,
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
        ResponseEntity<String> response;

        final var brokerObject = brokerService.createCustomBroker(brokerUri, title);

        if (brokerObject != null) {
            response = ResponseEntity.ok(Utility.jsonMessage("message", "Created a new broker with id: " + brokerUri));
        } else {
            response = ResponseEntity.badRequest().body("Could not create a broker");
        }

        return response;
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
        ResponseEntity<String> response;

        if (brokerService.updateBroker(brokerId, title)) {
            final var jsonObject = new JSONObject();
            jsonObject.put("message", "Updated the broker");
            jsonObject.put("brokerId", brokerId.toString());
            response = ResponseEntity.ok(jsonObject.toJSONString());
        } else {
            response = ResponseEntity.badRequest().body("Could not update the broker");
        }

        return response;
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
        ResponseEntity<String> response;

        if (brokerService.deleteBroker(brokerUri)) {
            response = ResponseEntity.ok(Utility.jsonMessage("message", "Broker with ID: " + brokerUri + " is deleted"));
        } else {
            response = ResponseEntity.badRequest().body("Could not delete the broker with the id:" + brokerUri);
        }

        return response;
    }

    /**
     * This method returns a list of all brokers as string.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getAllBrokers() {
        log.info(">> GET /brokers");
        ResponseEntity<String> response;

        final var brokers = brokerService.getCustomBrokers();

        try {
            response = new ResponseEntity<>(objectMapper.writeValueAsString(brokers), HttpStatus.OK);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
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
        ResponseEntity<String> response;

        final var broker = brokerService.getById(brokerUri);
        final var jsonObject = new JSONObject();
        final var success = "success";

        if (broker != null) {
            try {
                final var clientResponse = client.updateAtBroker(brokerUri.toString());
                if (clientResponse.isSuccessful()) {
                    brokerService.sentSelfDescToBroker(brokerUri);
                    brokerService.setBrokerStatus(brokerUri, BrokerStatus.REGISTERED);
                    jsonObject.put(success, true);
                } else {
                    jsonObject.put(success, false);
                }
                response = ResponseEntity.ok(jsonObject.toJSONString());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                jsonObject.put(success, false);
                response = ResponseEntity.ok(jsonObject.toJSONString());
            }
        } else {
            response = ResponseEntity.badRequest().body("Could not find the broker");
        }

        return response;
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
        ResponseEntity<String> response;

        final var broker = brokerService.getById(brokerUri);
        final var jsonObject = new JSONObject();
        final var success = "success";

        if (broker != null) {
            try {
                final var clientResponse = client.unregisterAtBroker(brokerUri.toString());
                final var clientResponseString = Objects.requireNonNull(clientResponse.body()).string();
                if (clientResponse.isSuccessful() && !clientResponseString.contains("RejectionMessage")) {
                    brokerService.unregisteredAtBroker(brokerUri);
                    brokerService.setBrokerStatus(brokerUri, BrokerStatus.UNREGISTERED);
                    jsonObject.put(success, true);
                } else {
                    jsonObject.put(success, false);
                }
                response = ResponseEntity.ok(jsonObject.toJSONString());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                jsonObject.put(success, false);
                response = ResponseEntity.ok(jsonObject.toJSONString());
            }
        } else {
            response = ResponseEntity.badRequest().body("Could not find the broker");
        }

        return response;
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
        ResponseEntity<String> response;

        final var broker = brokerService.getById(brokerUri);
        final var jsonObject = new JSONObject();

        if (broker != null) {
            try {
                final var clientResponse = client.updateAtBroker(brokerUri.toString());
                jsonObject.put("success", clientResponse.isSuccessful());
                if (clientResponse.isSuccessful()) {
                    brokerService.sentSelfDescToBroker(brokerUri);
                }
                response = ResponseEntity.ok(jsonObject.toJSONString());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            response = ResponseEntity.badRequest().body("Could not find the broker with URI: " + brokerUri);
        }

        return response;
    }

    /**
     * This method updates a {@link Resource} at a given broker URI.
     *
     * @param brokerUri  URI of the Broker the connector shall to connect to
     * @param resourceId the ID of the resource that shall be updated in the broker
     * @return HTTP response entity with the response as body string
     */
    @Override
    public ResponseEntity<String> updateResourceAtBroker(final URI brokerUri, final URI resourceId) {
        log.info(">> POST /broker/update/resource brokerUri: " + brokerUri + " resourceId: " + resourceId);

//        ResponseEntity<String> response;
//        response = updateConnector(brokerUri);
//
//        return response;

        ResponseEntity<String> response;
        final var broker = brokerService.getById(brokerUri);
        final var jsonObject = new JSONObject();
        if (broker != null) {
            try {
                Response clientResponse = client.updateResourceAtBroker(brokerUri.toString(), resourceId);
                final var clientResponseString = Objects.requireNonNull(clientResponse.body()).string();
                jsonObject.put("response", clientResponseString);
                if (clientResponse.isSuccessful()) {
                    jsonObject.put("success", true);
                    response = ResponseEntity.ok(jsonObject.toJSONString());
                } else {
                    jsonObject.put("success", false);
                    response = ResponseEntity.badRequest().body(jsonObject.toJSONString());
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                jsonObject.put("success", false);
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonObject.toJSONString());
            }
        } else {
            jsonObject.put("message", "Could not find the broker");
            response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonObject.toJSONString());
        }
        return response;
    }

    /**
     * This method deletes a {@link Resource} at a given broker URI.
     *
     * @param brokerUri  URI of the Broker the connector shall to connect to
     * @param resourceId the ID of the resource that shall be updated in the broker
     * @return HTTP response entity with the response as body string
     */
    @Override
    public ResponseEntity<String> deleteResourceAtBroker(final URI brokerUri, final URI resourceId) {
        log.info(">> POST /broker/delete/resource brokerUri: " + brokerUri + " resourceId: " + resourceId);

//        final var response = updateConnector(brokerUri);
//
//        if (response.getStatusCode() != HttpStatus.BAD_REQUEST) {
//            brokerService.deleteResourceAtBroker(brokerUri, resourceId);
//        }
//
//        return response;

        ResponseEntity<String> response;
        final var broker = brokerService.getById(brokerUri);
        final var jsonObject = new JSONObject();
        if (broker != null) {
            try {
                Response clientResponse = client.deleteResourceAtBroker(brokerUri.toString(), resourceId);
                final var clientResponseString = Objects.requireNonNull(clientResponse.body()).string();
                jsonObject.put("response", clientResponseString);
                if (clientResponse.isSuccessful()) {
                    brokerService.deleteResourceAtBroker(brokerUri, resourceId);
                    jsonObject.put("success", true);
                    response = ResponseEntity.ok(jsonObject.toJSONString());
                } else {
                    log.info("Deleting resource: {} at broker: {} failed", resourceId, brokerUri);
                    jsonObject.put("success", false);
                    response = ResponseEntity.badRequest().body(jsonObject.toJSONString());
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                jsonObject.put("success", false);
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonObject.toJSONString());
            }
        } else {
            response = ResponseEntity.status(HttpStatus.NOT_FOUND).body("Could not find the broker");
        }
        return response;
    }

    /**
     * This method returns the register status for a resource.
     *
     * @param resourceId id of the resource
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getRegisterStatusForResource(final URI resourceId) {
        log.info(">> GET /broker/resource/information resourceId: " + resourceId);
        ResponseEntity<String> response;

        final var jsonObject = brokerService.getRegisStatusForResource(resourceId);

        if (jsonObject == null) {
            response = ResponseEntity.ok(new JSONArray().toJSONString());
        } else {
            response = ResponseEntity.ok(jsonObject.toJSONString());
        }

        return response;
    }

}
