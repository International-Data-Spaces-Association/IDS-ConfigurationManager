#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

FROM maven:3-jdk-11 AS maven

COPY pom.xml /tmp/

WORKDIR tmp
RUN mvn verify clean --fail-never

COPY src /tmp/src/

RUN mvn clean package

FROM adoptopenjdk/openjdk11:jre-11.0.8_10-alpine

LABEL org.opencontainers.image.source="https://github.com/International-Data-Spaces-Association/IDS-ConfigurationManager"

RUN mkdir /app

COPY --from=maven /tmp/target/*.jar /app/configurationmanager-8.0.0.jar

WORKDIR /app/

ENTRYPOINT ["java","-Dfile.encoding=UTF-8","-jar","configurationmanager-8.0.0.jar"]
