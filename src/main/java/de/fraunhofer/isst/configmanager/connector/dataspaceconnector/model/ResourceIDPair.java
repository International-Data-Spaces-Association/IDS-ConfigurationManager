package de.fraunhofer.isst.configmanager.connector.dataspaceconnector.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.net.URI;
import java.util.UUID;

/**
 * The class helps to store the resource uri and the uuid in the object. This is needed because
 * the implementation refers to the dataspace connector and there the uuid is used for the unique identification.
 */
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResourceIDPair {
    @Id
    UUID uuid;

    @Column(unique = true)
    URI uri;
}
