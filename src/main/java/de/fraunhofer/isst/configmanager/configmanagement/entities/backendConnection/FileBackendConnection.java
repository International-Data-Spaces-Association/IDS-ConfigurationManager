package de.fraunhofer.isst.configmanager.configmanagement.entities.backendConnection;

import com.fasterxml.jackson.annotation.JsonAlias;

import javax.validation.constraints.NotNull;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.net.URI;

/**
 * Special type of a backend connection.
 */
public class FileBackendConnection extends BackendConnection {

    @NotNull
    @JsonAlias({"path"})
    private URI path;

    @JsonAlias({"name"})
    private String name;

    @JsonAlias({"description"})
    private String description;

    @JsonAlias({"byteSize"})
    private BigInteger byteSize;

    @JsonAlias({"creationDate"})
    private XMLGregorianCalendar creationDate;

    public FileBackendConnection() {
    }

    public URI getPath() {
        return path;
    }

    public void setPath(URI path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigInteger getByteSize() {
        return byteSize;
    }

    public void setByteSize(BigInteger byteSize) {
        this.byteSize = byteSize;
    }

    public XMLGregorianCalendar getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(XMLGregorianCalendar creationDate) {
        this.creationDate = creationDate;
    }
}
