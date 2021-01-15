package de.fraunhofer.isst.configmanager.configmanagement.entities.configLists;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD repository for the connectors.
 */
public interface ConnectorListRepository extends JpaRepository<ConnectorList, Long> {
}
