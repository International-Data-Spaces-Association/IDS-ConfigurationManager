package de.fraunhofer.isst.configmanager.configmanagement.entities.endpointinfo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Entity class for persisting the endpoint with the coordinates.
 */
@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EndpointInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    String routeId;
    String endpointId;
    int xCoordinate;
    int yCoordinate;

    public EndpointInformation() {
    }

    public EndpointInformation(final String routeId, final String endpointId, final int xCoordinate,
                               final int yCoordinate) {
        this.routeId = routeId;
        this.endpointId = endpointId;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }
}
