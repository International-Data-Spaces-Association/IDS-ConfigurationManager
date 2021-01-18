package de.fraunhofer.isst.configmanager.configmanagement.entities.configLists;

import de.fraunhofer.isst.configmanager.configmanagement.entities.config.CustomApp;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD repository for persisting custom app's.
 */
public interface CustomAppRepository extends JpaRepository<CustomApp, Long> {
}
