package de.fraunhofer.isst.configmanager.controller;

import de.fraunhofer.isst.configmanager.configmanagement.service.UtilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The controller class offers the possibilities to provide other api's which could be needed.
 * As an example, enum values are supplied here via an api.
 */
@RestController
@RequestMapping("/api/ui")
@Tag(name = "Utility", description = "Endpoints for other requirements")
public class UtilUIController {

    private final UtilService utilService;

    @Autowired
    public UtilUIController(UtilService utilService) {
        this.utilService = utilService;
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
}
