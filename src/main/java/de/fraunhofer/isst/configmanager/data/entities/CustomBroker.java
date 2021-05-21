package de.fraunhofer.isst.configmanager.data.entities;

import de.fraunhofer.isst.configmanager.data.enums.BrokerRegistrationStatus;
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

    BrokerRegistrationStatus brokerRegistrationStatus;

    @ElementCollection
    List<String> registeredResources;

    public CustomBroker() {
    }

    public CustomBroker(final URI brokerUri) {
        this.brokerUri = brokerUri;
    }
}
