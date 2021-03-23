package de.fraunhofer.isst.configmanager.configmanagement.entities.configlists;

import de.fraunhofer.isst.configmanager.configmanagement.entities.routedeploymethod.RouteDeployMethod;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD repository for the route deploy method
 */
public interface RouteDeployMethodRepository extends JpaRepository<RouteDeployMethod, Long> {

}
