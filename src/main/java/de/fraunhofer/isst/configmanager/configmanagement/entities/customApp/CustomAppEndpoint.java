package de.fraunhofer.isst.configmanager.configmanagement.entities.customApp;

import javax.persistence.*;

@Entity
public class CustomAppEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private CustomApp customApp;

    private String accessURL;

    private String mediaType;

    private int endpointPort;

    private CustomEndpointType customEndpointType;

    private String endpointDocumentation;

    private String endpointInformation;

    private String inboundPath;

    private String outboundPath;

    private String path;

    private CustomLanguage language;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccessURL() {
        return accessURL;
    }

    public void setAccessURL(String accessURL) {
        this.accessURL = accessURL;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public int getEndpointPort() {
        return endpointPort;
    }

    public void setEndpointPort(int endpointPort) {
        this.endpointPort = endpointPort;
    }

    public CustomEndpointType getCustomEndpointType() {
        return customEndpointType;
    }

    public void setCustomEndpointType(CustomEndpointType customEndpointType) {
        this.customEndpointType = customEndpointType;
    }

    public String getEndpointDocumentation() {
        return endpointDocumentation;
    }

    public void setEndpointDocumentation(String endpointDocumentation) {
        this.endpointDocumentation = endpointDocumentation;
    }

    public String getEndpointInformation() {
        return endpointInformation;
    }

    public void setEndpointInformation(String endpointInformation) {
        this.endpointInformation = endpointInformation;
    }

    public String getInboundPath() {
        return inboundPath;
    }

    public void setInboundPath(String inboundPath) {
        this.inboundPath = inboundPath;
    }

    public String getOutboundPath() {
        return outboundPath;
    }

    public void setOutboundPath(String outboundPath) {
        this.outboundPath = outboundPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public CustomLanguage getLanguage() {
        return language;
    }

    public void setLanguage(CustomLanguage language) {
        this.language = language;
    }
}
