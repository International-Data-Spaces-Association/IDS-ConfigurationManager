package de.fraunhofer.isst.configmanager.controller;


import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.configmanagement.service.AppRouteService;
import de.fraunhofer.isst.configmanager.configmanagement.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.configmanagement.service.RepresentationEndpointService;
import de.fraunhofer.isst.configmanager.configmanagement.service.UtilService;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * The controller class implements the EndpointUIApi and offers the possibilities to manage
 * the endpoints in the configuration manager.
 */
@RestController
@RequestMapping("/api/ui")
@Tag(name = "Endpoints Management", description = "Different endpoint types can be managed here")
public class EndpointUIController implements EndpointUIApi {

    private final static Logger logger = LoggerFactory.getLogger(EndpointUIController.class);

    private final Serializer serializer;
    private final ConfigModelService configModelService;
    private final UtilService utilService;
    private final RepresentationEndpointService representationEndpointService;
    private final AppRouteService appRouteService;

    @Autowired
    public EndpointUIController(Serializer serializer,
                                ConfigModelService configModelService,
                                UtilService utilService,
                                RepresentationEndpointService representationEndpointService,
                                AppRouteService appRouteService) {
        this.serializer = serializer;
        this.configModelService = configModelService;
        this.utilService = utilService;
        this.representationEndpointService = representationEndpointService;
        this.appRouteService = appRouteService;
    }

    /**
     * This method returns the app route endpoint with the given parameters.
     *
     * @param routeId    id of the app route
     * @param endpointId if of the endpoint
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getGenericEndpoint(URI routeId, URI endpointId) {

        var route = configModelService.getConfigModel().getAppRoute()
                .stream()
                .filter(appRoute -> appRoute.getId().equals(routeId))
                .findAny().orElse(null);

        if (route != null) {
            var appRouteEndpoint = route.getAppRouteStart()
                    .stream()
                    .filter(endpoint -> endpoint.getId().equals(endpointId))
                    .findAny().orElse(null);
            if (appRouteEndpoint != null) {
                try {
                    return ResponseEntity.ok(serializer.serialize(appRouteEndpoint));
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return ResponseEntity.badRequest().body("Could not get route endpoint");
    }

    /**
     * This method returns the app route endpoint in JSON format with the given parameters.
     *
     * @param routeId    id of the app route
     * @param endpointId id of the endpoint
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getGenericEndpointJson(URI routeId, URI endpointId) {
        var route = configModelService.getConfigModel().getAppRoute()
                .stream()
                .filter(appRoute -> appRoute.getId().equals(routeId))
                .findAny().orElse(null);

        if (route != null) {
            var appRouteEndpoint = route.getAppRouteStart()
                    .stream()
                    .filter(endpoint -> endpoint.getId().equals(endpointId))
                    .findAny().orElse(null);
            if (appRouteEndpoint != null) {

                GenericEndpoint genericEndpoint = (GenericEndpoint) route.getAppRouteStart().get(0);

                JSONObject endpoinJson = new JSONObject();
                endpoinJson.put("accessUrl", genericEndpoint.getAccessURL().toString());
                endpoinJson.put("username", genericEndpoint.getGenericEndpointAuthentication().getAuthUsername());
                endpoinJson.put("password", genericEndpoint.getGenericEndpointAuthentication().getAuthPassword());

                return ResponseEntity.ok(endpoinJson.toJSONString());
            }
        }
        return ResponseEntity.badRequest().body("Could not get route endpoint");
    }

    /**
     * This method returns all connector endpoints.
     *
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getConnectorEndpoints() {

        if (configModelService.getConfigModel().getConnectorDescription() == null) {
            return ResponseEntity.badRequest().body("Could not get the connector");
        }
        Connector connector = configModelService.getConfigModel().getConnectorDescription();
        if (connector.getHasEndpoint() == null) {
            return ResponseEntity.badRequest().body("Could not find any connector endpoints");
        } else {
            try {
                return ResponseEntity.ok(serializer.serialize(connector.getHasEndpoint()));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems while serializing");
            }
        }

    }

    /**
     * This method returns a specific connector endpoint.
     *
     * @param connectorEndpointId id of the connector endpoint
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> getConnectorEndpoint(URI connectorEndpointId) {
        if (configModelService.getConfigModel().getConnectorDescription() == null) {
            return ResponseEntity.badRequest().body("Could not get the connector");
        }
        Connector connector = configModelService.getConfigModel().getConnectorDescription();
        if (connector.getHasEndpoint() == null) {
            return ResponseEntity.badRequest().body("Could not find any connector endpoints");
        } else {
            ConnectorEndpoint connectorEndpoint = connector.getHasEndpoint()
                    .stream()
                    .filter(connectorEndpoint1 -> connectorEndpoint1.getId().equals(connectorEndpointId))
                    .findAny().orElse(null);
            if (connectorEndpoint != null) {
                try {
                    return ResponseEntity.ok(serializer.serialize(connectorEndpoint));
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Problems while serializing");
                }
            }
        }
        return ResponseEntity.badRequest().body("Could not find any connector endpoint with id: " + connectorEndpointId);
    }

    /**
     * This method creates a connector endpoint with given parameters.
     *
     * @param accessUrl access url of the endpoint
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> createConnectorEndpoint(String accessUrl) {

        var configModelImpl = (ConfigurationModelImpl) configModelService.getConfigModel();
        var baseConnector = (BaseConnectorImpl) configModelImpl.getConnectorDescription();
        if (baseConnector.getHasEndpoint() == null) {
            baseConnector.setHasEndpoint(new ArrayList<>());
        }
        ArrayList<ConnectorEndpoint> connectorEndpoints = (ArrayList<ConnectorEndpoint>) baseConnector.getHasEndpoint();
        // Create Connector Endpoint
        ConnectorEndpoint connectorEndpoint = new ConnectorEndpointBuilder()._accessURL_(URI.create(accessUrl)).build();
        // Add Connector Endpoint in Connector
        connectorEndpoints.add(connectorEndpoint);

        configModelService.saveState();

        var jsonObject = new JSONObject();
        jsonObject.put("connectorEndpointId", connectorEndpoint.getId().toString());
        jsonObject.put("message", "Created a new connector endpoint for the connector");
        return ResponseEntity.ok(jsonObject.toJSONString());
    }

    /**
     * This method creates an app route endpoint with the given parameters.
     *
     * @param routeId   if of the app route
     * @param accessUrl the access url of the endpoint
     * @param username  username for the authentication
     * @param password  password for the authentication
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> createGenericEndpoint(URI routeId, String accessUrl,
                                                        String username, String password) {

        var configModelImpl = (ConfigurationModelImpl) configModelService.getConfigModel();

        if (configModelImpl.getAppRoute() == null) {
            configModelImpl.setAppRoute(new ArrayList<>());
        }
        ArrayList<AppRoute> appRoutes = (ArrayList<AppRoute>) configModelImpl.getAppRoute();

        if (routeId == null) {
            // Creates some route if routeID is null
            AppRoute appRoute = new AppRouteBuilder()._routeDeployMethod_("custom")
                    ._appRouteEnd_(Util.asList(new EndpointBuilder()._accessURL_(URI.create("http://api/ids/data")).build()))
                    ._appRouteStart_(Util.asList(new GenericEndpointBuilder()
                            ._accessURL_(URI.create(accessUrl))
                            ._genericEndpointAuthentication_(
                                    new BasicAuthenticationBuilder()
                                            ._authUsername_(username)
                                            ._authPassword_(password).build()).build()))
                    ._appRouteBroker_(new ArrayList<>())
                    ._appRouteOutput_(new ArrayList<>()).build();
            appRoutes.add(appRoute);
            configModelImpl.setAppRoute(appRoutes);
            configModelService.saveState();

            routeId = appRoute.getId();
            return ResponseEntity.ok(String.format("{\"msg\": \"Successfully created endpoint!\",\"routeID\":\"%s\", \"endpointId\":\"%s\"}",
                    routeId, appRoute.getAppRouteStart().get(0).getId()));
        } else {
            URI finalRouteId = routeId;
            var routeImpl = (AppRouteImpl) configModelImpl.getAppRoute()
                    .stream()
                    .filter(appRoute -> appRoute.getId().equals(finalRouteId))
                    .findAny().orElse(null);

            if (routeImpl != null) {
                if (routeImpl.getAppRouteEnd() == null) {
                    routeImpl.setAppRouteEnd(Util.asList(new EndpointBuilder()._accessURL_(URI.create("http://api/ids/data")).build()));
                }
                if (routeImpl.getAppRouteStart() == null) {
                    routeImpl.setAppRouteStart(new ArrayList<>());
                }
                if (routeImpl.getAppRouteOutput() == null) {
                    routeImpl.setAppRouteOutput(new ArrayList<>());
                }
                if (routeImpl.getAppRouteBroker() == null) {
                    routeImpl.setAppRouteBroker(new ArrayList<>());
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"msg\":\"could not find route with given ID!\"}");
            }
            ArrayList<Endpoint> endpoints = (ArrayList<Endpoint>) routeImpl.getAppRouteStart();
            GenericEndpoint genericEndpoint = new GenericEndpointBuilder()
                    ._accessURL_(URI.create(accessUrl))
                    ._genericEndpointAuthentication_(new BasicAuthenticationBuilder()
                            ._authUsername_(username)._authPassword_(password).build()).build();
            endpoints.add(genericEndpoint);
            routeImpl.setAppRouteStart(endpoints);
            configModelService.saveState();
            var endpointId = genericEndpoint.getId();
            var jsonObject = new JSONObject();
            jsonObject.put("message", "Successfully created endpoint!");
            jsonObject.put("routeId", routeId.toString());
            jsonObject.put("endpointId", endpointId.toString());
            return ResponseEntity.ok(jsonObject.toJSONString());
        }
    }

    /**
     * This method updates the app route endpoint with the given parameters
     *
     * @param routeId    id of the app route
     * @param endpointId if of the endpoint
     * @param accessUrl  the access url of the endpoint
     * @param username   username from the authentication
     * @param password   password from the authentication
     * @return a suitable http response depending on success
     */
    @Override
    public ResponseEntity<String> updateGenericEndpoint(URI routeId, URI endpointId, String accessUrl, String username,
                                                        String password) {
        var routeImpl = (AppRouteImpl) configModelService.getConfigModel().getAppRoute()
                .stream()
                .filter(appRoute -> appRoute.getId().equals(routeId))
                .findAny().orElse(null);

        if (routeImpl != null) {
            var appRouteEndpointImpl = (GenericEndpointImpl) routeImpl.getAppRouteStart()
                    .stream()
                    .filter(endpoint -> endpoint.getId().equals(endpointId))
                    .findAny().orElse(null);

            if (appRouteEndpointImpl != null) {
                BasicAuthenticationImpl basicAuth =
                        (BasicAuthenticationImpl) appRouteEndpointImpl.getGenericEndpointAuthentication();
                if (accessUrl != null) {
                    appRouteEndpointImpl.setAccessURL(URI.create(accessUrl));
                }
                if (username != null) {
                    basicAuth.setAuthUsername(username);
                }
                if (password != null) {
                    basicAuth.setAuthPassword(password);
                }
            }

            configModelService.saveState();

            // Finds the correct resource and representation to update the representation at the connector
            URI representationId = representationEndpointService.getRepresentationId(endpointId);
            URI resourceId = null;
            Representation foundRepresentation = null;
            if (representationId != null) {
                for (ResourceCatalog resourceCatalog : configModelService.getConfigModel()
                        .getConnectorDescription().getResourceCatalog()) {
                    for (Resource resource : resourceCatalog.getOfferedResource()) {
                        for (Representation representation : resource.getRepresentation()) {
                            if (representationId.equals(representation.getId())) {
                                resourceId = resource.getId();
                                foundRepresentation = representation;
                                break;
                            }
                        }
                    }
                }
            }

            // Updates the custom resource representation of the connector
            ResponseEntity<String> res =
                    utilService.addEndpointToConnectorRepresentation(endpointId, resourceId, foundRepresentation);
            logger.info("Response of updates custom resource representation: {}", res);

            var jsonObject = new JSONObject();
            jsonObject.put("message", "Successfully updated the endpoint in the app route with the id: " +
                    endpointId.toString());

            return ResponseEntity.ok(jsonObject.toJSONString());
        }
        return ResponseEntity.badRequest().body("Could not find the app route for updating the endpoint");
    }
}
