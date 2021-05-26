package de.fraunhofer.isst.configmanager.data.repositories;

import de.fraunhofer.isst.configmanager.data.entities.CustomGenericEndpointList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * CRUD repository for persisting generic endpoints.
 */
@Repository
public interface CustomGenericEndpointRepository extends JpaRepository<CustomGenericEndpointList, Long> {
}
