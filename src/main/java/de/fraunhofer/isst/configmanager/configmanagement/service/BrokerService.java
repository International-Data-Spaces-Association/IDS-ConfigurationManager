package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.isst.configmanager.configmanagement.entities.config.BrokerStatus;
import de.fraunhofer.isst.configmanager.configmanagement.entities.config.CustomBroker;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.CustomBrokerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for the custom broker.
 */
@Service
public class BrokerService {

    private final static Logger logger = LoggerFactory.getLogger(BrokerService.class);
    private final CustomBrokerRepository customBrokerRepository;

    @Autowired
    public BrokerService(CustomBrokerRepository customBrokerRepository) {
        this.customBrokerRepository = customBrokerRepository;

        // If no broker is found in the database, a default broker is created at this point.
        if (customBrokerRepository.count() == 0) {
            logger.info("Db is empty! Creating custom broker");
            CustomBroker customBroker = new CustomBroker();
            customBroker.setBrokerUri(URI.create("https://broker.ids.isst.fraunhofer.de/infrastructure"));
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
    public CustomBroker createCustomBroker(URI brokerUri, String title) {

        CustomBroker customBroker = new CustomBroker(brokerUri);
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
    public boolean updateBroker(URI brokerUri, String title) {
        boolean updated = false;
        CustomBroker broker = getById(brokerUri);
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
        List<URI> brokerUris = new ArrayList<>();
        for (CustomBroker customBroker : customBrokerRepository.findAll()) {
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
    public boolean deleteBroker(URI id) {
        boolean deleted = false;
        CustomBroker customBroker = getById(id);
        if (customBroker != null) {
            customBrokerRepository.delete(customBroker);
            deleted = true;
        } else {
            logger.warn(String.format("Tried to delete a Broker, but no config with id %s exists!", id.toString()));
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
    public CustomBroker getById(URI id) {
        return customBrokerRepository.findAll().stream().filter(customBroker -> customBroker.getBrokerUri().equals(id))
                .findAny().orElse(null);
    }

    /**
     * This method is responsible for setting the broker status
     *
     * @param brokerId     id of the broker
     * @param brokerStatus broker status
     */
    public void setBrokerStatus(URI brokerId, BrokerStatus brokerStatus) {
        CustomBroker customBroker = getById(brokerId);
        if (customBroker != null) {
            customBroker.setBrokerStatus(brokerStatus);
            customBrokerRepository.save(customBroker);
        }
    }
}
