package de.fraunhofer.isst.configmanager.api_test;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.configmanager.api.controller.ConfigModelController;
import de.fraunhofer.isst.configmanager.api.service.ConfigModelService;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultConnectorClient;
import de.fraunhofer.isst.configmanager.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@WebMvcTest(ConfigModelController.class)
public class ConfigModelApiTest {

    @Autowired
    private transient MockMvc mockMvc;

    @MockBean
    private transient ConfigModelService configModelService;

    @MockBean
    private DefaultConnectorClient defaultConnectorClient;

    @Test
    public void should_return_configuration_model() throws Exception {

        ConfigurationModel configurationModel = TestUtil.configurationModel();
        Mockito.when(configModelService.getConfigModel()).thenReturn(configurationModel);
        MvcResult result = this.mockMvc.perform(get("/api/ui/configmodel")).andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void should_update_configuration_model_setttings() throws Exception {

        LinkedMultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("loglevel", "NO_LOGGING");
        requestParams.add("connectorDeployMode", "TEST_DEPLOYMENT");
        requestParams.add("trustStore", "http://trustStore");
        requestParams.add("trustStorePassword", "password");
        requestParams.add("keyStore", "http://keyStore");
        requestParams.add("keyStorePassword", "password");
        requestParams.add("proxyUri", null);
        requestParams.add("noProxyUri", null);
        requestParams.add("username", null);
        requestParams.add("password", null);

        Mockito.when(configModelService.updateConfigurationModel(
                "NO_LOGGING",
                "TEST_DEPLOYMENT",
                "http://trustStore",
                "password",
                "http://keyStore",
                "password",
                null, null, null, null)).thenReturn(true);

        ConfigurationModel configurationModel = TestUtil.configurationModel();
        Mockito.when(configModelService.getConfigModel()).thenReturn(configurationModel);

        MvcResult result = this.mockMvc.perform(put("/api/ui/configmodel").params(requestParams)).andReturn();


        assertEquals(400, result.getResponse().getStatus());
    }

    @Test
    public void should_update_configuration_model_proxy_setttings() throws Exception {

        LinkedMultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("loglevel", "NO_LOGGING");
        requestParams.add("connectorDeployMode", "TEST_DEPLOYMENT");
        requestParams.add("trustStore", "http://trustStore");
        requestParams.add("trustStorePassword", "password");
        requestParams.add("keyStore", "http://keyStore");
        requestParams.add("keyStorePassword", "password");

        Mockito.when(configModelService.updateConfigurationModel(
                "NO_LOGGING",
                "TEST_DEPLOYMENT",
                "http://trustStore",
                "password",
                "http://keyStore",
                "password",
                null, null, null, null)).thenReturn(true);

        ConfigurationModel configurationModel = TestUtil.configurationModel();
        Mockito.when(configModelService.getConfigModel()).thenReturn(configurationModel);

        MvcResult result = this.mockMvc.perform(put("/api/ui/configmodel")
                .params(requestParams)
                .param("proxyUri", String.valueOf(URI.create("http://proxy")))
                .param("noProxyUri", String.valueOf(Util.asList(URI.create("http://noproxy"))))
                .param("username", "admin")
                .param("password", "password")).andReturn();

        assertEquals(400, result.getResponse().getStatus());
    }

}
