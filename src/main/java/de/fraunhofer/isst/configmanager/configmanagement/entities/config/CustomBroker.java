package de.fraunhofer.isst.configmanager.configmanagement.entities.config;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.net.URI;

/**
 * A custom broker entity, to be able to persist the broker in the intern database.
 */
@Entity
public class CustomBroker {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private URI brokerUri;

    private String title;

    private BrokerStatus brokerStatus;

    public CustomBroker() {
    }

    public CustomBroker(URI brokerUri) {
        this.brokerUri = brokerUri;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public URI getBrokerUri() {
        return brokerUri;
    }

    public void setBrokerUri(URI brokerUri) {
        this.brokerUri = brokerUri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BrokerStatus getBrokerStatus() { return brokerStatus; }

    public void setBrokerStatus(BrokerStatus brokerStatus) { this.brokerStatus = brokerStatus; }
}
