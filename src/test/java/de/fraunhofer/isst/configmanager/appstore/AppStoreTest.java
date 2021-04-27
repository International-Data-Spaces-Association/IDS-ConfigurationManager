package de.fraunhofer.isst.configmanager.appstore;

import com.github.dockerjava.api.model.AuthConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class AppStoreTest {

    @Test
    public void pull_image_from_docker_registry() {

        DataAppStoreClient dataAppStoreClient = new DataAppStoreClient();

        String fullImageName = "docker.pkg.github.com/international-data-spaces-association/" +
                "ids-configurationmanager/configurationmanager:6.0.0";

        AuthConfig authConfig = new AuthConfig()
                .withUsername("username")
                .withPassword("password")
                .withRegistryAddress("https://docker.pkg.github.com");

        var result = dataAppStoreClient.pullAppImageFromRegistry(fullImageName, authConfig);
        assert result;
    }
}
