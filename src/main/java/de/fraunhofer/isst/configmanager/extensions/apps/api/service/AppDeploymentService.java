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
package de.fraunhofer.isst.configmanager.extensions.apps.api.service;

import de.fraunhofer.isst.configmanager.extensions.apps.api.util.LocalDockerHost;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AppDeploymentService {
    //Java-Docker-API
    final transient LocalDockerHost localDockerHost;

    public void startContainer(final String containerID) {
        if (log.isInfoEnabled()) {
            log.info("Starting the Docker Container with id: {}", containerID);
        }
        localDockerHost.getDockerClient().startContainerCmd(containerID).exec();
    }

    public void stopContainer(final String containerID) {
        if (log.isInfoEnabled()) {
            log.info("Stop the container with id: {}", containerID);
        }
        localDockerHost.getDockerClient().stopContainerCmd(containerID).exec();
    }

    public void removeContainer(final String containerID) {
        if (log.isInfoEnabled()) {
            log.info("Removing the container with id: {}", containerID);
        }
        localDockerHost.getDockerClient().removeContainerCmd(containerID).withForce(true).exec();
    }
}
