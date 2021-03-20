package de.fraunhofer.isst.configmanager.configmanagement.entities.configlists;

import de.fraunhofer.isst.configmanager.configmanagement.entities.customapp.CustomApp;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD repository for persisting custom app's.
 */
public interface CustomAppRepository extends JpaRepository<CustomApp, Long> {
}
