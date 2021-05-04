package de.fraunhofer.isst.configmanager.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface ExampleDemoApi {

    @GetMapping(value = "/appstore/images")
    ResponseEntity<String> getImages();

    @PostMapping(value = "/appstore/images/pull")
    ResponseEntity<String> getImage(@RequestParam("String imageName") String imageName);

    @PostMapping(value = "/appstore/images/push")
    ResponseEntity<String> pushImage(@RequestParam("String imageName") String imageName);

    @GetMapping(value = "/appstore/containers")
    ResponseEntity<String> getContainers();

    @PostMapping(value = "/appstore/containers/build")
    ResponseEntity<String> buildContainer(@RequestParam("String imageName") String imageName);

    @PostMapping(value = "/appstore/containers/start")
    ResponseEntity<String> startContainer(@RequestParam("String containerID") String containerID);

    @PostMapping(value = "/appstore/containers/stop")
    ResponseEntity<String> stopContainer(@RequestParam("String containerID") String containerID);

}
