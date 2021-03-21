package de.fraunhofer.isst.configmanager.configmanagement.entities.configlists;

import de.fraunhofer.isst.configmanager.configmanagement.entities.config.RepresentationEndpointObject;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD repository for caching the resource representation associated with the endpoint
 */
public interface RepresentationEndpointRepository extends JpaRepository<RepresentationEndpointObject, Long> {

}
