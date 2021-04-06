package de.fraunhofer.isst.configmanager.model.customapp;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.List;

/**
 * Entity class for creating a custom app.
 */
@Data
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomApp {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    String title;

    @OneToMany(cascade = CascadeType.ALL)
    List<CustomAppEndpoint> appEndpointList;
}
