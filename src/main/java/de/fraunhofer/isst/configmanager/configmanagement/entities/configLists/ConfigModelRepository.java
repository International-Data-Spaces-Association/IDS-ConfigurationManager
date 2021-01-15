package de.fraunhofer.isst.configmanager.configmanagement.entities.configLists;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD repository for configuration model objects.
 */
public interface ConfigModelRepository extends JpaRepository<ConfigModelList, Long> {
}
