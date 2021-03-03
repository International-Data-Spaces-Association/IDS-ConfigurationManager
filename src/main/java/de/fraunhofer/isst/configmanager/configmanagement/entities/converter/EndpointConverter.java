package de.fraunhofer.isst.configmanager.configmanagement.entities.converter;

import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;

/**
 * JPA Converter, for converting Endpoints to and from JsonLD for persisting.
 */
@Converter
public class EndpointConverter implements AttributeConverter<Endpoint, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointConverter.class);
    private final Serializer serializer = new Serializer();

    /**
     * This method serializes the endpoint, which is given in the parameter to a string, to be able to persist it
     * in the database.
     *
     * @param endpoint which is be serialized
     * @return serialized endpoint
     */
    @Override
    public String convertToDatabaseColumn(Endpoint endpoint) {
        try {
            return serializer.serialize(endpoint);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
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
    public Endpoint convertToEntityAttribute(String s) {
        try {
            return serializer.deserialize(s, Endpoint.class);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

}
