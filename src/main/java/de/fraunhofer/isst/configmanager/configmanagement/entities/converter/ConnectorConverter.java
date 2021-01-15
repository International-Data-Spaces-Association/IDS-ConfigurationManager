package de.fraunhofer.isst.configmanager.configmanagement.entities.converter;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;

/**
 * JPA Converter, for converting Connectors to and from JsonLD for persisting.
 */
@Converter
public class ConnectorConverter implements AttributeConverter<Connector, String> {

    private final Serializer serializer = new Serializer();

    /**
     * This method serializes the connector, which is given in the parameter to a string, to be able to persist it
     * in the database.
     *
     * @param connector which is be serialized
     * @return serialized connector
     */
    @Override
    public String convertToDatabaseColumn(Connector connector) {
        try {
            return serializer.serialize(connector);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method is responsible for deserializing the database entry to a connector object.
     *
     * @param s the JSON-LD string
     * @return deserialized connector
     */
    @Override
    public Connector convertToEntityAttribute(String s) {
        try {
            return serializer.deserialize(s, Connector.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
