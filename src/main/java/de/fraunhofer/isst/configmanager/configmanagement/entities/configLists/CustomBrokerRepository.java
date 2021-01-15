package de.fraunhofer.isst.configmanager.configmanagement.entities.configLists;

import de.fraunhofer.isst.configmanager.configmanagement.entities.config.CustomBroker;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD repository for custom broker objects.
 */
public interface CustomBrokerRepository extends JpaRepository<CustomBroker, Long> {
}
