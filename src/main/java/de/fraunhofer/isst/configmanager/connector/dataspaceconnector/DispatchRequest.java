package de.fraunhofer.isst.configmanager.connector.dataspaceconnector;

import de.fraunhofer.isst.configmanager.util.OkHttpUtils;
import lombok.experimental.UtilityClass;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@UtilityClass
public class DispatchRequest {
    final static transient OkHttpClient OK_HTTP_CLIENT = OkHttpUtils.getUnsafeOkHttpClient();

    @NotNull
    public static Response sendToDataspaceConnector(final Request request) throws IOException {
        return OK_HTTP_CLIENT.newCall(request).execute();
    }
}
