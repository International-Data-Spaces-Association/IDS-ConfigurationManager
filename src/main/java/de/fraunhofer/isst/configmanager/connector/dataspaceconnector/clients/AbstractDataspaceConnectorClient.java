package de.fraunhofer.isst.configmanager.connector.dataspaceconnector.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.isst.configmanager.connector.dataspaceconnector.util.ResourceMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class AbstractDataspaceConnectorClient {
    static final Serializer SERIALIZER = new Serializer();
    static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${dataspace.connector.host}")
    transient String dataSpaceConnectorHost;

    @Value("${dataspace.connector.api.username}")
    transient String dataSpaceConnectorApiUsername;

    @Value("${dataspace.connector.api.password}")
    transient String dataSpaceConnectorApiPassword;

    @Value("${dataspace.connector.port}")
    transient Integer dataSpaceConnectorPort;

    transient String protocol;

    final transient ResourceMapper dataSpaceConnectorResourceMapper;

    String connectorBaseUrl = "";

    protected AbstractDataspaceConnectorClient(final ResourceMapper dataSpaceConnectorResourceMapper) {
        this.dataSpaceConnectorResourceMapper = dataSpaceConnectorResourceMapper;
    }

    @Autowired
    public void setProtocol(final @Value("${dataspace.communication.ssl}") String https) {
        protocol = Boolean.parseBoolean(https) ? "https" : "http";
        connectorBaseUrl = protocol + "://" + dataSpaceConnectorHost + ":" + dataSpaceConnectorPort + "/";
        if (log.isInfoEnabled()) {
            log.info("---- [AbstractDataspaceConnectorClient extended by " + this.getClass().getSimpleName() + " setProtocol]"
                    + " Communication Protocol with DataspaceConnector is: " + protocol);
        }
    }

    @NotNull
    protected Request.Builder getRequestBuilder() {
        return new Request.Builder();
    }
}
