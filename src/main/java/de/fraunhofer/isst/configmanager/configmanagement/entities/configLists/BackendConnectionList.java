package de.fraunhofer.isst.configmanager.configmanagement.entities.configLists;

import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.isst.configmanager.configmanagement.entities.backendConnection.BackendConnectionObject;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
public class BackendConnectionList {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn
    private List<BackendConnectionObject> backendConnectionObjects = new ArrayList<>();

    /**
     * @return list of endpoints
     */
    public List<Endpoint> getEndpoints() {
        return backendConnectionObjects.stream().map(BackendConnectionObject::getEndpoint).collect(Collectors.toList());
    }
}
