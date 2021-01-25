//package de.fraunhofer.isst.configmanager.controller;
//
//import de.fraunhofer.iais.eis.Resource;
//import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
//import de.fraunhofer.isst.configmanager.configmanagement.service.AppRouteService;
//import de.fraunhofer.isst.configmanager.util.Utility;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.io.IOException;
//import java.net.URI;
//
//@RestController
//@RequestMapping("/api/ui")
//@Tag(name = "App Route: Output Management", description = "Endpoints for managing route outputs in the configuration manager")
//public class AppRouteOutputUIController implements AppRouteOutputApi {
//
//    private final AppRouteService appRouteService;
//    private final Serializer serializer;
//
//    @Autowired
//    public AppRouteOutputUIController(AppRouteService appRouteService, Serializer serializer) {
//        this.appRouteService = appRouteService;
//        this.serializer = serializer;
//    }
//
//    @Override
//    public ResponseEntity<String> getResourceFromAppRouteOutput(URI routeId, URI resourceId) {
//        Resource resource = appRouteService.getResourceFromAppRouteOutput(routeId, resourceId);
//
//        if (resource != null) {
//            try {
//                return ResponseEntity.ok(serializer.serialize(resource));
//            } catch (IOException e) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not serialize the resource");
//            }
//        } else {
//            return ResponseEntity.badRequest().body("Could not get resource with id: " + resource.getId());
//        }
//    }
//
//    @Override
//    public ResponseEntity<String> getResourceFromSubrouteOutput(URI routeId, URI routeStepId, URI resourceId) {
//
//        Resource resource = appRouteService.getResourceFromSubrouteOutput(routeId, routeStepId, resourceId);
//
//        if (resource != null) {
//            try {
//                return ResponseEntity.ok(serializer.serialize(resource));
//            } catch (IOException e) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not serialize the resource");
//            }
//        } else {
//            return ResponseEntity.badRequest().body("Could not get resource with id: " + resource.getId());
//        }
//    }
//
//    @Override
//    public ResponseEntity<String> deleteResourceFromAppRouteOutput(URI routeId, URI resourceId) {
//
//        boolean deleted = appRouteService.deleteResourceFromAppRouteOutput(routeId, resourceId);
//        if (deleted) {
//            return ResponseEntity.ok(Utility.jsonMessage("message", "Deleted the resource with id: " + resourceId));
//        } else {
//            return ResponseEntity.badRequest().body("Could not delete the resource with id: " + resourceId);
//        }
//
//    }
//
//    @Override
//    public ResponseEntity<String> deleteResourceFromSubrouteOutput(URI routeId, URI routeStepId, URI resourceId) {
//
//        boolean deleted = appRouteService.deleteResourceFromSubrouteOutput(routeId, routeStepId, resourceId);
//        if (deleted) {
//            return ResponseEntity.ok(Utility.jsonMessage("message", "Deleted the resource with id: " + resourceId));
//        } else {
//            return ResponseEntity.badRequest().body("Could not delete the resource with id: " + resourceId);
//        }
//    }
//}
