package de.fraunhofer.isst.configmanager.model.configlists;

import de.fraunhofer.isst.configmanager.model.customapp.CustomApp;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD repository for persisting custom app's.
 */
public interface CustomAppRepository extends JpaRepository<CustomApp, Long> {
}
