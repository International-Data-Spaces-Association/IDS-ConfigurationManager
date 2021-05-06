package de.fraunhofer.isst.configmanager.appstore;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@Service
public class DataAppStoreClient implements AppStoreClient {

    final DockerClient dockerClient;

    @Value("${appstore.docker.auth.username}")
    String username;

    @Value("${appstore.docker.auth.password}")
    String password;

    @Value("${appstore.docker.auth.registry_address}")
    String registryAddress;

    public DataAppStoreClient(final @Value("${appstore.docker.host}") String dockerHost) {
        DefaultDockerClientConfig clientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost).build();
        this.dockerClient = DockerClientBuilder.getInstance(clientConfig).build();
    }

    @Override
    public boolean pullImage(String imageName) {

        boolean pulledImage = false;

        if (log.isInfoEnabled()) {
            log.info("Full image name: {}", imageName);
        }

        final var authConfig = createAuthConfig();
        final var authResponse = dockerClient.authCmd().withAuthConfig(authConfig).exec();

        if (log.isInfoEnabled()) {
            log.info("Status of authentication: {}", authResponse.getStatus());
            log.info("Pulling docker image for app store started");
        }

        try {
            pulledImage = dockerClient.pullImageCmd(imageName)
                    .withAuthConfig(authConfig)
                    .exec(new PullImageResultCallback()).awaitCompletion(300, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to pull the docker image. Occurred exception: {}", e.getMessage(), e);
            }
        }
        if (pulledImage) {
            if (log.isInfoEnabled()) {
                log.info("Success on pulling docker image");
            }
            final var images = dockerClient.listImagesCmd().exec();
            log.info(images.toString());
        }
        return pulledImage;
    }

    @Override
    public void pushImage(String imageName) {

        final var authConfig = createAuthConfig();
        final var authResponse = dockerClient.authCmd().withAuthConfig(authConfig).exec();

        if (log.isInfoEnabled()) {
            log.info("Status of authentication: {}", authResponse.getStatus());
            log.info("Pushing image to registry started");
        }

        try {
            dockerClient.pushImageCmd(imageName)
                    .withAuthConfig(authConfig)
                    .exec(new ResultCallback.Adapter<>())
                    .awaitCompletion(300, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public List<Image> getImages() {
        return dockerClient.listImagesCmd().exec();
    }

    @Override
    public void removeImage(String imageID) {
        if (log.isInfoEnabled()) {
            log.info("Removing image with id: {}", imageID);
        }
        dockerClient.removeImageCmd(imageID).withForce(true).exec();
    }

    @Override
    public List<Container> getContainers() {
        final var statusFilter = new ArrayList<String>();
        statusFilter.add("exited");
        statusFilter.add("running");
        return dockerClient.listContainersCmd().withStatusFilter(statusFilter).exec();
    }

    @Override
    public String buildContainer(String imageName) {
        if (log.isInfoEnabled()) {
            log.info("Creating a new Docker Container from the image: {}", imageName);
        }
        return dockerClient.createContainerCmd(imageName).exec().getId();
    }

    @Override
    public void startContainer(String containerID) {
        if (log.isInfoEnabled()) {
            log.info("Starting the Docker Container with id: {}", containerID);
        }
        dockerClient.startContainerCmd(containerID).exec();
    }

    @Override
    public void stopContainer(String containerID) {
        if (log.isInfoEnabled()) {
            log.info("Stop the container with id: {}", containerID);
        }
        dockerClient.stopContainerCmd(containerID).exec();
    }

    @Override
    public void removeContainer(String containerID) {
        if (log.isInfoEnabled()) {
            log.info("Removing the container with id: {}", containerID);
        }
        dockerClient.removeContainerCmd(containerID).withForce(true).exec();
    }

    /**
     * @return authorization config
     */
    private AuthConfig createAuthConfig() {
        return new AuthConfig().withUsername(username).withPassword(password)
                .withRegistryAddress(registryAddress);
    }
}
