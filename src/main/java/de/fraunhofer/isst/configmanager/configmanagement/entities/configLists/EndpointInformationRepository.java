package de.fraunhofer.isst.configmanager.configmanagement.entities.configLists;

import de.fraunhofer.isst.configmanager.configmanagement.entities.endpointInfo.EndpointInformation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD repository for persisting endpoint information
 */
public interface EndpointInformationRepository extends JpaRepository<EndpointInformation, Long> {
}
