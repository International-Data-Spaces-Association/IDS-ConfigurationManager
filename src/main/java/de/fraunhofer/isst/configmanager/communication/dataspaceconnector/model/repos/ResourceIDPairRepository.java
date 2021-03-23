package de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model.repos;

import de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model.ResourceIDPair;
import org.springframework.data.jpa.repository.JpaRepository;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * This repo persists an ResourceIDPair object internally in the H2 database, which helps to
 * store the resource once
 * with the uri and once with the uuid.
 */
public interface ResourceIDPairRepository extends JpaRepository<ResourceIDPair, UUID> {

    /**
     * @param uri of the resource
     * @return list of ResourceIDPair objects
     */
    List<ResourceIDPair> findByUri(URI uri);

}
