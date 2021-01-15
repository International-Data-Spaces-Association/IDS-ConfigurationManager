package de.fraunhofer.isst.configmanager.configmanagement.entities.configLists;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.isst.configmanager.configmanagement.entities.config.ConfigModelObject;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entity class for a list of configuration models.
 */
@Entity
@Data
public class ConfigModelList {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn
    private List<ConfigModelObject> configModelObjects = new ArrayList<>();

    private int currentIndex;

    /**
     * @return list of configuration models
     */
    public List<ConfigurationModel> getConfigurationModels() {
        return configModelObjects.stream().map(ConfigModelObject::getConfigurationModel).collect(Collectors.toList());
    }

    /**
     * @return current used configuration model
     */
    public ConfigurationModel getCurrentConfigurationModel() {
        return configModelObjects.get(currentIndex).getConfigurationModel();
    }


}
