package de.fraunhofer.isst.configmanager.util;

import de.fraunhofer.isst.configmanager.configmanagement.entities.config.CustomBroker;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class TestUtil {

//    /**
//     * @return broker
//     */
//    public static Broker createBroker() {
//
//        return new BrokerBuilder()
//                ._inboundModelVersion_(new ArrayList<>(List.of("1")))
//                ._outboundModelVersion_("1")
//                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
//                ._maintainer_(URI.create("maintainer"))
//                ._curator_(URI.create("curator"))
//                .build();
//    }

    public static CustomBroker createCustomBroker() {
        CustomBroker customBroker = new CustomBroker();
        customBroker.setBrokerUri(URI.create("https://example.com"));
        customBroker.setTitle("CustomBroker");
        customBroker.setSelfDeclaration("Self declaration of broker");
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

//    /**
//     * @param o object which is serialized
//     * @return jsonLD representation from the object
//     * @throws IOException exception which could be thrown during the serialization
//     */
//    public static String serializer(Object o) throws IOException {
//        Serializer serializer = new Serializer();
//        return serializer.serialize(o);
//    }

}
