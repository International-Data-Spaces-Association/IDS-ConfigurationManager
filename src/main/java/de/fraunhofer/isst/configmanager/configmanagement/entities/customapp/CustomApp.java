package de.fraunhofer.isst.configmanager.configmanagement.entities.customapp;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Entity class for creating a custom app.
 */
@Entity
@Data
public class CustomApp {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String title;
    @OneToMany(cascade = CascadeType.ALL)
    private List<CustomAppEndpoint> appEndpointList;
}
