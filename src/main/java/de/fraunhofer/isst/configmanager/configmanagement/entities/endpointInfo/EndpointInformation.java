package de.fraunhofer.isst.configmanager.configmanagement.entities.endpointInfo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Entity class for persisting the endpoint with the coordinates.
 */
@Entity
public class EndpointInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String routeId;

    private String endpointId;

    private int xCoordinate;

    private int yCoordinate;

    public EndpointInformation() {
    }

    public EndpointInformation(String routeId, String endpointId, int xCoordinate, int yCoordinate) {
        this.routeId = routeId;
        this.endpointId = endpointId;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public int getxCoordinate() {
        return xCoordinate;
    }

    public void setxCoordinate(int xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public int getyCoordinate() {
        return yCoordinate;
    }

    public void setyCoordinate(int yCoordinate) {
        this.yCoordinate = yCoordinate;
    }
}
