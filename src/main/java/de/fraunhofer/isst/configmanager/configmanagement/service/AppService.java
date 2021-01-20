package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.CustomAppRepository;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customApp.CustomApp;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customApp.CustomAppEndpoint;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customApp.CustomEndpointType;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customApp.CustomLanguage;
import de.fraunhofer.isst.configmanager.util.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            customApp1.setTitle("Custom App 1");
            List<CustomAppEndpoint> customAppEndpoints = new ArrayList<>();
            CustomAppEndpoint customAppEndpoint = Utility.createCustomApp(CustomEndpointType.INPUT_ENDPOINT, 80,
                    "documentation", "information", "www.ca1.com",
                    "iPath", "oPath", CustomLanguage.DE, "JSON", "path");
            CustomAppEndpoint customAppEndpoint2 = Utility.createCustomApp(CustomEndpointType.OUTPUT_ENDPOINT, 81,
                    "documentation", "information", "www.ca2.com",
                    "iPath", "oPath", CustomLanguage.DE, "JSON", "path");
            customAppEndpoints.add(customAppEndpoint);
            customAppEndpoints.add(customAppEndpoint2);
            customApp1.setAppEndpointList(customAppEndpoints);

            CustomApp customApp2 = new CustomApp();
            customApp2.setTitle("Custom App 2");
            List<CustomAppEndpoint> customAppEndpoints2 = new ArrayList<>();
            CustomAppEndpoint customAppEndpoint3 = Utility.createCustomApp(CustomEndpointType.INPUT_ENDPOINT, 82,
                    "documentation", "information", "www.ca3.com",
                    "iPath", "oPath", CustomLanguage.DE, "JSON", "path");
            CustomAppEndpoint customAppEndpoint4 = Utility.createCustomApp(CustomEndpointType.INPUT_ENDPOINT, 83,
                    "documentation", "information", "www.ca4.com",
                    "iPath", "oPath", CustomLanguage.DE, "JSON", "path");

            customAppEndpoints2.add(customAppEndpoint3);
            customAppEndpoints2.add(customAppEndpoint4);
            customApp2.setAppEndpointList(customAppEndpoints2);

            customAppList.add(customApp1);
            customAppList.add(customApp2);

            customAppRepository.saveAll(customAppList);
        }
    }

    /**
     * This method creates an app.
     *
     * @param title title of the app
     * @return custom app
     */
    public CustomApp createApp(String title) {

        CustomApp customApp = new CustomApp();
        customApp.setTitle(title);

        customAppRepository.save(customApp);
        return customApp;
    }

    /**
     * This method updates an app with the given parameters.
     *
     * @param title title of the app
     * @return true, if app is updated
     */
    public boolean updateApp(String id, String title) {

        boolean updated = false;

        Long appId = Long.valueOf(id);
        CustomApp app = customAppRepository.findById(appId).orElse(null);

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

    public CustomApp getApp(String id) {

        Long appId = Long.valueOf(id);
        CustomApp app = customAppRepository.findById(appId).orElse(null);

        return app;
    }

    /**
     * This method deletes an app from the repository.
     *
     * @return true, if app is deleted
     */
    public boolean deleteApp(String id) {

        boolean deleted = false;

        Long appId = Long.valueOf(id);
        CustomApp app = customAppRepository.findById(appId).orElse(null);

        if (app != null) {
            customAppRepository.delete(app);
            deleted = true;
        }
        return deleted;
    }
}
