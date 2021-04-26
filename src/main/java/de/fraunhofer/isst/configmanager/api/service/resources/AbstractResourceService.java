package de.fraunhofer.isst.configmanager.api.service.resources;

import de.fraunhofer.iais.eis.BaseConnector;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.RouteStep;
import de.fraunhofer.isst.configmanager.api.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultConnectorClient;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class AbstractResourceService {

    transient ConfigModelService configModelService;
    transient DefaultConnectorClient connectorClient;

    @Autowired
    protected AbstractResourceService(final ConfigModelService configModelService,
                                   final DefaultConnectorClient connectorClient) {
        this.configModelService = configModelService;
        this.connectorClient = connectorClient;
    }

    /**
     * Gets the {@link Resource} of a given resource ID.
     *
     * @param resourceId of the resource
     * @return resource
     */
    public Resource getResource(final URI resourceId) {
        try {
            return getResources().stream()
                    .dropWhile(res -> !res.getId().equals(resourceId))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * This method returns a list of all resources from the connector.
     *
     * @return list of resources from the connector
     */
    public List<Resource> getResources() {
        final ArrayList<Resource> resources = new ArrayList<>();

        BaseConnector baseConnector = null;
        try {
            baseConnector = connectorClient.getSelfDeclaration();
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
        if (baseConnector != null && baseConnector.getResourceCatalog() != null) {
            for (final var resourceCatalog : baseConnector.getResourceCatalog()) {
                if (resourceCatalog.getOfferedResource() != null) {
                    resources.addAll(resourceCatalog.getOfferedResource());
                }
            }
        }
        return resources;
    }

    /**
     * Delete occurrence of a resource with resourceID from all SubRoutes.
     *
     * @param current    current Node in AppRoute
     * @param visited    already visited AppRoutes
     * @param resourceId ID of the Resource to delete
     */
    public void deleteFromSubRoutes(final RouteStep current, final List<RouteStep> visited,
                                    final URI resourceId) {
        if (current == null) {
            return;
        }
        if (current.getAppRouteOutput() != null) {
            current.getAppRouteOutput().removeIf(resource -> resource.getId().equals(resourceId));
        }
        if (current.getHasSubRoute() == null) {
            return;
        }
        for (final var subRoute : current.getHasSubRoute()) {
            if (!visited.contains(subRoute)) {
                visited.add(current);
                deleteFromSubRoutes(subRoute, visited, resourceId);
            }
        }
    }
}
