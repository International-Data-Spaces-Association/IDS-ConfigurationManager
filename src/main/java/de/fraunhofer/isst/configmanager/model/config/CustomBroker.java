package de.fraunhofer.isst.configmanager.model.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.net.URI;
import java.util.List;

/**
 * A custom broker entity, to be able to persist the broker in the intern database.
 */
@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomBroker {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    URI brokerUri;

    String title;

    BrokerStatus brokerStatus;

    @ElementCollection
    List<String> registeredResources;

    public CustomBroker() {
    }

    public CustomBroker(final URI brokerUri) {
        this.brokerUri = brokerUri;
    }
}
