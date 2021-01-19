package de.fraunhofer.isst.configmanager.configmanagement.entities.configLists;

import de.fraunhofer.isst.configmanager.configmanagement.entities.backendConnection.BackendConnection;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD repository for persisting backend connections.
 */
public interface BackendConnectionRepository extends JpaRepository<BackendConnection, Long> {
}
