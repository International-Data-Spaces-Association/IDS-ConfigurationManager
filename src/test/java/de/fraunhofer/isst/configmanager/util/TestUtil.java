package de.fraunhofer.isst.configmanager.util;

import de.fraunhofer.isst.configmanager.configmanagement.entities.config.CustomBroker;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class TestUtil {
    public static CustomBroker createCustomBroker() {
        CustomBroker customBroker = new CustomBroker();
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
