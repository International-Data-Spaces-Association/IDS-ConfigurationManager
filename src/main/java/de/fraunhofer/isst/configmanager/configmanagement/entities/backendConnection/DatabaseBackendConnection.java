package de.fraunhofer.isst.configmanager.configmanagement.entities.backendConnection;

import com.fasterxml.jackson.annotation.JsonAlias;
import de.fraunhofer.isst.configmanager.configmanagement.entities.backendConnection.enums.DBType;

import javax.validation.constraints.NotNull;

/**
 * Special type of a backend connection.
 */
public class DatabaseBackendConnection extends BackendConnection {

    @NotNull
    @JsonAlias({"jdbcURL"})
    private String jdbcURL;

    @NotNull
    @JsonAlias({"username"})
    private String username;

    @NotNull
    @JsonAlias({"password"})
    private String password;

    @JsonAlias({"query"})
    private String query;

    @NotNull
    @JsonAlias({"dbType"})
    private DBType dbType;

    @NotNull
    @JsonAlias({"connectionInstance"})
    private ConnectionInstance connectionInstance;

    public DatabaseBackendConnection() {

    }

    public String getJdbcURL() {
        return jdbcURL;
    }

    public void setJdbcURL(String jdbcURL) {
        this.jdbcURL = jdbcURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public DBType getDbType() {
        return dbType;
    }

    public void setDbType(DBType dbType) {
        this.dbType = dbType;
    }

    public ConnectionInstance getConnectionInstance() {
        return connectionInstance;
    }

    public void setConnectionInstance(ConnectionInstance connectionInstance) {
        this.connectionInstance = connectionInstance;
    }
}
