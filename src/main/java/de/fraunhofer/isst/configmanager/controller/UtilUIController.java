package de.fraunhofer.isst.configmanager.controller;

import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.configmanagement.service.UtilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * The controller class offers the possibilities to provide other api's which could be needed.
 * As an example, enum values are supplied here via an api.
 */
@RestController
@RequestMapping("/api/ui")
@Tag(name = "Utility", description = "Endpoints for other requirements")
public class UtilUIController {

    private final static Logger LOGGER = LoggerFactory.getLogger(UtilUIController.class);

    private final UtilService utilService;
    private final DefaultConnectorClient client;

    @Autowired
    public UtilUIController(UtilService utilService, DefaultConnectorClient client) {
        this.utilService = utilService;
        this.client = client;
    }

    /**
     * This method returns for a given enum name all enum values.
     *
     * @param enumName name of the enum
     * @return enum values as a string
     */
    @GetMapping(value = "/enum/{enumName}")
    @Operation(summary = "Get the specific enum")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully get the enums")})
    public ResponseEntity<String> getSpecificEnum(@PathVariable String enumName) {

        String enums = utilService.getSpecificEnum(enumName);
        if (enums != null) {
            return ResponseEntity.ok(enums);
        } else {
            return ResponseEntity.badRequest().body("Could not get the enums");
        }
    }

    /**
     * This method returns for a given policy the pattern.
     *
     * @param policy string, representing a policy
     * @return pattern of policy
     */
    @PostMapping(value = "/policy-pattern")
    @Operation(summary = "Get pattern of policy")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Successfully get the pattern of policy")})
    public ResponseEntity<String> getPolicyPattern(@RequestBody String policy) {

        String pattern;
        try {
            pattern = client.getPolicyPattern(policy);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to determine policy pattern at the client");
        }
        if (pattern != null) {
            return ResponseEntity.ok(pattern);
        } else {
            return ResponseEntity.badRequest().body("Could not find any pattern for the given policy");
        }
    }
}
