package de.fraunhofer.isst.configmanager.configmanagement.entities.configlists;

import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customgenericendpoint.CustomGenericEndpointObject;
import lombok.Data;

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
 * Entity class for a list of generic endpoints
 */
@Entity
@Data
public class CustomGenericEndpointList {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn
    private List<CustomGenericEndpointObject> customGenericEndpointObjects = new ArrayList<>();

    /**
     * @return list of endpoints
     */
    public List<Endpoint> getEndpoints() {
        return customGenericEndpointObjects.stream().map(CustomGenericEndpointObject::getEndpoint).collect(Collectors.toList());
    }
}
