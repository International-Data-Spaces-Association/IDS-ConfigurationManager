package de.fraunhofer.isst.configmanager.configmanagement.entities.converter;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;

/**
 * A converter class for the configuration model objects.
 */
@Converter
public class ConfigModelConverter implements AttributeConverter<ConfigurationModel, String> {

    private final Serializer serializer = new Serializer();

    /**
     * Converter method converts the value stored in the entity attribute into the data representation
     * to be stored in the database.
     *
     * @param configurationModel which is serialized
     * @return serialized configuration model
     */
    @Override
    public String convertToDatabaseColumn(ConfigurationModel configurationModel) {
        try {
            return serializer.serialize(configurationModel);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converter converts the data stored in the database column into the value to be stored in the entity attribute.
     *
     * @param s the JSON-LD string
     * @return configuration model
     */
    @Override
    public ConfigurationModel convertToEntityAttribute(String s) {
        try {
            return serializer.deserialize(s, ConfigurationModel.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
