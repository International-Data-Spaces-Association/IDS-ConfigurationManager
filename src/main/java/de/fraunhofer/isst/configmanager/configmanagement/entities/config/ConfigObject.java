package de.fraunhofer.isst.configmanager.configmanagement.entities.config;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.isst.configmanager.configmanagement.entities.converter.ConnectorConverter;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * The generic abstract class helps to persist objects inherited from the connector from the information model.
 */
@Data
@NoArgsConstructor
@MappedSuperclass
public abstract class ConfigObject<T extends Connector> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Convert(converter = ConnectorConverter.class)
    @Column(columnDefinition = "TEXT")
    private T connector;

    public ConfigObject(T connector) {
        this.connector = connector;
    }
}
