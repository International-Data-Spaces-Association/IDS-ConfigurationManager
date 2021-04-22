package de.fraunhofer.isst.configmanager.data.util;

import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;

/**
 * JPA Converter, for converting Endpoints to and from JsonLD for persisting.
 */
@Slf4j
@Converter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EndpointConverter implements AttributeConverter<Endpoint, String> {
    final transient Serializer serializer = new Serializer();

    /**
     * This method serializes the endpoint, which is given in the parameter to a string, to be
     * able to persist it
     * in the database.
     *
     * @param endpoint which is be serialized
     * @return serialized endpoint
     */
    @Override
    public String convertToDatabaseColumn(final Endpoint endpoint) {
        try {
            return serializer.serialize(endpoint);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * This method is responsible for deserializing the database entry to a endpoint object.
     *
     * @param s the JSON-LD string
     * @return deserialized endpoint
     */
    @Override
    public Endpoint convertToEntityAttribute(final String s) {
        try {
            return serializer.deserialize(s, Endpoint.class);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

}
