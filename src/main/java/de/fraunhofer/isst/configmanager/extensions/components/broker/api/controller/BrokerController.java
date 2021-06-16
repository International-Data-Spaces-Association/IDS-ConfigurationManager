package de.fraunhofer.isst.configmanager.extensions.components.broker.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.isst.configmanager.extensions.components.broker.api.BrokerApi;
import de.fraunhofer.isst.configmanager.extensions.components.broker.api.service.BrokerService;
import de.fraunhofer.isst.configmanager.extensions.apps.util.AppEndpointBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
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

/**
 * The api class implements the BrokerAPI and offers the possibilities to manage
 * the brokers in the configuration manager.
 */
@Slf4j
@RestController
@RequestMapping("/api/ui")
@Tag(name = "Extension: Component Metadata Broker")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BrokerController implements BrokerApi {
    transient BrokerService brokerService;
    transient ObjectMapper objectMapper;

    /**
     * This method creates a broker with the given parameters.
     *
     * @param brokerUri uri of the broker
     * @param title     title of the broker
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> createBroker(final URI brokerUri, final String title) {
        if (log.isInfoEnabled()) {
            log.info(">> POST /broker brokerUri: " + brokerUri + " title: " + title);
        }

        brokerService.createCustomBroker(brokerUri, title);
        return ResponseEntity.ok(AppEndpointBuilder.jsonMessage("message", "Created a new broker with id: " + brokerUri));
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
        if (log.isInfoEnabled()) {
            log.info(">> PUT /broker brokerId: " + brokerId + " title: " + title);
        }
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
        if (log.isInfoEnabled()) {
            log.info(">> DELETE /broker brokerUri " + brokerUri);
        }
        ResponseEntity<String> response;

        if (brokerService.deleteBroker(brokerUri)) {
            response = ResponseEntity.ok(AppEndpointBuilder.jsonMessage("message", "Broker with ID: " + brokerUri + " is deleted"));
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
        if (log.isInfoEnabled()) {
            log.info(">> GET /brokers");
        }
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
}
