package de.fraunhofer.isst.configmanager.data.repositories;

import de.fraunhofer.isst.configmanager.data.entities.ConfigModelObject;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD repository for configuration model objects.
 */
public interface ConfigModelRepository extends JpaRepository<ConfigModelObject, Long> {
}
