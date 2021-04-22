package de.fraunhofer.isst.configmanager.data.repositories;

import de.fraunhofer.isst.configmanager.data.entities.EndpointInformation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD repository for persisting endpoint information.
 */
public interface EndpointInformationRepository extends JpaRepository<EndpointInformation, Long> {
}
