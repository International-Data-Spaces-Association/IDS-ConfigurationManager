package de.fraunhofer.isst.configmanager.configmanagement.entities.backendConnection;

import com.fasterxml.jackson.annotation.JsonAlias;

import javax.validation.constraints.NotNull;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.net.URI;

/**
 * A backend connection can contain one connection instance. This specifies for example,
 * the address at which the artifact can be found.
 * This class is a type of connection instance.
 */
public class BackendArtifact extends ConnectionInstance {

    @NotNull
    @JsonAlias({"path"})
    private URI path;

    @JsonAlias({"description"})
    private BigInteger byteSize;

    @JsonAlias({"description"})
    private XMLGregorianCalendar creationDate;

    public URI getPath() {
        return path;
    }

    public void setPath(URI path) {
        this.path = path;
    }

    public BigInteger getByteSize() {
        return byteSize;
    }

    public void setByteSize(BigInteger byteSize) {
        this.byteSize = byteSize;
    }

    public XMLGregorianCalendar getCredationDate() {
        return creationDate;
    }

    public void setCreationDate(XMLGregorianCalendar creationDate) {
        this.creationDate = creationDate;
    }


}
