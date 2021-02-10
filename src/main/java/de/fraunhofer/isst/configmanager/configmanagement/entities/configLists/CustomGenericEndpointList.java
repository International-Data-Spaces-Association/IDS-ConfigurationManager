package de.fraunhofer.isst.configmanager.configmanagement.entities.configLists;

import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customGenericEndpoint.CustomGenericEndpointObject;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
