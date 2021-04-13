package de.fraunhofer.isst.configmanager.api.service.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ResourceImpl;
import de.fraunhofer.iais.eis.RouteStep;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.api.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.model.usagecontrol.Pattern;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing resource contracts.
 */
@Slf4j
@Service
@Transactional
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ResourceContractService extends AbstractResourceService {

    transient ResourceContractBuilder resourceContractBuilder;

    @Autowired
    public ResourceContractService(final ConfigModelService configModelService,
                                   final DefaultConnectorClient connectorClient,
                                   final ResourceContractBuilder resourceContractBuilder) {

        super(configModelService, connectorClient);
        this.resourceContractBuilder = resourceContractBuilder;
    }

    /**
     * This method returns from a resource the contract offer.
     *
     * @param resourceId id of the resource
     * @return contract offer
     */
    public ContractOffer getResourceContract(final URI resourceId) {
        for (final var resource : getResources()) {
            if (resourceId.equals(resource.getId()) && resource.getContractOffer().get(0) != null) {
                return resource.getContractOffer().get(0);
            }
        }
        return null;
    }

    /**
     * This method updates the resource contract with the given parameters.
     *
     * @param resourceId    id of the resource
     * @param contractOffer the contract offer which will be updated
     */
    public void updateResourceContractInAppRoute(final URI resourceId,
                                                 final ContractOffer contractOffer) {
        // Update resource representation in app route
        if (configModelService.getConfigModel().getAppRoute() == null) {
            log.info("---- [ResourceService updateResourceContractInAppRoute] Could not find any app route");
        } else {
            final ArrayList<RouteStep> emptyList = new ArrayList<>();
            for (final var appRoute : configModelService.getConfigModel().getAppRoute()) {

                if (appRoute == null) {
                    continue;
                }
                if (appRoute.getAppRouteOutput() != null) {
                    for (final var resource : appRoute.getAppRouteOutput()) {
                        if (resourceId.equals(resource.getId())) {
                            final var resourceImpl = (ResourceImpl) resource;
                            resourceImpl.setContractOffer(Util.asList(contractOffer));
                            log.info("---- [ResourceService updateResourceContractInSubroutes] Updated" +
                                    " resource contract in the app route");
                            break;
                        }
                    }
                }
                if (appRoute.getHasSubRoute() == null) {
                    continue;
                }
                for (final var subRoute : appRoute.getHasSubRoute()) {
                    updateResourceContractInSubroutes(subRoute, emptyList, resourceId,
                            contractOffer);
                }
            }
            configModelService.saveState();
        }
    }

    /**
     * @param routeStep     route step
     * @param visited       list of route steps already managed
     * @param resourceId    id of the resource
     * @param contractOffer the contract offer
     */
    private void updateResourceContractInSubroutes(final RouteStep routeStep,
                                                   final List<RouteStep> visited,
                                                   final URI resourceId,
                                                   final ContractOffer contractOffer) {
        if (routeStep == null) {
            return;
        }
        if (routeStep.getAppRouteOutput() != null) {
            for (final var resource : routeStep.getAppRouteOutput()) {
                if (resourceId.equals(resource.getId())) {
                    final var resourceImpl = (ResourceImpl) resource;
                    resourceImpl.setContractOffer(Util.asList(contractOffer));
                    log.info("---- [ResourceService updateResourceContractInSubroutes] Updated resource" +
                            " contract in the subroute");
                    break;
                }
            }
        }
        if (routeStep.getHasSubRoute() == null) {
            return;
        }
        for (final var subRoute : routeStep.getHasSubRoute()) {
            if (!visited.contains(subRoute)) {
                visited.add(routeStep);
                updateResourceContractInSubroutes(subRoute, visited, resourceId, contractOffer);
            }
        }
    }

    /**
     * @param pattern      pattern to create appropriate contract offer
     * @param contractJson the request body which holds the necessary information
     * @return contract offer
     */
    public ContractOffer getContractOffer(final Pattern pattern, final String contractJson) throws JsonProcessingException {
        ContractOffer contractOffer = null;

        switch (pattern) {
            case PROVIDE_ACCESS:
                contractOffer = resourceContractBuilder.buildProvideAccess();
                break;
            case PROHIBIT_ACCESS:
                contractOffer = resourceContractBuilder.buildProhibitAccess();
                break;
            case N_TIMES_USAGE:
                if (contractJson != null && !contractJson.equals("{}")) {
                    final var jsonNode = resourceContractBuilder.getJsonNodeFromContract(contractJson);

                    final var binaryOperator = resourceContractBuilder.getBinaryOperator(jsonNode);
                    final var number = jsonNode.get("number").asText();
                    final var pipEndpoint = jsonNode.get("pipendpoint").asText();

                    contractOffer = resourceContractBuilder.buildNTimesUsage(binaryOperator, number, pipEndpoint);
                    break;
                }
            case DURATION_USAGE:
                if (contractJson != null && !contractJson.equals("{}")) {
                    final var jsonNode = resourceContractBuilder.getJsonNodeFromContract(contractJson);
                    final var number = jsonNode.get("number").asText();

                    contractOffer = resourceContractBuilder.buildDurationUsage(number);
                    break;
                }
            case USAGE_NOTIFICATION:
                if (contractJson != null && !contractJson.equals("{}")) {
                    final var jsonNode = resourceContractBuilder.getJsonNodeFromContract(contractJson);
                    final var url = jsonNode.get("url").asText();

                    contractOffer = resourceContractBuilder.buidUsageNotification(url);
                    break;
                }
            case USAGE_LOGGING:
                contractOffer = resourceContractBuilder.buildUsageLogging();
                break;

            case USAGE_DURING_INTERVAL:
                if (contractJson != null && !contractJson.equals("{}")) {
                    final var jsonNode = resourceContractBuilder.getJsonNodeFromContract(contractJson);
                    final var fromDate = jsonNode.get("fromDate").asText();
                    final var toDate = jsonNode.get("toDate").asText();

                    contractOffer = resourceContractBuilder.buildUsageDuringInterval(fromDate, toDate);
                    break;
                }
            case USAGE_UNTIL_DELETION:
                if (contractJson != null && !contractJson.equals("{}")) {
                    final var jsonNode = resourceContractBuilder.getJsonNodeFromContract(contractJson);
                    final var startDate = jsonNode.get("startDate").asText();
                    final var endDate = jsonNode.get("endDate").asText();
                    final var deletionDate = jsonNode.get("deletionDate").asText();

                    contractOffer = resourceContractBuilder.buildUsageUntilDeletion(startDate, endDate, deletionDate);
                    break;
                }
            default:
                break;
        }
        return contractOffer;
    }

}
