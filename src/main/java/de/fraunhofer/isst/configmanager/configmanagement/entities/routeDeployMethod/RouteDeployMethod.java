package de.fraunhofer.isst.configmanager.configmanagement.entities.routeDeployMethod;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class RouteDeployMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private DeployMethod deployMethod;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DeployMethod getDeployMethod() {
        return deployMethod;
    }

    public void setDeployMethod(DeployMethod deployMethod) {
        this.deployMethod = deployMethod;
    }
}
