package de.fraunhofer.isst.configmanager.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

public interface AppApi {
    @GetMapping(value = "/apps", produces = "application/ld+json")
    @Operation(summary = "Returns a list of all apps")
    @ApiResponse(responseCode = "200", description = "Returns a list of custom apps")
    @ApiResponse(responseCode = "400", description = "Could not find customs apps")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    ResponseEntity<String> getApps();
}
