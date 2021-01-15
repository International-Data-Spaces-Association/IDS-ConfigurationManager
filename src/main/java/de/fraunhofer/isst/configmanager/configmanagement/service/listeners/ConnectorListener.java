package de.fraunhofer.isst.configmanager.configmanagement.service.listeners;

import de.fraunhofer.iais.eis.Connector;

/**
 * Classes that want to register to ConfigModelService have to implement this interface and be registered with
 */
public interface ConnectorListener {
    
    /**
     *
     * @param connector for which a check is made to see if there have been any changes
     */
    void notifyConfig(Connector connector);

}
