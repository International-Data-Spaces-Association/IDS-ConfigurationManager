package de.fraunhofer.isst.configmanager.appstore;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataAppStoreClient implements AppStoreClient {

    final DockerClient dockerClient;

    @Autowired
    public DataAppStoreClient() {
        DefaultDockerClientConfig clientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("host").build();
        this.dockerClient = DockerClientBuilder.getInstance(clientConfig).build();
    }

    @Override
    public boolean pullAppImageFromRegistry(String imageName, AuthConfig authConfig) {

        boolean pulledImage = false;

        log.info("Full image name: " + imageName);

        // Authenticate with server and necessary credentials
        log.info("Authentication configuration: " + authConfig.toString());
        var authResponse = dockerClient.authCmd().withAuthConfig(authConfig).exec();
        log.info("Status of authentication: " + authResponse.getStatus());

        log.info("Pulling docker image for app store started");
        try {
            pulledImage = dockerClient.pullImageCmd(imageName)
                    .withAuthConfig(authConfig)
                    .exec(new PullImageResultCallback()).awaitCompletion(300, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.info("Failed to pull the docker image");
            log.error(e.getMessage(), e);
        }
        if (pulledImage) {
            log.info("Success on pulling docker image");
            List<Image> images = dockerClient.listImagesCmd().exec();
            log.info(images.toString());
        }
        return pulledImage;
    }
}
