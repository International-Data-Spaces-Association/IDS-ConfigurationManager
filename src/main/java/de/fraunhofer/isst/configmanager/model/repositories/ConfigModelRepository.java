package de.fraunhofer.isst.configmanager.model.repositories;

import de.fraunhofer.isst.configmanager.model.config.ConfigModelObject;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD repository for configuration model objects.
 */
public interface ConfigModelRepository extends JpaRepository<ConfigModelObject, Long> {
}
