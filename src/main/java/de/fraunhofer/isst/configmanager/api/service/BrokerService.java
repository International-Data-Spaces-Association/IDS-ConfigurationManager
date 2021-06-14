package de.fraunhofer.isst.configmanager.api.service;

import de.fraunhofer.isst.configmanager.data.entities.CustomBroker;
import de.fraunhofer.isst.configmanager.data.enums.BrokerRegistrationStatus;
import de.fraunhofer.isst.configmanager.data.repositories.CustomBrokerRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;

/**
 * Service class for the custom broker.
 */
@Slf4j
@Service
@Transactional
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BrokerService {
    transient CustomBrokerRepository customBrokerRepository;

    @Autowired
    public BrokerService(final CustomBrokerRepository customBrokerRepository) {
        this.customBrokerRepository = customBrokerRepository;

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
}
