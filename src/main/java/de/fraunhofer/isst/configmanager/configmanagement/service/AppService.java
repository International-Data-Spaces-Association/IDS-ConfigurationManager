package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.isst.configmanager.configmanagement.entities.configLists.CustomAppRepository;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customApp.CustomApp;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customApp.CustomAppEndpoint;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customApp.CustomEndpointType;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customApp.CustomLanguage;
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
            CustomAppEndpoint customAppEndpoint = new CustomAppEndpoint();
            customAppEndpoint.setCustomEndpointType(CustomEndpointType.INPUT_ENDPOINT);
            customAppEndpoint.setEndpointPort(80);
            customAppEndpoint.setEndpointDocumentation("Test documentation");
            customAppEndpoint.setEndpointInformation("Test information");
            customAppEndpoint.setAccessURL("www.customapp1.com");
            customAppEndpoint.setInboundPath("testInbound");
            customAppEndpoint.setOutboundPath("testOutbound");
            customAppEndpoint.setLanguage(CustomLanguage.DE);
            customAppEndpoint.setMediaType("json");
            customAppEndpoint.setPath("pathToApp");
            customAppEndpoints.add(customAppEndpoint);
            customApp1.setAppEndpointList(customAppEndpoints);

            CustomApp customApp2 = new CustomApp();
            customApp2.setTitle("Custom App 2");
            List<CustomAppEndpoint> customAppEndpoints2 = new ArrayList<>();
            CustomAppEndpoint customAppEndpoint2 = new CustomAppEndpoint();
            customAppEndpoint2.setCustomEndpointType(CustomEndpointType.OUTPUT_ENDPOINT);
            customAppEndpoint2.setEndpointPort(81);
            customAppEndpoint2.setEndpointDocumentation("Test documentation");
            customAppEndpoint2.setEndpointInformation("Test information");
            customAppEndpoint2.setAccessURL("www.customapp2.com");
            customAppEndpoint2.setInboundPath("testInbound");
            customAppEndpoint2.setOutboundPath("testOutbound");
            customAppEndpoint2.setLanguage(CustomLanguage.DE);
            customAppEndpoint2.setMediaType("json");
            customAppEndpoint2.setPath("pathToApp2");
            customAppEndpoints2.add(customAppEndpoint2);
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
     * @param title  title of the app
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
