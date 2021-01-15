package de.fraunhofer.isst.configmanager.controller;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.communication.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.configmanagement.service.ResourceService;
import de.fraunhofer.isst.configmanager.util.CalenderUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * The controller class implements the ResourceUIApi and offers the possibilities to manage
 * the resources in the configurationmanager.
 */
@RestController
@RequestMapping("/api/ui")
@Tag(name = "Resource Management", description = "Endpoints for managing the resource in the configuration manager")
public class ResourceUIController implements ResourceUIApi {

    private final static Logger logger = LoggerFactory.getLogger(ResourceUIController.class);

    private final ResourceService resourceService;
    private final ConfigModelService configModelService;
    private final DefaultConnectorClient client;
    private final Serializer serializer;

    @Autowired
    public ResourceUIController(ResourceService resourceService, ConfigModelService configModelService,
                                DefaultConnectorClient client, Serializer serializer) {
        this.resourceService = resourceService;
        this.configModelService = configModelService;
        this.client = client;
        this.serializer = serializer;
    }

    /**
     * This method returns a resource from the connector with the given paraemter.
     *
     * @param resourceId id of the resource
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getResource(URI resourceId) {

        Resource resource = resourceService.getResource(resourceId);

        if (resource != null) {
            try {
                return ResponseEntity.ok(serializer.serialize(resource));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not serialize resource!");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not determine the resource");
        }
    }

    /**
     * This method returns all resources from the connector.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getResources() {

        if (configModelService.getConfigModel() == null
                || configModelService.getConfigModel().getConnectorDescription() == null
                || configModelService.getConfigModel().getConnectorDescription().getResourceCatalog() == null) {
            return ResponseEntity.ok(new JSONArray().toJSONString());
        }

        ArrayList<Resource> resources = resourceService.getResources();
        if (resources != null) {
            try {
                return ResponseEntity.ok(serializer.serialize(resources));
            } catch (IOException e) {
                logger.error(e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not serialize resources!");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not determine the resources");
        }
    }

    /**
     * This method returns a specific resource in JSON format.
     *
     * @param resourceId if of the resource
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getResourceInJson(URI resourceId) {

        Resource resource = resourceService.getResource(resourceId);

        JSONObject resourceJson = new JSONObject();
        resourceJson.put("title", resource.getTitle().get(0).getValue());
        resourceJson.put("description", resource.getDescription().get(0).getValue());
        resourceJson.put("keyword", resource.getKeyword());
        resourceJson.put("version", resource.getVersion());
        resourceJson.put("standardlicense", resource.getStandardLicense().toString());
        resourceJson.put("publisher", resource.getPublisher().toString());

        return ResponseEntity.ok(resourceJson.toJSONString());
    }

    /**
     * This method deletes the resource from the connector and the app route with the given parameter.
     * If both are deleted the dataspace connector is informed about the change.
     *
     * @param resourceId id of the resource
     * @return http response from the target connector
     */
    @Override
    public ResponseEntity<String> deleteResource(URI resourceId) {

        boolean deleted = configModelService.getConfigModel().getConnectorDescription().getResourceCatalog()
                .stream()
                .map(ResourceCatalog::getOfferedResource)
                .map(resources -> resources.removeIf(resource -> resource.getId().equals(resourceId)))
                .reduce(false, (a, b) -> a || b);

        deleted |= configModelService.getConfigModel().getAppRoute().stream()
                .map(AppRoute::getAppRouteOutput)
                .map(resources -> resources.removeIf(resource -> resource.getId().equals(resourceId)))
                .reduce(false, (a, b) -> a || b);

        if (deleted) {
            try {
                var response = client.deleteResource(resourceId);
                var jsonObject = new JSONObject();
                jsonObject.put("connectorResponse", response);
                jsonObject.put("resourceID", resourceId.toString());
                return ResponseEntity.ok(jsonObject.toJSONString());
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.badRequest().body("Could not send delete request to connector");
            }
        } else {
            return ResponseEntity.badRequest().body("Could not delete the resource");
        }
    }

    /**
     * This method creates a resource with the given parameters. The special feature here is that the created resource
     * is included once in the app route and once in the resource catalog of the connector.
     *
     * @param routeId         id of the route
     * @param title           title of the resource
     * @param description     description of the resource
     * @param language        language of the resource
     * @param keywords        keywords for the resource
     * @param version         version of the resource
     * @param standardlicense standard license for the resource
     * @param publisher       the publisher of the resource
     * @param brokerList      a possible list of brokers
     * @return response from the target connector
     */
    @Override
    public ResponseEntity<String> createResource(URI routeId,
                                                 String title,
                                                 String description,
                                                 String language,
                                                 ArrayList<String> keywords,
                                                 String version,
                                                 String standardlicense,
                                                 String publisher,
                                                 List<URI> brokerList) {

        ArrayList<TypedLiteral> keys = new ArrayList<>();
        for (String keyword : keywords) {
            keys.add(new TypedLiteral(keyword));
        }

        // Create the resource with the given parameters
        Resource resource = new ResourceBuilder()
                ._title_(Util.asList(new TypedLiteral(title)))
                ._description_(Util.asList(new TypedLiteral(description)))
                ._language_(Util.asList(Language.valueOf(language)))
                ._keyword_(keys)
                ._version_(version)
                ._standardLicense_(URI.create(standardlicense))
                ._publisher_(URI.create(publisher))
                ._created_(CalenderUtil.getGregorianNow())
                ._modified_(CalenderUtil.getGregorianNow())
                .build();
        resource.setProperty("brokerList", brokerList);

        var configModulIMpl = (ConfigurationModelImpl) configModelService.getConfigModel();

        // Set Resource in AppRoute
        if (configModulIMpl.getAppRoute() == null) {
            // Create new list for app route
            configModulIMpl.setAppRoute(new ArrayList<>());

            AppRoute appRoute = new AppRouteBuilder(routeId)
                    ._appRouteOutput_(new ArrayList<>())
                    ._routeDeployMethod_("custom")
                    .build();

            ArrayList<AppRoute> appRoutes = (ArrayList<AppRoute>) configModulIMpl.getAppRoute();
            appRoutes.add(appRoute);
        }
        var appRoutes = (ArrayList<AppRoute>) configModulIMpl.getAppRoute();
        // Check if app route exists in the list
        var routeExists = appRoutes.stream().map(AppRoute::getId).anyMatch(r -> r.equals(routeId));
        if (!routeExists) {
            // Create new app route and it to the list of app routes
            appRoutes.add(new AppRouteBuilder(routeId)._appRouteOutput_(new ArrayList<>())
                    ._routeDeployMethod_("custom").build());
        }
        // Finally find the correct app route, check if the app route output is empty or not and then insert the created
        // resource to the app route.
        for (AppRoute appRoute : appRoutes) {
            if (routeId.equals(appRoute.getId())) {
                routeExists = true;
                var appRouteImpl = (AppRouteImpl) appRoute;
                if (appRouteImpl.getAppRouteOutput() == null) {
                    appRouteImpl.setAppRouteOutput(new ArrayList<>());
                }
                ArrayList<Resource> resources = (ArrayList<Resource>) appRouteImpl.getAppRouteOutput();
                resources.add(resource);
            }
        }

        // Set Resource in Connector
        if (configModulIMpl.getConnectorDescription() == null) {
            // Default connector is created, if no connector description can be found
            configModulIMpl.setConnectorDescription(
                    new BaseConnectorBuilder()
                            ._curator_(URI.create("http://example"))
                            ._maintainer_(URI.create("http://example"))
                            ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                            ._inboundModelVersion_(new ArrayList<>(List.of("4.0.0")))
                            ._outboundModelVersion_("4.0.0")
                            .build()
            );
        }
        var connectorImpl = (BaseConnectorImpl) configModulIMpl.getConnectorDescription();
        if (connectorImpl.getResourceCatalog() == null) {
            // New resource catalog will be set, if it is not existing
            connectorImpl.setResourceCatalog(new ArrayList<>());
        }
        var oldCatalog = configModulIMpl.getConnectorDescription().getResourceCatalog()
                .stream().findAny();
        ArrayList<Resource> resources;
        if (oldCatalog.isPresent()) {
            //get the offers as List of Resources instead of Capture of ? extends Resource
            resources = (ArrayList<Resource>) oldCatalog.get().getOfferedResource();
            //add the resource to the list
            resources.add(resource);
        } else {
            // Resource will be added to the list of offered resources and the resource catalog of the connector will be
            // updated.
            resources = new ArrayList<>();
            resources.add(resource);
            var catalog = new ResourceCatalogBuilder()
                    ._offeredResource_(resources)
                    ._requestedResource_(new ArrayList<>()).build();
            connectorImpl.setResourceCatalog(Util.asList(catalog));
        }
        // Save and register the resource in the dataspace connector
        try {
            configModelService.saveState();
            var response = client.registerResource(resource);
            var jsonObject = new JSONObject();
            jsonObject.put("connectorResponse", response);
            jsonObject.put("resourceID", resource.getId().toString());
            return ResponseEntity.ok(jsonObject.toJSONString());
        } catch (IOException e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body("Could not register resource at connector");
        }
    }

    /**
     * This method updates a resource with the given parameters. The special feature here is that the resource
     * is updated once in the app route and once in the resource catalog of the connector.
     *
     * @param routeId         id of the route
     * @param resourceId      id of the resource
     * @param title           title of the resource
     * @param description     description of the resource
     * @param language        language of the resource
     * @param keywords        keywords for the resource
     * @param version         version of the resource
     * @param standardlicense standard license for the resource
     * @param publisher       the publisher of the resource
     * @param brokerList      a possible list of brokers
     * @return response from the target connector
     */
    @Override
    public ResponseEntity<String> updateResource(URI routeId, URI resourceId, String title, String description,
                                                 String language, ArrayList<String> keywords, String version,
                                                 String standardlicense, String publisher, List<URI> brokerList) {

        // Update resource in resource catalog
        ResourceImpl resourceImpl = null;
        var configModelImpl = (ConfigurationModelImpl) configModelService.getConfigModel();
        var catalogs = configModelImpl.getConnectorDescription().getResourceCatalog();
        // Find the correct resource in the resource catalog of the connector
        for (var catalog : catalogs) {
            for (var offerdResource : catalog.getOfferedResource()) {
                if (resourceId.equals(offerdResource.getId())) {
                    resourceImpl = (ResourceImpl) offerdResource;
                    break;
                }
            }
        }

        // Update the resource with the given parameters and optionally a broker list is set
        if (resourceImpl != null) {
            resourceService.updateResourceContent(title, description, language, keywords, version, standardlicense,
                    publisher, resourceImpl);
            resourceImpl.setProperty("brokerList", brokerList);
        }

        // Update resource in app route
        var appRoute = configModelService.getConfigModel().getAppRoute()
                .stream().filter(appRoute1 -> appRoute1.getId().equals(routeId)).findAny().orElse(null);

        ResourceImpl appRouteResource = null;
        if (appRoute != null && appRoute.getAppRouteOutput() != null) {
            appRouteResource = (ResourceImpl) appRoute.getAppRouteOutput()
                    .stream().filter(resource -> resource.getId().equals(resourceId)).findAny().orElse(null);
        }

        // Update the resource in the app route and optionally a broker list is set
        if (appRouteResource != null) {
            resourceService.updateResourceContent(title, description, language, keywords, version, standardlicense,
                    publisher, appRouteResource);
            appRouteResource.setProperty("brokerList", brokerList);
        }

        // Save the updated resource and update the resource in the dataspace connector
        try {
            configModelService.saveState();
            var response = client.updateResource(resourceId, resourceImpl);
            var jsonObject = new JSONObject();
            jsonObject.put("connectorResponse", response);
            jsonObject.put("resourceID", resourceId.toString());
            return ResponseEntity.ok(jsonObject.toJSONString());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
