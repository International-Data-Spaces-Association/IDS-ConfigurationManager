package de.fraunhofer.isst.configmanager.configmanagement.entities.config;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.isst.configmanager.configmanagement.entities.converter.ConfigModelConverter;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Entity class for the configuration model.
 */
@Data
@NoArgsConstructor
@Entity
public class ConfigModelObject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Convert(converter = ConfigModelConverter.class)
    @Column(columnDefinition = "TEXT")
    private ConfigurationModel configurationModel;

    public ConfigModelObject(ConfigurationModel configurationModel) {
        this.configurationModel = configurationModel;
    }

}
