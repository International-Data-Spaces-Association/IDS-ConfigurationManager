package de.fraunhofer.isst.configmanager.data.util;

import de.fraunhofer.iais.eis.Endpoint;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

/**
 * The class helps to persist objects inherited from the endpoint from the information model.
 */
@Data
@MappedSuperclass
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BackendConfig<T extends Endpoint> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    @Convert(converter = EndpointConverter.class)
    @Column(columnDefinition = "TEXT")
    T endpoint;

    public BackendConfig(final T endpoint) {
        this.endpoint = endpoint;
    }
}
