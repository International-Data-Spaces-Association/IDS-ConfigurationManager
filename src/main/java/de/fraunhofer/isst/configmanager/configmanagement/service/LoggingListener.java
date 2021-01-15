package de.fraunhofer.isst.configmanager.configmanagement.service;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.configmanagement.service.listeners.ConfigModelListener;
import de.fraunhofer.isst.configmanager.configmanagement.service.listeners.ConnectorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Listener for ConfigService, creates a log entry when current config is changed
 */
@Component
public class LoggingListener implements ConnectorListener, ConfigModelListener {

    private final static Logger logger = LoggerFactory.getLogger(LoggingListener.class);
    private final Serializer serializer;

    public LoggingListener(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public void notifyConfig(Connector connector) {
        try {
            logger.info("Current configuration changed: " + serializer.serialize(connector));
        } catch (IOException e) {
            logger.warn("Could not serialize current configuration!");
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    public void notifyConfig(ConfigurationModel configurationModel) {
        try {
            logger.info("Current ConfigModel changed: " + serializer.serialize(configurationModel));
        } catch (IOException e) {
            logger.warn("Could not serialize current configuration!");
            logger.warn(e.getMessage(), e);
        }
    }
}
