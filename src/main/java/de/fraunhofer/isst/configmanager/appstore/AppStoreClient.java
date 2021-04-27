package de.fraunhofer.isst.configmanager.appstore;

import com.github.dockerjava.api.model.AuthConfig;

public interface AppStoreClient {

    boolean pullAppImageFromRegistry(String imageName, AuthConfig authConfig);

}
