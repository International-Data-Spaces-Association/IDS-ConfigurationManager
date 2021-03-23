package de.fraunhofer.isst.configmanager.configmanagement.entities.configlists;

import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customgenericendpoint.CustomGenericEndpointObject;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entity class for a list of generic endpoints
 */
@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomGenericEndpointList {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn
    List<CustomGenericEndpointObject> customGenericEndpointObjects = new ArrayList<>();

    /**
     * @return list of endpoints
     */
    public List<Endpoint> getEndpoints() {
        return customGenericEndpointObjects.stream().map(CustomGenericEndpointObject::getEndpoint).collect(Collectors.toList());
    }
}
