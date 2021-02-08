package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.AppEndpoint;
import de.fraunhofer.iais.eis.AppEndpointType;
import de.fraunhofer.iais.eis.Language;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.CustomAppRepository;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customApp.CustomApp;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customApp.CustomAppEndpoint;
import de.fraunhofer.isst.configmanager.util.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.net.URISyntaxException;
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
    public AppService(CustomAppRepository customAppRepository) throws URISyntaxException {
        this.customAppRepository = customAppRepository;

        // If db is empty dummy apps will be created
        if (customAppRepository.count() == 0) {
            logger.info("No custom app is found! Creating custom apps.");
            List<CustomApp> customAppList = new ArrayList<>();

            // Create custom app with endpoints
            CustomApp customApp1 = new CustomApp();
            customApp1.setTitle("Custom App 1");

            List<CustomAppEndpoint> customAppEndpoints = new ArrayList<>();

            AppEndpoint appEndpoint = Utility.createAppEndpoint(AppEndpointType.INPUT_ENDPOINT, new BigInteger("80"),
                    "documentation", "information", "http://app1",
                    "iPath", "oPath", Language.DE, "PDF", "path");
            CustomAppEndpoint customAppEndpoint = new CustomAppEndpoint(appEndpoint);


            AppEndpoint appEndpoint2 = Utility.createAppEndpoint(AppEndpointType.OUTPUT_ENDPOINT, new BigInteger("81"),
                    "documentation", "information", "http://app2",
                    "iPath", "oPath", Language.DE, "JSON", "path");
            CustomAppEndpoint customAppEndpoint2 = new CustomAppEndpoint(appEndpoint2);

            customAppEndpoints.add(customAppEndpoint);
            customAppEndpoints.add(customAppEndpoint2);
            customApp1.setAppEndpointList(customAppEndpoints);
            customAppList.add(customApp1);


            // Create custom app 2 with endpoints
            CustomApp customApp2 = new CustomApp();
            customApp2.setTitle("Custom App 2");
            List<CustomAppEndpoint> customAppEndpoints2 = new ArrayList<>();
            AppEndpoint appEndpoint3 = Utility.createAppEndpoint(AppEndpointType.INPUT_ENDPOINT, new BigInteger("82"),
                    "documentation", "information", "http://app3",
                    "iPath", "oPath", Language.DE, "JSON", "path");
            CustomAppEndpoint customAppEndpoint3 = new CustomAppEndpoint(appEndpoint3);
            AppEndpoint appEndpoint4 = Utility.createAppEndpoint(AppEndpointType.OUTPUT_ENDPOINT, new BigInteger("83"),
                    "documentation", "information", "http://app4",
                    "iPath", "oPath", Language.DE, "JSON", "path");
            CustomAppEndpoint customAppEndpoint4 = new CustomAppEndpoint(appEndpoint4);

            customAppEndpoints2.add(customAppEndpoint3);
            customAppEndpoints2.add(customAppEndpoint4);
            customApp2.setAppEndpointList(customAppEndpoints2);

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
