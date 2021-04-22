package de.fraunhofer.isst.configmanager.data.repositories;

import de.fraunhofer.isst.configmanager.data.entities.CustomBroker;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD repository for custom broker objects.
 */
public interface CustomBrokerRepository extends JpaRepository<CustomBroker, Long> {
}
