package de.fraunhofer.isst.configmanager.model.repositories;

import de.fraunhofer.isst.configmanager.model.routedeploymethod.RouteDeployMethod;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD repository for the route deploy method.
 */
public interface RouteDeployMethodRepository extends JpaRepository<RouteDeployMethod, Long> {
}
