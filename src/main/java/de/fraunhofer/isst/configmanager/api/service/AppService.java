package de.fraunhofer.isst.configmanager.api.service;

import de.fraunhofer.iais.eis.AppEndpointType;
import de.fraunhofer.iais.eis.Language;
import de.fraunhofer.isst.configmanager.data.repositories.CustomAppRepository;
import de.fraunhofer.isst.configmanager.data.entities.CustomApp;
import de.fraunhofer.isst.configmanager.data.entities.CustomAppEndpoint;
import de.fraunhofer.isst.configmanager.util.Utility;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service class for managing created apps.
 */
@Slf4j
@Service
@Transactional
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AppService {
    transient CustomAppRepository customAppRepository;

    @Autowired
    public AppService(final CustomAppRepository customAppRepository) throws URISyntaxException {
        this.customAppRepository = customAppRepository;

        // If db is empty dummy apps will be created
        if (customAppRepository.count() == 0) {
            if (log.isInfoEnabled()) {
                log.info("---- [AppService] No custom app is found! Creating custom apps.");
            }
            final List<CustomApp> customAppList = new ArrayList<>();

            final var customApp1 = new CustomApp();
            final var appName = System.getenv("CUSTOM_APP_NAME");
            customApp1.setTitle(Objects.requireNonNullElse(appName, "Custom App 1"));
            if (log.isInfoEnabled()) {
                log.info("---- [AppService] Created custom app with title: " + customApp1.getTitle());
            }

            final List<CustomAppEndpoint> customAppEndpoints = new ArrayList<>();

            final var appEndpoint = Utility.createAppEndpoint(AppEndpointType.INPUT_ENDPOINT,
                    BigInteger.valueOf(80), "documentation", "information",
                    "http://app1", "iPath", "oPath",
                    Language.DE, "PDF", "path");
            final var customAppEndpoint = new CustomAppEndpoint(appEndpoint);


            final var appEndpoint2 = Utility.createAppEndpoint(AppEndpointType.OUTPUT_ENDPOINT,
                    BigInteger.valueOf(81),
                    "documentation", "information", "http://app2",
                    "iPath", "oPath", Language.DE, "JSON", "path");
            final var customAppEndpoint2 = new CustomAppEndpoint(appEndpoint2);

            customAppEndpoints.add(customAppEndpoint);
            customAppEndpoints.add(customAppEndpoint2);
            customApp1.setAppEndpointList(customAppEndpoints);
            customAppList.add(customApp1);

            customAppRepository.saveAll(customAppList);
        }
    }

    /**
     * @return list of custom apps
     */
    public List<CustomApp> getApps() {
        return customAppRepository.findAll();
    }
}
