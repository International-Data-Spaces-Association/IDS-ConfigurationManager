package de.fraunhofer.isst.configmanager.configmanagement.entities.customGenericEndpoint;

import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.isst.configmanager.configmanagement.entities.converter.EndpointConverter;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * The generic abstract class helps to persist objects inherited from the endpoint from the information model.
 */
@Data
@NoArgsConstructor
@MappedSuperclass
public abstract class BackendConfig<T extends Endpoint> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Convert(converter = EndpointConverter.class)
    @Column(columnDefinition = "TEXT")
    private T endpoint;

    public BackendConfig(T endpoint) {
        this.endpoint = endpoint;
    }
}
