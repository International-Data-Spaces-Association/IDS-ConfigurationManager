package de.fraunhofer.isst.configmanager.extensions.gui.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface GuiUtilApi {
    @GetMapping(value = "/api/ui/enum/{enumName}")
    @Operation(summary = "Get the specific enum")
    @ApiResponse(responseCode = "200", description = "Successfully get the enums")
    @ApiResponse(responseCode = "400", description = "Can not find the enums")
    ResponseEntity<String> getSpecificEnum(@PathVariable String enumName);
}
