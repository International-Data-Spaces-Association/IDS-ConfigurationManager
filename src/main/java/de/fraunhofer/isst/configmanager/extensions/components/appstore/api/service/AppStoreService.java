package de.fraunhofer.isst.configmanager.extensions.components.appstore.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;

/**
 * Service class for app stores operations
 */
@Service
@Transactional
public class AppStoreService {

    /**
     * The method creates a new app store.
     *
     * @param accessUrl uri of the app store
     * @param title     title of the app store
     */
    public void createAppStore(URI accessUrl, String title) {
        //TODO: persist in DB
    }

    /**
     * The method updates the app store with given accessUrl.
     *
     * @param accessUrl uri of the app store
     * @param title     title of the app store
     * @return true, when app store is updated
     */
    public boolean updateAppStore(URI accessUrl, String title) {
        //TODO: persist in DB
        return true;
    }

    /**
     * The method deletes the app store with the given acessUrl.
     *
     * @param accessUrl of the app store to be deleted
     * @return true, if accessUrl is deleted
     */
    public boolean deleteAppStore(URI accessUrl) {
        //TODO: delete from DB
        return true;
    }
    /**
     * @return list of all app stores
     */
    public List<?> getAppStores() {
        //TODO: get from DB
        return null;
    }
}
