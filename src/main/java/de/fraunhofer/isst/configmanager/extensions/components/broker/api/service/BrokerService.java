package de.fraunhofer.isst.configmanager.extensions.components.broker.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;

/**
 * Service class for the broker extension.
 */
@Service
@Transactional
public class BrokerService {
    /**
     * The method creates a custom broker.
     *
     * @param brokerUri uri of the broker
     * @param title     title of the broker
     */
    public void createBroker(final URI brokerUri, final String title) {
        //TODO: Save to DB
    }

    /**
     * The method updates the given broker.
     *
     * @param brokerUri uri of the broker
     * @param title     title of the broker
     * @return true, when broker is updated
     */
    public boolean updateBroker(final URI brokerUri, final String title) {
        //TODO: Save to DB
        return true;
    }

    /**
     * The method deletes the broker using the broker id.
     *
     * @param id of the broker which is deleted
     * @return true, if broker is deleted
     */
    public boolean deleteBroker(final URI id) {
        //TODO: Delete from DB
        return true;
    }

    /**
     * @return list of all custom brokers
     */
    public List<?> getBrokers() {
        //TODO: Get from DB
        return null;
    }
}
