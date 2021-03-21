package de.fraunhofer.isst.configmanager.configmanagement.entities.endpointinfo;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Entity class for persisting the endpoint with the coordinates.
 */
@Entity
@Data
public class EndpointInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String routeId;
    private String endpointId;
    private transient int xCoordinate;
    private transient int yCoordinate;

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
