package de.fraunhofer.isst.configmanager.configmanagement.entities.config;

import javax.persistence.*;
import java.util.List;

/**
 * Entity class for creating a custom app.
 */
@Entity
public class CustomApp {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;

    @OneToMany(cascade=CascadeType.ALL)
    private List<CustomAppEndpoint> appEndpointList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<CustomAppEndpoint> getAppEndpointList() {
        return appEndpointList;
    }

    public void setAppEndpointList(List<CustomAppEndpoint> appEndpointList) {
        this.appEndpointList = appEndpointList;
    }
}
