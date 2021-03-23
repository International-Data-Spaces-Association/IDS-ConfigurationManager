package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.isst.configmanager.configmanagement.entities.config.BrokerStatus;
import de.fraunhofer.isst.configmanager.configmanagement.entities.config.CustomBroker;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configlists.CustomBrokerRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for the custom broker.
 */
@Service
@Slf4j
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
            log.info("---- [BrokerService] Db is empty! Creating custom broker");
            final var customBroker = new CustomBroker();
            customBroker.setBrokerUri(URI.create("https://broker.ids.isst.fraunhofer" +
                    ".de/infrastructure"));
            customBroker.setTitle("IDS Broker");
            customBroker.setBrokerStatus(BrokerStatus.UNREGISTERED);
            customBrokerRepository.save(customBroker);
        }
    }

    /**
     * The method creates a custom broker.
     *
     * @param brokerUri uri of the broker
     * @param title     title of the broker
     * @return custom broker
     */
    public CustomBroker createCustomBroker(final URI brokerUri, final String title) {

        final var customBroker = new CustomBroker(brokerUri);
        if (title != null) {
            customBroker.setTitle(title);
            customBroker.setBrokerStatus(BrokerStatus.UNREGISTERED);
        }
        customBrokerRepository.save(customBroker);

        return customBroker;
    }

    /**
     * The method updates the given broker.
     *
     * @param brokerUri uri of the broker
     * @param title     title of the broker
     * @return true, when broker is updated
     */
    public boolean updateBroker(final URI brokerUri, final String title) {
        boolean updated = false;
        final var broker = getById(brokerUri);
        if (broker != null) {
            if (title != null) {
                broker.setTitle(title);
            }
            updated = true;
        }
        customBrokerRepository.save(broker);
        return updated;
    }

    /**
     * The method returns a list of all broker uri's
     *
     * @return list of all broker uri's
     */
    public List<URI> getAllBrokerUris() {
        final List<URI> brokerUris = new ArrayList<>();
        for (var customBroker : customBrokerRepository.findAll()) {
            if (customBroker != null) {
                brokerUris.add(customBroker.getBrokerUri());
            }
        }
        return brokerUris;
    }

    /**
     * The method deletes the broker using the broker id.
     *
     * @param id of the broker which is deleted
     * @return true, if broker is deleted
     */
    public boolean deleteBroker(final URI id) {
        boolean deleted = false;
        final var customBroker = getById(id);
        if (customBroker != null) {
            customBrokerRepository.delete(customBroker);
            deleted = true;
        } else {
            log.warn(String.format("---- [BrokerService deleteBroker] Tried to delete a Broker, but no config with id %s " +
                    "exists!", id.toString()));
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
     * This method is responsible for setting the broker status
     *
     * @param brokerId     id of the broker
     * @param brokerStatus broker status
     */
    public void setBrokerStatus(final URI brokerId, final BrokerStatus brokerStatus) {
        final var customBroker = getById(brokerId);
        if (customBroker != null) {
            customBroker.setBrokerStatus(brokerStatus);
            customBrokerRepository.save(customBroker);
        }
    }

    /**
     * This method set a resource at a broker.
     *
     * @param brokerId   id of the broker
     * @param resourceId id of the resource
     */
    public void setResourceAtBroker(final URI brokerId, final URI resourceId) {
        final var customBroker = getById(brokerId);
        if (customBroker != null) {
            if (customBroker.getRegisteredResources() == null) {
                customBroker.setRegisteredResources(new ArrayList<>());
            }
            final var registeredResources = customBroker.getRegisteredResources();
            registeredResources.add(resourceId.toString());
            customBroker.setRegisteredResources(registeredResources);
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
     * This method deletes the resource at the broker
     *
     * @param brokerUri  id of the broker
     * @param resourceId id of the resource
     */
    public void deleteResourceAtBroker(final URI brokerUri, final URI resourceId) {
        final var customBroker = getById(brokerUri);
        if (customBroker != null) {
            if (customBroker.getRegisteredResources() == null) {
                log.info("---- [BrokerService deleteResourceAtBroker] Could not found any resource to delete");
            } else {
                final var registeredResources = customBroker.getRegisteredResources();
                registeredResources.removeIf(s -> s.equals(resourceId.toString()));
                customBroker.setRegisteredResources(registeredResources);
                customBrokerRepository.save(customBroker);
            }
        }
    }

    /**
     * This method creates a JSON for the registration status of a resource at a broker
     *
     * @param resourceId id of the resource
     * @return jsonObject
     */
    public JSONArray getRegisStatusForResource(final URI resourceId) {

        final var customBrokers = customBrokerRepository.findAll();
        if (customBrokers.isEmpty()) {
            log.info("---- [BrokerService getRegisStatusForResource] Could not find any broker");
        } else {
            final var jsonArray = new JSONArray();
            final var jsonObject = new JSONObject();
            for (var customBroker : customBrokers) {
                if (customBroker.getRegisteredResources() != null) {
                    for (var id : customBroker.getRegisteredResources()) {
                        if (resourceId.toString().equals(id)) {
                            jsonObject.clear();
                            jsonObject.put("brokerId", customBroker.getBrokerUri().toString());
                            jsonObject.put("brokerStatus",
                                    customBroker.getBrokerStatus().toString());
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
}
