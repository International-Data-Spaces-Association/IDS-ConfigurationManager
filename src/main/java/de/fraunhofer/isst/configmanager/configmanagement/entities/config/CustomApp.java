package de.fraunhofer.isst.configmanager.configmanagement.entities.config;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.net.URI;

/**
 * Entity class for creating a custom app.
 */
@Entity
public class CustomApp {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private URI appUri;

    private String title;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public URI getAppUri() {
        return appUri;
    }

    public void setAppUri(URI appUri) {
        this.appUri = appUri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
