package de.fraunhofer.isst.configmanager.extensions.apps.util;

import de.fraunhofer.isst.configmanager.data.entities.CustomBroker;
import lombok.experimental.UtilityClass;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for testing
 */
@UtilityClass
public class TestUtil {

    /**
     * @return custom broker
     */
    public static CustomBroker createCustomBroker() {
        final var customBroker = new CustomBroker();
        customBroker.setBrokerUri(URI.create("https://example.com"));
        customBroker.setTitle("CustomBroker");
        return customBroker;
    }

    /**
     * @return list of brokers
     */
    public static List<CustomBroker> brokers() {
        List<CustomBroker> brokers = new ArrayList<>();
        brokers.add(createCustomBroker());
        return brokers;
    }
}
