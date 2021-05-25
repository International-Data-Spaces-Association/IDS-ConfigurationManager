```
   ____                __  _         __  __
  / ___| ___   _ __   / _|(_)  __ _ |  \/  |  __ _  _ __    __ _   __ _   ___  _ __
 | |    / _ \ | '_ \ | |_ | | / _` || |\/| | / _` || '_ \  / _` | / _` | / _ \| '__|
 | |___| (_) || | | ||  _|| || (_| || |  | || (_| || | | || (_| || (_| ||  __/| |
  \____|\___/ |_| |_||_|  |_| \__, ||_|  |_| \__,_||_| |_| \__,_| \__, | \___||_|
                              |___/                               |___/
```
# Changelog

All notable changes to this project will be documented in this file.

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [7.0.0] - UNRELEASED
### Major Changes
- Remove 20 unused APIs (= 1.150 lines of code) (unused by ConfigManager-UI project)
- For the APIs of the enums, in addition to the technical name, such as DE for the language-dropdown, the written labels are now also supplied, e.g. German <-> DE. The structure of the API returns has changed accordingly
- Specification of a source-type of a backend-connection for API POST/PUT /generic/endpoint new and mandatory, removed parameter for API POST/PUT /resource/representation 

### Major Change: Infomodel Maintenance
- Major Change: Upgraded infomodel-java-artifacts 4.0.2 -> 4.0.6
- Used infomodel artifacts: java, util, serializer
- Changes the structure of the API-enums-returns like country abbreviations. These now have the URI form "http://.../code/EN" instead of simply returning an abbreviation like "EN".

### Added
- Minor Change: New API GET /api/ui/connector/status to return the accessibility-status of the Public-Connector-Endpoint
- Minor Change: New API PUT /resource/contract/update to create the contract in the configmanager and update it at the dataspace connector
- Minor Change: New setting option to address the DSC via HTTP or HTTPS. dataspace.communication.ssl=true/false in application.properties and DATASPACE_COMMUNICATION_SSL=true/false in docker-compose environment.
- Minor Change: The CM now tries to reach the connector several times during startup, with a pause of 5 seconds each time, instead of just try once. The number of tries can be set individually via application.properties or docker-compose e.g. dataspace.connector.connectionattemps=5.

### Changes
- Patch Change: Code and Architecture refactoring
- Patch Change: Docker, the Java version to be used is now fixed in the Dockerfile
- Patch Change: If running infomodel-deserialize throws IOException,  these are now explicitly logged in the DataspaceConnectorClient
- Patch Change: Added Log-Guard levels at console log-outputs and switched spring log level to warn
- Patch Change: Refactored Model/Data-Architecture

### Fixes
- Patch Change: POST /api/ui/broker/register now return success:false if connector doesn't return 200 and GET /api/ui/brokers returns not registered in this case
- Patch Change: Resources are updated at broker after they have been edited
- Patch Change: Internal Database can be reached and viewed again at: http://localhost:8081/console
- Patch Change: After editing the connector settings the broker will be updated with the new information
- Patch Change: Updated recursion methods in ResourceService that caused problems
- Patch Change: A percent sign within URLs in the UI no longer results in an error in the CM-Backend.
- Patch Change: Refactored Swagger-UI API Documentation, added all actually possible return-status-codes for API calls
- Patch Change: IDS Resources Duration Policy in (h) in edit mode is poluted with xml-coding (2^^xsd:duration instead of 2), fixed by upgrading infomodel dependencies

### Dependency Maintenance
- Dependabot: Dependabot will now automatically suggest pull requests for updates to dependencies.
- Add: org.apache.logging.log4j:log4j-web 2.14.1 (Apache 2.0)
- Add: org.postgresql:postgresql 42.2.20 (BSD-2-Clause)
- Upgrade: org.springframework.boot:spring-boot-starter-parent 2.4.2 -> 2.5.0
- Upgrade: org.springframework.security:spring-security-test 5.4.2 -> 5.5.0
- Upgrade: org.projectlombok:lombok 1.18.18 -> 1.18.20
- Upgrade: junit:junit 4.13.1 -> 4.13.2
- Upgrade: org.springdoc:springdoc-openapi-ui 1.5.7 -> 1.5.9
- Exclude: com.vaadin.external.google:android-json from org.springframework.boot:spring-boot-starter-test
- Remove: io.jsonwebtoken:jjwt 0.9.1

### Miscellaneous
- Added custom banner at application startup instead of default banner
- Disabled Swagger Petstore default playground
- Docs: Updated Readme
- Docs: Add package-info files where appropriate

## [6.0.0] - 2021-03-26

### Added
- Print version number of used ConfigManager in scheduled log
- Log incoming API calls
- Interception of "undefined" API-parameter contents for most important APIs (e.g. "undefined" resourceId)
- Print JVM system-default-charset at Jar/Image Start (should be UTF-8 to work with german umlauts)
- Print used, free and max Java Heap Space for CM

### Changes
- Major Change: if no broker has been created in CM, API /api/ui/broker/resource/information now returns empty list instead of status-code
- More detailed error logging with Class-/Methodnames where log is produced
- Code refactoring
- Increased timeout for a response from connector before readtimeout exception is thrown (30 sec instead of 10 sec before)

### Fixes
- POST /api/ui/broker/delete/resource not working
- GET /api/ui/broker/resource/information does not return brokers
- Encode API-Input/Output UTF8 (support german umlauts)
- Error 404 at PUT /contract 
- Error 404 at POST /representation. 
- DSC: Resources can now be edited and the updated information is now stored in the DSC
- Recursively update all resource changes in approutes
- Major Change: If requesting a contract fails, CM now returns a fail-message instead of agreementId: Failed
- Force use of UTF-8 for JVM for docker image start (serializer and ulauts need UTF-8 encoding)

## [5.0.0] - 2021-03-19

### Added
- New API for returning the requested resources of a connected connector (/resources/requested)
- New API for returning the enum-name of a policy for a given policy-pattern (/policy-pattern with request-body patter)
- New Docker-Compose Env-Variable CUSTOM_APP_NAME to set name for Demo-App

### Fixes
- Workaround: Use BrokerUpdateMessage and send complete catalog instead of ResourceUpdateMessage when updating a resource at a Broker
- Send Proxy Settings to Connector
- Always load resources at startup of Connector

### Changes
- Major Change: ProxySettings are now also set calling the /configmodel instead of having a additional API

## [4.1.0] - 2021-03-15

### Fixes
- Updating Resource Representation at DSC
- Endpoint-Information handling
- Sending ConfigManager Connector-Setting to DSC
- Updating Config on CM start-up with config of connected Connector
- Minor Change: Improved broker controller error handling now possible for UI
- Deleting Resources at DSC when deleted via CM
- receive complete backend-access URL (GUI fix)

### Changes
- docs: update contribution-guideline and add code-of-conduct files
- infomodel: switch used dependencies from 4.0.2-Snapshot to 4.0.2 stable-release  

## [4.0.0] - 2021-03-03

### Added

- New petri net module was created to create a petri net for defined app routes
  (can currently be visualized, will be used for validation of AppRoutes later)

### Feature

- App routes can be created to publish data or resources via defined routes
    - App routes can be created
    - Route steps can be created (with or without a resource)
    - App route deploy method can be changed
- Dummy data apps can be loaded to simulate data processing workflows
- Connector endpoints and generic endpoints can now be managed

### Fixes

- General bugfixes in the logic of the application
- API calls to the dataspace connector

### Changes
- Deleted unnecessary classes and methods 
- Reworked the code in the configuration manager

## [Unreleased]

## [1.0.1] - 2021-02-02

### Added

- POM: Licenses, clean up and version updates, infomodel 4.0.2-SNAPSHOT
- README: Update to the latest state according to POM

## [0.0.1] - 2020-07-15

### Added

- REST - API for the UI
