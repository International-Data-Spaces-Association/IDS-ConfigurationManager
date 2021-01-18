package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.isst.configmanager.configmanagement.entities.config.CustomApp;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.CustomAppRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing created apps.
 */
@Service
public class AppService {

    private final static Logger logger = LoggerFactory.getLogger(AppService.class);
    private final CustomAppRepository customAppRepository;

    @Autowired
    public AppService(CustomAppRepository customAppRepository) {
        this.customAppRepository = customAppRepository;

        // If db is empty dummy apps will be created
        if (customAppRepository.count() == 0) {
            logger.info("No custom app is found! Creating custom apps.");
            List<CustomApp> customAppList = new ArrayList<>();

            CustomApp customApp1 = new CustomApp();
            customApp1.setAppUri(URI.create("www.customApp1.com"));
            customApp1.setTitle("Custom App 1");

            CustomApp customApp2 = new CustomApp();
            customApp2.setAppUri(URI.create("www.customApp2.com"));
            customApp2.setTitle("Custom App 2");

            customAppList.add(customApp1);
            customAppList.add(customApp2);

            customAppRepository.saveAll(customAppList);
        }
    }

    /**
     * This method creates an app.
     *
     * @param appUri uri of the app
     * @param title  title of the app
     * @return custom app
     */
    public CustomApp createApp(URI appUri, String title) {

        CustomApp customApp = new CustomApp();
        customApp.setAppUri(appUri);
        customApp.setTitle(title);

        customAppRepository.save(customApp);
        return customApp;
    }

    /**
     * This method updates an app with the given parameters.
     *
     * @param appUri uri of the app
     * @param title  title of the app
     * @return true, if app is updated
     */
    public boolean updateApp(URI appUri, String title) {

        boolean updated = false;

        CustomApp app = customAppRepository.findAll()
                .stream()
                .filter(customApp -> customApp.getAppUri().equals(appUri))
                .findAny().orElse(null);

        if (app != null) {
            if (title != null) {
                app.setTitle(title);
                customAppRepository.save(app);
                updated = true;
            }
        }
        return updated;
    }

    /**
     * @return list of custom apps
     */
    public List<CustomApp> getApps() {
        return customAppRepository.findAll();
    }

    public CustomApp getApp(URI appUri) {

        return customAppRepository
                .findAll()
                .stream()
                .filter(customApp -> customApp.getAppUri().equals(appUri))
                .findAny().orElse(null);
    }

    /**
     * This method deletes an app from the repository.
     *
     * @param appUri uri of the app
     * @return true, if app is deleted
     */
    public boolean deleteApp(URI appUri) {

        boolean deleted = false;

        CustomApp app = customAppRepository
                .findAll().stream()
                .filter(customApp -> customApp.getAppUri().equals(appUri)).findAny().orElse(null);

        if (app != null) {
            customAppRepository.delete(app);
            deleted = true;
        }
        return deleted;
    }
}
