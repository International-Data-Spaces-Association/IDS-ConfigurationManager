package de.fraunhofer.isst.configmanager.configmanagement.entities.converter;

import de.fraunhofer.iais.eis.Endpoint;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;

@Converter
public class EndpointConverter implements AttributeConverter<Endpoint, String> {

    private final Serializer serializer = new Serializer();

    @Override
    public String convertToDatabaseColumn(Endpoint endpoint) {
        try {
            return serializer.serialize(endpoint);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Endpoint convertToEntityAttribute(String s) {
        try {
            return serializer.deserialize(s, Endpoint.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
