package de.fraunhofer.isst.configmanager.configmanagement.entities.config;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.isst.configmanager.configmanagement.entities.converter.ConfigModelConverter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

/**
 * Entity class for the configuration model.
 */
@Data
@NoArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigModelObject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    @Convert(converter = ConfigModelConverter.class)
    @Column(columnDefinition = "TEXT")
    ConfigurationModel configurationModel;

    public ConfigModelObject(final ConfigurationModel configurationModel) {
        this.configurationModel = configurationModel;
    }

}
