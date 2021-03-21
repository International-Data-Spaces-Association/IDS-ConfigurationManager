package de.fraunhofer.isst.configmanager.configmanagement.entities.config;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.isst.configmanager.configmanagement.entities.converter.ConfigModelConverter;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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

    public ConfigModelObject(final ConfigurationModel configurationModel) {
        this.configurationModel = configurationModel;
    }

}
