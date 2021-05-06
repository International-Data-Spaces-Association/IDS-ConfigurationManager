package de.fraunhofer.isst.configmanager.appstore;


import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;

import java.util.List;

public interface AppStoreClient {

    /**
     * @param imageName name of the image which will be pulled
     * @return true, if image is pulled
     */
    boolean pullImage(String imageName);

    /**
     * @param imageName id of the image, which will be pushed to a registry service
     */
    void pushImage(String imageName);

    /**
     * @return list of all used images
     */
    List<Image> getImages();

    /**
     * @param imageID id of the image
     */
    void removeImage(String imageID);

    /**
     * @return list of all used containers
     */
    List<Container> getContainers();

    /**
     * @param imageName name of the image
     */
    String buildContainer(String imageName);

    /**
     * @param containerID id of the container, which will be started
     */
    void startContainer(String containerID);

    /**
     * @param containerID id of the container, which will be stopped
     */
    void stopContainer(String containerID);

    /**
     * @param containerID id of the container, which will be removed
     */
    void removeContainer(String containerID);
}
