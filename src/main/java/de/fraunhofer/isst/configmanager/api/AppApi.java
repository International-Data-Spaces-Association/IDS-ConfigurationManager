package de.fraunhofer.isst.configmanager.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface AppApi {
    @GetMapping(value = "/apps", produces = "application/ld+json")
    @Operation(summary = "Returns a list of all apps")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returned a list of all apps")})
    ResponseEntity<String> getApps();

    @GetMapping(value = "/app", produces = "application/ld+json")
    @Operation(summary = "Return an app")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Returned an app")})
    ResponseEntity<String> getApp(@RequestParam(value = "id") String id);
}
