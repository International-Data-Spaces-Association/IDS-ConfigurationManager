package de.fraunhofer.isst.configmanager.configmanagement.entities.config;

import lombok.Data;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.net.URI;
import java.util.List;

/**
 * A custom broker entity, to be able to persist the broker in the intern database.
 */
@Entity
@Data
public class CustomBroker {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private URI brokerUri;
    private String title;
    private BrokerStatus brokerStatus;
    @ElementCollection
    private List<String> registeredResources;

    public CustomBroker() {
    }

    public CustomBroker(final URI brokerUri) {
        this.brokerUri = brokerUri;
    }
}
