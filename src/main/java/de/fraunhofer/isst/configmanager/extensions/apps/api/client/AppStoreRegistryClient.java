/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.isst.configmanager.extensions.apps.api.client;

import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.AuthConfig;
import de.fraunhofer.isst.configmanager.extensions.apps.api.util.LocalDockerHost;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AppStoreRegistryClient {
    //Java-Docker-API
    final transient LocalDockerHost localDockerHost;

    //TODO: Read info from DB depending on selected App-Store in GUI where the App is hosted at (= App-Store-Registry info needed)
    String username;

    //TODO: Read info from DB depending on selected App-Store in GUI where the App is hosted at (= App-Store-Registry info needed)
    String password;

    //TODO: Read info from DB depending on selected App-Store in GUI where the App is hosted at (= App-Store-Registry info needed)
    String registryAddress;

    public boolean pullImage(final String imageName) {
        boolean pulledImage = false;

        if (log.isInfoEnabled()) {
            log.info("Full image name: {}", imageName);
        }

        final var authConfig = createAuthConfig();
        final var authResponse = localDockerHost.getDockerClient().authCmd().withAuthConfig(authConfig).exec();

        if (log.isInfoEnabled()) {
            log.info("Status of authentication: {}", authResponse.getStatus());
            log.info("Pulling docker image for app store started");
        }

        try {
            pulledImage = localDockerHost.getDockerClient().pullImageCmd(imageName)
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
            final var images = localDockerHost.getDockerClient().listImagesCmd().exec();
            log.info(images.toString());
        }
        return pulledImage;
    }

    /**
     * @return authorization config
     */
    private AuthConfig createAuthConfig() {
        return new AuthConfig().withUsername(username).withPassword(password)
                .withRegistryAddress(registryAddress);
    }
}
