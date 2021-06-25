package de.fraunhofer.isst.configmanager.util.camel.dto;

import java.net.URI;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpMethod;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RouteStepEndpoint {

    URI endpointUrl;
    HttpMethod httpMethod;

}
