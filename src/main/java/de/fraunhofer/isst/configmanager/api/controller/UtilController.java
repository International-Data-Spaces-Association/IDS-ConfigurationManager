package de.fraunhofer.isst.configmanager.api.controller;

import de.fraunhofer.isst.configmanager.api.UtilApi;
import de.fraunhofer.isst.configmanager.api.service.UtilService;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultConnectorClient;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * The api class offers the possibilities to provide other api's which could be needed.
 * As an example, enum values are supplied here via an api.
 */
@Slf4j
@RestController
@Tag(name = "Util Management", description = "Endpoints for managing utility")
public class UtilController implements UtilApi {

    private final transient UtilService utilService;
    private final transient DefaultConnectorClient client;

    @Autowired
    public UtilController(final UtilService utilService, final DefaultConnectorClient client) {
        this.utilService = utilService;
        this.client = client;
    }

    /**
     * This method returns for a given enum name all enum values.
     *
     * @param enumName name of the enum
     * @return enum values as a string
     */
    @Override
    public ResponseEntity<String> getSpecificEnum(final String enumName) {
        log.info(">> GET /api/ui/enum " + enumName);
        ResponseEntity<String> response;

        final var enums = utilService.getSpecificEnum(enumName);

        if (enums != null) {
            response = ResponseEntity.ok(enums);
        } else {
            response = ResponseEntity.badRequest().body("Could not get the enums");
        }

        return response;
    }

    /**
     * This method returns for a given policy the pattern.
     *
     * @param policy string, representing a policy
     * @return pattern of policy
     */
    @Override
    public ResponseEntity<String> getPolicyPattern(final String policy) {
        log.info(">> GET /api/ui/policy-pattern " + policy);
        ResponseEntity<String> response;

        String pattern;
        try {
            pattern = client.getPolicyPattern(policy);

            if (pattern != null) {
                response = ResponseEntity.ok(pattern);
            } else {
                response = ResponseEntity.badRequest().body("Could not find any pattern for the given policy");
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            response = ResponseEntity.badRequest().body("Failed to determine policy pattern at the client");
        }

        return response;
    }
}
