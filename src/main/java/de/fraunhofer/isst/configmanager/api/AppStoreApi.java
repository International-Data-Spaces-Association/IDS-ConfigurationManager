package de.fraunhofer.isst.configmanager.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface AppStoreApi {

    @GetMapping(value = "/appstore/images")
    @Operation(summary = "Returns a list of docker images")
    @ApiResponse(responseCode = "200", description = "Returned a list of docker images")
    ResponseEntity<String> getImages();

    @PostMapping(value = "/appstore/images/pull")
    @Operation(summary = "Pulls a docker image from the registry")
    @ApiResponse(responseCode = "200", description = "Pulled successfully docker image from registry")
    @ApiResponse(responseCode = "400", description = "Failed to pull docker image from registry")
    ResponseEntity<String> pullImage(@RequestParam("String imageName") String imageName);

    @PostMapping(value = "/appstore/images/push")
    @Operation(summary = "Pushs a docker image to the registry")
    @ApiResponse(responseCode = "200", description = "Pushed image successfully to registry")
    ResponseEntity<String> pushImage(@RequestParam("String imageName") String imageName);

    @DeleteMapping(value = "/appstore/images/delete")
    @Operation(summary = "Deletes a docker image from the registry")
    @ApiResponse(responseCode = "200", description = "Deleted image successfully to registry")
    ResponseEntity<String> removeImage(@RequestParam("String imageID") String imageID);

    @GetMapping(value = "/appstore/containers")
    @Operation(summary = "Returns a list of docker containers")
    @ApiResponse(responseCode = "200", description = "Returned a list of docker containers")
    ResponseEntity<String> getContainers();

    @PostMapping(value = "/appstore/containers/build")
    @Operation(summary = "Creates a docker container with given image")
    @ApiResponse(responseCode = "200", description = "Created successfully a docker container")
    ResponseEntity<String> buildContainer(@RequestParam("String imageName") String imageName);

    @PostMapping(value = "/appstore/containers/start")
    @Operation(summary = "Starts a docker container")
    @ApiResponse(responseCode = "200", description = "Started a docker container")
    ResponseEntity<String> startContainer(@RequestParam("String containerID") String containerID);

    @PostMapping(value = "/appstore/containers/stop")
    @Operation(summary = "Stops a docker container")
    @ApiResponse(responseCode = "200", description = "Stopped a docker container")
    ResponseEntity<String> stopContainer(@RequestParam("String containerID") String containerID);

    @DeleteMapping(value = "/appstore/containers/delete")
    @Operation(summary = "Removes a docker container")
    @ApiResponse(responseCode = "200", description = "Removed a docker container")
    ResponseEntity<String> removeContainer(@RequestParam("String containerID") String containerID);
}
