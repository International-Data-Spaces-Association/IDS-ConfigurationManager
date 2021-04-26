package de.fraunhofer.isst.configmanager.api.service;

import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.isst.configmanager.api.service.resources.ResourceService;
import de.fraunhofer.isst.configmanager.data.enums.BrokerRegistrationStatus;
import de.fraunhofer.isst.configmanager.data.entities.CustomBroker;
import de.fraunhofer.isst.configmanager.data.repositories.CustomBrokerRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for the custom broker.
 */
@Slf4j
@Service
@Transactional
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BrokerService {
    transient CustomBrokerRepository customBrokerRepository;
    transient ResourceService resourceService;

    @Autowired
    public BrokerService(final CustomBrokerRepository customBrokerRepository,
                         final ResourceService resourceService) {
        this.customBrokerRepository = customBrokerRepository;
        this.resourceService = resourceService;

        // If no broker is found in the database, a default broker is created at this point.
        if (customBrokerRepository.count() == 0) {
            if (log.isInfoEnabled()) {
                log.info("---- [BrokerService] Db is empty! Creating custom broker");
            }
            final var customBroker = new CustomBroker();
            customBroker.setBrokerUri(URI.create("https://broker.ids.isst.fraunhofer.de/infrastructure"));
            customBroker.setTitle("IDS Broker");
            customBroker.setBrokerRegistrationStatus(BrokerRegistrationStatus.UNREGISTERED);
            customBrokerRepository.save(customBroker);
        }
    }

    /**
     * The method creates a custom broker.
     *
     * @param brokerUri uri of the broker
     * @param title     title of the broker
     */
    public void createCustomBroker(final URI brokerUri, final String title) {
        final var customBroker = new CustomBroker(brokerUri);
        if (title != null) {
            customBroker.setTitle(title);
        }
        customBroker.setBrokerRegistrationStatus(BrokerRegistrationStatus.UNREGISTERED);
        customBrokerRepository.save(customBroker);
    }

    /**
     * The method updates the given broker.
     *
     * @param brokerUri uri of the broker
     * @param title     title of the broker
     * @return true, when broker is updated
     */
    public boolean updateBroker(final URI brokerUri, final String title) {
        var updated = false;
        final var broker = getById(brokerUri);

        if (broker != null) {
            if (title != null) {
                broker.setTitle(title);
            }
            customBrokerRepository.save(broker);
            updated = true;
        }

        return updated;
    }

    /**
     * The method deletes the broker using the broker id.
     *
     * @param id of the broker which is deleted
     * @return true, if broker is deleted
     */
    public boolean deleteBroker(final URI id) {
        var deleted = false;
        final var customBroker = getById(id);
        if (customBroker != null) {
            customBrokerRepository.delete(customBroker);
            deleted = true;
        } else {
            if (log.isWarnEnabled()) {
                log.warn(String.format("---- [BrokerService deleteBroker] Tried to delete a Broker, but no config with id %s exists!", id.toString()));
            }
        }
        return deleted;
    }

    /**
     * @return list of all custom brokers
     */
    public List<CustomBroker> getCustomBrokers() {
        return customBrokerRepository.findAll();
    }

    /**
     * The method returns a specific broker using the id.
     *
     * @param id of the broker
     * @return custom broker
     */
    public CustomBroker getById(final URI id) {
        return customBrokerRepository.findAll().stream().filter(customBroker -> customBroker.getBrokerUri().equals(id))
                .findAny().orElse(null);
    }

    /**
     * This method is responsible for setting the broker status.
     *
     * @param brokerId     id of the broker
     * @param brokerRegistrationStatus broker status
     */
    public void setBrokerStatus(final URI brokerId, final BrokerRegistrationStatus brokerRegistrationStatus) {
        final var customBroker = getById(brokerId);
        if (customBroker != null) {
            customBroker.setBrokerRegistrationStatus(brokerRegistrationStatus);
            customBrokerRepository.save(customBroker);
        }
    }

    public void sentSelfDescToBroker(final URI brokerId) {
        final var customBroker = getById(brokerId);
        customBroker.setRegisteredResources(
                resourceService.getResources().stream().map(Resource::getId).map(URI::toString)
                        .collect(Collectors.toList())
        );
        customBrokerRepository.save(customBroker);
    }

    public void unregisteredAtBroker(final URI brokerId) {
        final var customBroker = getById(brokerId);
        customBroker.setRegisteredResources(new ArrayList<>());
        customBrokerRepository.save(customBroker);
    }

    /**
     * This method deletes the resource at the broker.
     *
     * @param brokerUri  id of the broker
     * @param resourceId id of the resource
     */
    public void deleteResourceAtBroker(final URI brokerUri, final URI resourceId) {
        final var customBroker = getById(brokerUri);
        if (customBroker != null) {
            if (customBroker.getRegisteredResources() == null) {
                if (log.isInfoEnabled()) {
                    log.info("---- [BrokerService deleteResourceAtBroker] Could not found any resource to delete");
                }
            } else {
                final var registeredResources = customBroker.getRegisteredResources();
                registeredResources.removeIf(s -> s.equals(resourceId.toString()));
                customBroker.setRegisteredResources(registeredResources);
                customBrokerRepository.save(customBroker);
            }
        }
    }

    /**
     * This method creates a JSON for the registration status of a resource at a broker.
     *
     * @param resourceId id of the resource
     * @return jsonObject
     */
    public JSONArray getRegisStatusForResource(final URI resourceId) {

        final var customBrokers = customBrokerRepository.findAll();
        if (customBrokers.isEmpty()) {
            if (log.isInfoEnabled()) {
                log.info("---- [BrokerService getRegisStatusForResource] Could not find any broker");
            }
        } else {
            final var jsonArray = new JSONArray();
            final var jsonObject = new JSONObject();
            for (final var customBroker : customBrokers) {
                if (customBroker.getRegisteredResources() != null) {
                    for (final var id : customBroker.getRegisteredResources()) {
                        if (resourceId.toString().equals(id)) {
                            jsonObject.clear();
                            jsonObject.put("brokerId", customBroker.getBrokerUri().toString());
                            jsonObject.put("brokerStatus",
                                    customBroker.getBrokerRegistrationStatus().toString());
                            jsonObject.put("resourceId", resourceId.toString());
                            jsonArray.add(jsonObject);
                        }
                    }
                }
            }
            return jsonArray;
        }
        return null;
    }

    /**
     * @return json array of all registered broker
     */
    public JSONArray getRegisteredBroker() {
        final var jsonArray = new JSONArray();
        final var customBrokers = customBrokerRepository.findAll();
        if (customBrokers.isEmpty()) {
            if (log.isInfoEnabled()) {
                log.info("---- [BrokerService getRegisteredBroker] Could not find any broker");
            }
        } else {
            final var jsonObject = new JSONObject();

            for (final var broker : customBrokers) {
                if (BrokerRegistrationStatus.REGISTERED.equals(broker.getBrokerRegistrationStatus())) {
                    jsonObject.clear();
                    jsonObject.put("brokerId", broker.getBrokerUri().toString());
                    jsonArray.add(jsonObject);
                }
            }
        }
        return jsonArray;
    }
}
