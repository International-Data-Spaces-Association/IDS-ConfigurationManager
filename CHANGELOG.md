# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [4.x.y] - UNRELEASED

### Fixes
- Fix: For non-existing proxy configurations, "http://test" is no longer used as the default proxy configuration.

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
