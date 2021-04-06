package de.fraunhofer.isst.configmanager.model.configlists;

import de.fraunhofer.isst.configmanager.model.endpointinfo.EndpointInformation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD repository for persisting endpoint information.
 */
public interface EndpointInformationRepository extends JpaRepository<EndpointInformation, Long> {
}
