package de.fraunhofer.isst.configmanager.data.repositories;

import de.fraunhofer.isst.configmanager.data.entities.CustomApp;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD repository for persisting custom app's.
 */
public interface CustomAppRepository extends JpaRepository<CustomApp, Long> {
}
