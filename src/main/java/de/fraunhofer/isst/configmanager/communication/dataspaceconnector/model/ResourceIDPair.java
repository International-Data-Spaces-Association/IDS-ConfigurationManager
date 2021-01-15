package de.fraunhofer.isst.configmanager.communication.dataspaceconnector.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.net.URI;
import java.util.UUID;

/**
 * The class helps to store the resource uri and the uuid in the object. This is needed because the implementation
 * refers to the dataspace connector and there the uuid is used for the unique identification.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ResourceIDPair {

    @Id
    private UUID uuid;

    @Column(unique = true)
    private URI uri;

}
