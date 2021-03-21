package de.fraunhofer.isst.configmanager.configmanagement.entities.routedeploymethod;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Entity class for the route deploy method. This class helps to manage the route deploy method
 * from every app route and subroute.
 */
@Entity
@Data
public class RouteDeployMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private DeployMethod deployMethod;
}
