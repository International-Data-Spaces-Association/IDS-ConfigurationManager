package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.AppEndpoint;
import de.fraunhofer.iais.eis.AppEndpointType;
import de.fraunhofer.iais.eis.Language;
import de.fraunhofer.isst.configmanager.configmanagement.entities.configlists.CustomAppRepository;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customapp.CustomApp;
import de.fraunhofer.isst.configmanager.configmanagement.entities.customapp.CustomAppEndpoint;
import de.fraunhofer.isst.configmanager.util.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service class for managing created apps.
 */
@Service
@Slf4j
public class AppService {
    private transient final CustomAppRepository customAppRepository;

    @Autowired
    public AppService(CustomAppRepository customAppRepository) throws URISyntaxException {
        this.customAppRepository = customAppRepository;

        // If db is empty dummy apps will be created
        if (customAppRepository.count() == 0) {
            log.info("---- No custom app is found! Creating custom apps.");
            List<CustomApp> customAppList = new ArrayList<>();

            var customApp1 = new CustomApp();
            var appName = System.getenv("CUSTOM_APP_NAME");
            customApp1.setTitle(Objects.requireNonNullElse(appName, "Custom App 1"));
            log.info("---- Created custom app with title: " + customApp1.getTitle());

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


//            // Create custom app 2 with endpoints
//            CustomApp customApp2 = new CustomApp();
//            customApp2.setTitle("Custom App 2");
//            List<CustomAppEndpoint> customAppEndpoints2 = new ArrayList<>();
//            AppEndpoint appEndpoint3 = Utility.createAppEndpoint(AppEndpointType.INPUT_ENDPOINT, new BigInteger("82"),
//                    "documentation", "information", "http://app3",
//                    "iPath", "oPath", Language.DE, "JSON", "path");
//            CustomAppEndpoint customAppEndpoint3 = new CustomAppEndpoint(appEndpoint3);
//            AppEndpoint appEndpoint4 = Utility.createAppEndpoint(AppEndpointType.OUTPUT_ENDPOINT, new BigInteger("83"),
//                    "documentation", "information", "http://app4",
//                    "iPath", "oPath", Language.DE, "JSON", "path");
//            CustomAppEndpoint customAppEndpoint4 = new CustomAppEndpoint(appEndpoint4);
//
//            customAppEndpoints2.add(customAppEndpoint3);
//            customAppEndpoints2.add(customAppEndpoint4);
//            customApp2.setAppEndpointList(customAppEndpoints2);
//
//            customAppList.add(customApp2);

            customAppRepository.saveAll(customAppList);
        }
    }

    /**
     * @return list of custom apps
     */
    public List<CustomApp> getApps() {
        return customAppRepository.findAll();
    }

    public CustomApp getApp(String id) {

        var appId = Long.valueOf(id);

        return customAppRepository.findById(appId).orElse(null);
    }
}
