package de.fraunhofer.isst.configmanager.configmanagement.entities.configlists;

import de.fraunhofer.isst.configmanager.configmanagement.entities.config.ConfigModelObject;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD repository for configuration model objects.
 */
public interface ConfigModelRepository extends JpaRepository<ConfigModelObject, Long> {
}
