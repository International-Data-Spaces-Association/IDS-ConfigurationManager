package de.fraunhofer.isst.configmanager.data.entities;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.isst.configmanager.data.util.ConfigModelConverter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

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
@Entity
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigModelObject {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    @Convert(converter = ConfigModelConverter.class)
    @Column(columnDefinition = "TEXT")
    ConfigurationModel configurationModel;
}
