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
package de.fraunhofer.isst.configmanager.extensions.apps.api.util;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
public class LocalDockerHost {
    @Getter
    @Setter
    DockerClient dockerClient;

    public LocalDockerHost() {
        //TODO: Needs to be configured depending on local setup (automated?, application.props (as before)?, which additional settings needed?)
        final var localDockerHostURI = "tcp://localhost:2375";

        final var clientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerHost(localDockerHostURI).build();
        dockerClient = DockerClientBuilder.getInstance(clientConfig).build();
    }
}
