package de.fraunhofer.isst.configmanager.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultResourceClient;
import de.fraunhofer.isst.configmanager.model.configlists.EndpointInformationRepository;
import de.fraunhofer.isst.configmanager.model.endpointinfo.EndpointInformation;
import de.fraunhofer.isst.configmanager.model.usagecontrol.Pattern;
import de.fraunhofer.isst.configmanager.util.CalenderUtil;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Service class for managing resources.
 */
@Slf4j
@Service
@Transactional
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ResourceService {

    transient ConfigModelService configModelService;
    transient EndpointService endpointService;
    transient EndpointInformationRepository endpointInformationRepository;
    transient DefaultResourceClient resourceClient;
    transient DefaultConnectorClient connectorClient;

    @Autowired
    public ResourceService(final ConfigModelService configModelService,
                           final EndpointService endpointService,
                           final EndpointInformationRepository endpointInformationRepository,
                           final DefaultResourceClient resourceClient,
                           final DefaultConnectorClient connectorClient) {
        this.configModelService = configModelService;
        this.endpointService = endpointService;
        this.endpointInformationRepository = endpointInformationRepository;
        this.resourceClient = resourceClient;
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
     * This method updates the content of the resource with the given parameters.
     *
     * @param title           title of the resource
     * @param description     description of the resource
     * @param language        the language
     * @param keywords        the keywords
     * @param version         the version of the resource
     * @param standardlicense the license of the resource
     * @param publisher       the publisher of the resource
     * @param resourceImpl    the resource implementation class to set the parameters
     */
    public void updateResourceContent(final String title, final String description,
                                      final String language, final List<String> keywords,
                                      final String version, final String standardlicense,
                                      final String publisher, final ResourceImpl resourceImpl) {
        if (title != null) {
            resourceImpl.setTitle(Util.asList(new TypedLiteral(title)));
        }
        if (description != null) {
            resourceImpl.setDescription(Util.asList(new TypedLiteral(description)));
        }
        if (language != null) {
            resourceImpl.setLanguage(Util.asList(Language.valueOf(language)));
        }
        if (keywords != null) {
            final ArrayList<TypedLiteral> keys = new ArrayList<>();
            final var literal = new TypedLiteral();
            for (final var keyword : keywords) {
                literal.setValue(keyword);
                keys.add(literal);
            }
            resourceImpl.setKeyword(keys);
        }
        if (version != null) {
            resourceImpl.setVersion(version);
        }
        if (standardlicense != null) {
            resourceImpl.setStandardLicense(URI.create(standardlicense));
        }
        if (publisher != null) {
            resourceImpl.setPublisher(URI.create(publisher));
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
            log.error(e.getMessage(), e);
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
     * This method returns all offered resources of a connector as plain json String.
     *
     * @return list of resources from the connector
     */
    public String getOfferedResourcesAsJsonString() {
        try {
            return resourceClient.getOfferedResourcesAsJsonString();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * This method returns all requested resources of a connector as plain json String.
     *
     * @return list of resources from the connector
     */
    public String getRequestedResourcesAsJsonString() {
        try {
            return resourceClient.getRequestedResourcesAsJsonString();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
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
                            log.info("---- [ResourceService updateResourceContractInSubroutes] Updated resource contract in the app route");
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
                    log.info("---- [ResourceService updateResourceContractInSubroutes] Updated resource contract in the subroute");
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
     * @param representationId id of the representation
     * @return representation implementation
     */
    public RepresentationImpl getResourceRepresentationInCatalog(final URI representationId) {
        return (RepresentationImpl) getResources()
                .stream()
                .map(DigitalContent::getRepresentation)
                .flatMap(Collection::stream)
                .filter(representation -> representation.getId().equals(representationId))
                .findAny()
                .orElse(null);
    }

    /**
     * @param resourceId       id of the resource
     * @param representationId id of the representation to delete
     */
    public void deleteResourceRepresentationFromAppRoute(final URI resourceId,
                                                         final URI representationId) {
        if (configModelService.getConfigModel().getAppRoute() == null) {
            log.info("---- [ResourceService deleteResourceRepresentationFromAppRoute] Could not find any app route to delete the resource");
        } else {
            final ArrayList<RouteStep> emptyList = new ArrayList<>();
            for (final var route : configModelService.getConfigModel().getAppRoute()) {
                if (route == null) {
                    continue;
                }
                if (route.getAppRouteOutput() != null) {
                    for (final var resource : route.getAppRouteOutput()) {
                        if (resource.getRepresentation() != null) {
                            resource.getRepresentation().removeIf(representation ->
                                    representation.getId().equals(representationId)
                            );
                        }
                    }
                }
                if (route.getHasSubRoute() == null) {
                    continue;
                }

                for (final var subRoute : route.getHasSubRoute()) {
                    deleteRepresentationFromSubRoutes(subRoute, emptyList, resourceId,
                            representationId);
                }
            }
        }
        configModelService.saveState();
    }

    /**
     * Delete occurrence of a resource representation with resourceID and representationID from.
     * all SubRoutes
     *
     * @param current          current Node in AppRoute
     * @param visited          already visited AppRoutes
     * @param resourceId       ID of the Resource for which the representation should be deleted
     * @param representationId ID of the Representation to delete
     */
    private void deleteRepresentationFromSubRoutes(final RouteStep current,
                                                   final List<RouteStep> visited,
                                                   final URI resourceId,
                                                   final URI representationId) {
        if (current == null) {
            return;
        }
        if (current.getAppRouteOutput() != null) {
            for (final var resource : current.getAppRouteOutput()) {
                if (resource.getRepresentation() != null) {
                    resource.getRepresentation().removeIf(representation ->
                            representation.getId().equals(representationId)
                    );
                }
            }
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

    /**
     * @param resourceId id of the resource
     */
    public void deleteResourceFromAppRoute(final URI resourceId) {
        if (configModelService.getConfigModel().getAppRoute() == null) {
            log.info("---- [ResourceService deleteResourceFromAppRoute] Could not find any app route to delete the resource");
        } else {
            final ArrayList<RouteStep> emptyList = new ArrayList<>();
            for (final var route : configModelService.getConfigModel().getAppRoute()) {
                if (route == null) {
                    continue;
                }
                if (route.getAppRouteOutput() != null) {
                    route.getAppRouteOutput().removeIf(resource -> resource.getId().equals(resourceId));
                }
                if (route.getHasSubRoute() == null) {
                    continue;
                }

                for (final var subRoute : route.getHasSubRoute()) {
                    deleteFromSubRoutes(subRoute, emptyList, resourceId);
                }
            }
        }
        configModelService.saveState();
    }

    /**
     * Delete occurrence of a resource with resourceID from all SubRoutes.
     *
     * @param current    current Node in AppRoute
     * @param visited    already visited AppRoutes
     * @param resourceId ID of the Resource to delete
     */
    private void deleteFromSubRoutes(final RouteStep current, final List<RouteStep> visited,
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

    /**
     * @param title           title of the resource
     * @param description     description of the resource
     * @param language        language of the resource
     * @param keywords        keywords for the resource
     * @param version         version of the resource
     * @param standardlicense standard license for the resource
     * @param publisher       the publisher of the resource
     * @return resource implementation
     */
    public ResourceImpl createResource(final String title, final String description,
                                       final String language, final List<String> keywords,
                                       final String version, final String standardlicense,
                                       final String publisher) {

        final ArrayList<TypedLiteral> keys = new ArrayList<>();
        final var literal = new TypedLiteral();
        for (final var keyword : keywords) {
            literal.setValue(keyword);
            keys.add(literal);
        }

        // Create the resource with the given parameters
        return (ResourceImpl) new ResourceBuilder()
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
    }

    public ResourceImpl updateResource(final URI resourceId, final String title,
                                       final String description, final String language,
                                       final List<String> keywords, final String version,
                                       final String standardlicense, final String publisher) {
        //Get a Resource and update if it exists
        for (final var resource : getResources()) {
            if (resource.getId().equals(resourceId)) {
                final var resImpl = (ResourceImpl) resource;
                updateResourceContent(title, description, language, keywords, version,
                        standardlicense, publisher, resImpl);
                return resImpl;
            }
        }
        return null;
    }

    /**
     * @param newResource new resource old version should be replaced with
     */
    public void updateResourceInAppRoute(final ResourceImpl newResource) {
        // Update the resource in the app route
        if (configModelService.getConfigModel().getAppRoute() == null) {
            log.info("---- [ResourceService updateResourceInAppRoute] Could not find any app route to update the resource");
        } else {
            final ArrayList<RouteStep> emptyList = new ArrayList<>();
            for (final var appRoute : configModelService.getConfigModel().getAppRoute()) {

                if (appRoute == null) {
                    continue;
                }

                if (appRoute.getAppRouteOutput() != null) {
                    for (final var resource : appRoute.getAppRouteOutput()) {
                        if (newResource.getId().equals(resource.getId())) {
                            final ArrayList<Resource> output =
                                    (ArrayList<Resource>) appRoute.getAppRouteOutput();
                            output.remove(resource);
                            output.add(newResource);
                            log.info("---- [ResourceService updateResourceInAppRoute] Updated resource in app route");
                            break;
                        }
                    }
                }

                if (appRoute.getHasSubRoute() == null) {
                    continue;
                }

                for (final var subRoute : appRoute.getHasSubRoute()) {
                    updateResourceInSubroutes(subRoute, emptyList, newResource);
                }
            }
            configModelService.saveState();
        }
    }

    /**
     * @param routeStep   routestep
     * @param visited     list of route steps already managed
     * @param newResource new resource old version should be replaced with
     */
    private void updateResourceInSubroutes(final RouteStep routeStep,
                                           final List<RouteStep> visited,
                                           final ResourceImpl newResource) {

        if (routeStep == null) {
            return;
        }
        if (routeStep.getAppRouteOutput() != null) {
            for (final var resource : routeStep.getAppRouteOutput()) {
                if (newResource.getId().equals(resource.getId())) {
                    final ArrayList<Resource> output =
                            (ArrayList<Resource>) routeStep.getAppRouteOutput();
                    output.remove(resource);
                    output.add(newResource);
                    log.info("---- [ResourceService updateResourceInAppRoute] Updated resource in subroute");
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
                updateResourceInSubroutes(subRoute, visited, newResource);
            }
        }
    }

    /**
     * This method updates a backend connection.
     *
     * @param resourceId id of the resource
     * @param endpointId id of the endpoint
     */
    public void updateBackendConnection(final URI resourceId, final URI endpointId) {
        if (configModelService.getConfigModel().getAppRoute() != null) {
            RouteStepImpl foundRouteStep = null;
            AppRouteImpl appRouteImpl = null;
            for (final var appRoute : configModelService.getConfigModel().getAppRoute()) {
                for (final var routeStep : appRoute.getHasSubRoute()) {
                    for (final var resource : routeStep.getAppRouteOutput()) {
                        if (resourceId.equals(resource.getId())) {
                            appRouteImpl = (AppRouteImpl) appRoute;
                            foundRouteStep = (RouteStepImpl) routeStep;
                            break;
                        }
                    }
                }
            }

            // Set app route start and subroute start to the updated endpoint
            if (appRouteImpl != null) {
                final var endpoint = endpointService.getGenericEndpoint(endpointId);
                if (endpoint != null) {
                    appRouteImpl.setAppRouteStart(Util.asList(endpoint));
                    foundRouteStep.setAppRouteStart(Util.asList(endpoint));
                }
            }
        }

        // Set first entry of endpoint informations to the new endpoint
        if (endpointInformationRepository.findAll().size() > 0) {
            final var endpointInfo = endpointInformationRepository.findAll().get(0);
            endpointInfo.setEndpointId(endpointId.toString());
            endpointInformationRepository.saveAndFlush(endpointInfo);
        } else {
            final var endpointInformation = new EndpointInformation();
            endpointInformation.setEndpointId(endpointId.toString());
            endpointInformationRepository.saveAndFlush(endpointInformation);
        }
    }

    /**
     * This method returns the resource if it is exists in an app route.
     *
     * @param resourceId id of the resource
     * @return resource
     */
    public Resource getResourceInAppRoute(final URI resourceId) {

        Resource foundResource = null;
        final ArrayList<RouteStep> emptyList = new ArrayList<>();
        for (final var appRoute : configModelService.getConfigModel().getAppRoute()) {

            if (appRoute == null) {
                continue;
            }

            if (appRoute.getAppRouteOutput() != null) {
                for (final var resource : appRoute.getAppRouteOutput()) {
                    if (resourceId.equals(resource.getId())) {
                        foundResource = resource;
                        log.info("---- [ResourceService getResourceInAppRoute] Resource is found in the app route");
                        break;
                    }
                }
            }

            if (appRoute.getHasSubRoute() == null) {
                continue;
            }

            for (final var subRoute : appRoute.getHasSubRoute()) {
                foundResource = getResourceInSubroutes(subRoute, emptyList, resourceId);
            }

        }
        if (foundResource == null) {
            log.info("---- [ResourceService getResourceInAppRoute] Could not find any resource in app routes and subroutes");
        }

        return foundResource;
    }

    /**
     * @param routeStep  routestep
     * @param visited    list of route steps already visited
     * @param resourceId id of the resource
     * @return resource
     */
    private Resource getResourceInSubroutes(final RouteStep routeStep,
                                            final List<RouteStep> visited,
                                            final URI resourceId) {

        Resource foundResource = null;

        if (routeStep == null) {
            return null;
        }
        if (routeStep.getAppRouteOutput() != null) {
            for (final var resource : routeStep.getAppRouteOutput()) {
                if (resourceId.equals(resource.getId())) {
                    foundResource = resource;
                    log.info("---- [ResourceService getResourceInSubroutes] Resource is found in subroute");
                    break;
                }
            }
        }

        if (!routeStep.getHasSubRoute().isEmpty()) {
            for (final var subRoute : routeStep.getHasSubRoute()) {
                if (!visited.contains(subRoute)) {
                    visited.add(routeStep);
                    foundResource = getResourceInSubroutes(subRoute, visited, resourceId);
                }
            }
        }

        return foundResource;
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
                contractOffer = buildProvideAccess();
                break;
            case PROHIBIT_ACCESS:
                contractOffer = buildProhibitAccess();
                break;
            case N_TIMES_USAGE:
                if (contractJson != null && !contractJson.equals("{}")) {
                    final var jsonNode = getJsonNodeFromContract(contractJson);

                    final var binaryOperator = getBinaryOperator(jsonNode);
                    final var number = jsonNode.get("number").asText();
                    final var pipEndpoint = jsonNode.get("pipendpoint").asText();

                    contractOffer = buildNTimesUsage(binaryOperator, number, pipEndpoint);
                    break;
                }
            case DURATION_USAGE:
                if (contractJson != null && !contractJson.equals("{}")) {
                    final var jsonNode = getJsonNodeFromContract(contractJson);
                    final var number = jsonNode.get("number").asText();

                    contractOffer = buildDurationUsage(number);
                    break;
                }
            case USAGE_NOTIFICATION:
                if (contractJson != null && !contractJson.equals("{}")) {
                    final var jsonNode = getJsonNodeFromContract(contractJson);
                    final var url = jsonNode.get("url").asText();

                    contractOffer = buidUsageNotification(url);
                    break;
                }
            case USAGE_LOGGING:
                contractOffer = buildUsageLogging();
                break;

            case USAGE_DURING_INTERVAL:
                if (contractJson != null && !contractJson.equals("{}")) {
                    final var jsonNode = getJsonNodeFromContract(contractJson);
                    final var fromDate = jsonNode.get("fromDate").asText();
                    final var toDate = jsonNode.get("toDate").asText();

                    contractOffer = buildUsageDuringInterval(fromDate, toDate);
                    break;
                }
            case USAGE_UNTIL_DELETION:
                if (contractJson != null && !contractJson.equals("{}")) {
                    final var jsonNode = getJsonNodeFromContract(contractJson);
                    final var startDate = jsonNode.get("startDate").asText();
                    final var endDate = jsonNode.get("endDate").asText();
                    final var deletionDate = jsonNode.get("deletionDate").asText();

                    contractOffer = buildUsageUntilDeletion(startDate, endDate, deletionDate);
                    break;
                }
            default:
                break;
        }
        return contractOffer;
    }

    private ContractOffer buildUsageUntilDeletion(final String startDate,
                                                  final String endDate,
                                                  final String deletionDate) {
        return new ContractOfferBuilder()
                ._permission_(Util.asList(new PermissionBuilder()
                        ._title_(Util.asList(new TypedLiteral("Usage Policy")))
                        ._description_(Util.asList(new TypedLiteral("usage-until-deletion")))
                        ._action_(Util.asList(Action.USE))
                        ._constraint_(Util.asList(new ConstraintBuilder()
                                ._leftOperand_(LeftOperand.POLICY_EVALUATION_TIME)
                                ._operator_(BinaryOperator.AFTER)
                                ._rightOperand_(new RdfResource(startDate,
                                        URI.create("xsd:dateTimeStamp")))
                                .build(), new ConstraintBuilder()
                                ._leftOperand_(LeftOperand.POLICY_EVALUATION_TIME)
                                ._operator_(BinaryOperator.BEFORE)
                                ._rightOperand_(new RdfResource(endDate,
                                        URI.create("xsd:dateTimeStamp")))
                                .build()))
                        ._postDuty_(Util.asList(new DutyBuilder()
                                ._action_(Util.asList(Action.DELETE))
                                ._constraint_(Util.asList(new ConstraintBuilder()
                                        ._leftOperand_(LeftOperand.POLICY_EVALUATION_TIME)
                                        ._operator_(BinaryOperator.TEMPORAL_EQUALS)
                                        ._rightOperand_(new RdfResource(deletionDate,
                                                URI.create("xsd:dateTimeStamp")))
                                        .build()))
                                .build()))
                        .build()))
                .build();
    }

    private ContractOffer buildUsageDuringInterval(final String fromDate,
                                                   final String toDate) {
        return new ContractOfferBuilder()
                ._permission_(Util.asList(new PermissionBuilder()
                        ._title_(Util.asList(new TypedLiteral("Usage Policy")))
                        ._description_(Util.asList(new TypedLiteral("usage-during-interval")))
                        ._action_(Util.asList(Action.USE))
                        ._constraint_(Util.asList(new ConstraintBuilder()
                                ._leftOperand_(LeftOperand.POLICY_EVALUATION_TIME)
                                ._operator_(BinaryOperator.AFTER)
                                ._rightOperand_(new RdfResource(fromDate,
                                        URI.create("xsd:dateTimeStamp")))
                                .build(), new ConstraintBuilder()
                                ._leftOperand_(LeftOperand.POLICY_EVALUATION_TIME)
                                ._operator_(BinaryOperator.BEFORE)
                                ._rightOperand_(new RdfResource(toDate,
                                        URI.create("xsd:dateTimeStamp")))
                                .build()))
                        .build()))
                .build();
    }

    private ContractOffer buildUsageLogging() {
        return new ContractOfferBuilder()
                ._permission_(Util.asList(new PermissionBuilder()
                        ._title_(Util.asList(new TypedLiteral("Usage Policy")))
                        ._description_(Util.asList(new TypedLiteral("usage-logging")))
                        ._action_(Util.asList(Action.USE))
                        ._postDuty_(Util.asList(new DutyBuilder()
                                ._action_(Util.asList(Action.LOG))
                                .build()))
                        .build()))
                .build();
    }

    private ContractOffer buidUsageNotification(final String url) {
        return new ContractOfferBuilder()
                ._permission_(Util.asList(new PermissionBuilder()
                        ._title_(Util.asList(new TypedLiteral("Usage Policy")))
                        ._description_(Util.asList(new TypedLiteral("usage-notification")))
                        ._action_(Util.asList(Action.USE))
                        ._postDuty_(Util.asList(new DutyBuilder()
                                ._action_(Util.asList(Action.NOTIFY))
                                ._constraint_(Util.asList(new ConstraintBuilder()
                                        ._leftOperand_(LeftOperand.ENDPOINT)
                                        ._operator_(BinaryOperator.DEFINES_AS)
                                        ._rightOperand_(
                                                new RdfResource(url, URI.create("xsd:anyURI")))
                                        .build()))
                                .build()))
                        .build()))
                .build();
    }

    private ContractOffer buildDurationUsage(final String number) {
        return new ContractOfferBuilder()
                ._permission_(Util.asList(new PermissionBuilder()
                        ._title_(Util.asList(new TypedLiteral("Usage Policy")))
                        ._description_(Util.asList(new TypedLiteral("duration-usage")))
                        ._action_(Util.asList(Action.USE))
                        ._constraint_(Util.asList(new ConstraintBuilder()
                                ._leftOperand_(LeftOperand.ELAPSED_TIME)
                                ._operator_(BinaryOperator.SHORTER_EQ)
                                ._rightOperand_(new RdfResource(number, URI.create("xsd:duration")))
                                .build()))
                        .build()))
                .build();
    }

    @NotNull
    private BinaryOperator getBinaryOperator(final JsonNode jsonNode) {
        final var operator = jsonNode.get("binaryoperator").asText();

        BinaryOperator binaryOperator;
        if ("<".equals(operator)) {
            binaryOperator = BinaryOperator.LT;
        } else if ("<=".equals(operator)) {
            binaryOperator = BinaryOperator.LTEQ;
        } else {
            binaryOperator = BinaryOperator.EQ;
        }
        return binaryOperator;
    }

    private ContractOffer buildNTimesUsage(final BinaryOperator binaryOperator,
                                           final String number,
                                           final String pipEndpoint) {
        return new NotMoreThanNOfferBuilder()
                ._permission_(Util.asList(new PermissionBuilder()
                        ._title_(Util.asList(new TypedLiteral("Usage Policy")))
                        ._description_(Util.asList(new TypedLiteral("n-times-usage")))
                        ._action_(Util.asList(Action.USE))
                        ._constraint_(Util.asList(new ConstraintBuilder()
                                ._leftOperand_(LeftOperand.COUNT)
                                ._operator_(binaryOperator)
                                ._rightOperand_(new RdfResource(number, URI.create("xsd:double")))
                                ._pipEndpoint_(
                                        URI.create(pipEndpoint))
                                .build()))
                        .build()))
                .build();
    }

    private ContractOffer buildProhibitAccess() {
        return new ContractOfferBuilder()
                ._prohibition_(Util.asList(new ProhibitionBuilder()
                        ._title_(Util.asList(new TypedLiteral("Usage Policy")))
                        ._description_(Util.asList(new TypedLiteral("prohibit-access")))
                        ._action_(Util.asList(Action.USE))
                        .build()))
                .build();
    }

    private ContractOffer buildProvideAccess() {
        return new ContractOfferBuilder()
                ._permission_(Util.asList(new PermissionBuilder()
                        ._title_(Util.asList(new TypedLiteral("Usage Policy")))
                        ._description_(Util.asList(new TypedLiteral("provide-access")))
                        ._action_(Util.asList(Action.USE))
                        .build()))
                .build();
    }

    /**
     * @param contractJson the contract offer
     * @return json node
     */
    private JsonNode getJsonNodeFromContract(final String contractJson) throws JsonProcessingException {
        final var mapper = new ObjectMapper();
        return mapper.readTree(Objects.requireNonNull(contractJson));
    }
}
