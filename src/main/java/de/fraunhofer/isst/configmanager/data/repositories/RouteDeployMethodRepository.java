package de.fraunhofer.isst.configmanager.data.repositories;

import de.fraunhofer.isst.configmanager.data.entities.RouteDeployMethod;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD repository for the route deploy method.
 */
public interface RouteDeployMethodRepository extends JpaRepository<RouteDeployMethod, Long> {
}
