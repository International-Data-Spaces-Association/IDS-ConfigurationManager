/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.isst.configmanager.data.util;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;

/**
 * A converter class for the configuration model objects.
 */
@Slf4j
@Converter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigModelConverter implements AttributeConverter<ConfigurationModel, String> {
    final transient Serializer serializer = new Serializer();

    /**
     * Converter method converts the value stored in the entity attribute into the data
     * representation
     * to be stored in the database.
     *
     * @param configurationModel which is serialized
     * @return serialized configuration model
     */
    @Override
    public String convertToDatabaseColumn(final ConfigurationModel configurationModel) {
        try {
            return serializer.serialize(configurationModel);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Converter converts the data stored in the database column into the value to be stored in
     * the entity attribute.
     *
     * @param s the JSON-LD string
     * @return configuration model
     */
    @Override
    public ConfigurationModel convertToEntityAttribute(final String s) {
        try {
            return serializer.deserialize(s, ConfigurationModel.class);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
