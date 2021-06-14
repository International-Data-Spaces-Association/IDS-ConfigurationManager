package de.fraunhofer.isst.configmanager.api.controller;

import de.fraunhofer.isst.configmanager.api.UtilApi;
import de.fraunhofer.isst.configmanager.api.service.UtilService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * The api class offers the possibilities to provide other api's which could be needed.
 * As an example, enum values are supplied here via an api.
 */
@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Tag(name = "Util Management", description = "Endpoints for managing utility")
public class UtilController implements UtilApi {

    private final transient UtilService utilService;

    /**
     * This method returns for a given enum name all enum values.
     *
     * @param enumName name of the enum
     * @return enum values as a string
     */
    @Override
    public ResponseEntity<String> getSpecificEnum(final String enumName) {
        if (log.isInfoEnabled()) {
            log.info(">> GET /api/ui/enum " + enumName);
        }
        ResponseEntity<String> response;

        final var enums = utilService.getSpecificEnum(enumName);

        if (enums != null) {
            response = ResponseEntity.ok(enums);
        } else {
            response = ResponseEntity.badRequest().body("Could not get the enums");
        }

        return response;
    }
}
