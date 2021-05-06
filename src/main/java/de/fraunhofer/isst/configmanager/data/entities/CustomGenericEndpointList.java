package de.fraunhofer.isst.configmanager.data.entities;

import de.fraunhofer.iais.eis.Endpoint;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entity class for a list of generic endpoints.
 */
@Data
@Entity
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