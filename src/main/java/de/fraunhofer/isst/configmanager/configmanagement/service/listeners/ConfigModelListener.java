package de.fraunhofer.isst.configmanager.configmanagement.service.listeners;

import de.fraunhofer.iais.eis.ConfigurationModel;

/**
 * Listener class to notify about changes that affect the configuration model
 */
public interface ConfigModelListener {

    /**
     * @param configurationModel for which a check is made to see if there have been any changes
     */
    void notifyConfig(ConfigurationModel configurationModel);

}
