package de.fraunhofer.isst.configmanager.configmanagement.entities.configLists;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * CRUD repository for persisting backend connections.
 */
@Repository
public interface CustomGenericEndpointRepository extends JpaRepository<CustomGenericEndpointList, Long> {
}
