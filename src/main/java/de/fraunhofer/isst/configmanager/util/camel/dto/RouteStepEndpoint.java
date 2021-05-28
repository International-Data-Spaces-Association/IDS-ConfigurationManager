package de.fraunhofer.isst.configmanager.util.camel.dto;

import java.net.URI;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpMethod;

@Data
@AllArgsConstructor
public class RouteStepEndpoint {

    private URI endpointUrl;

    private HttpMethod httpMethod;

}
