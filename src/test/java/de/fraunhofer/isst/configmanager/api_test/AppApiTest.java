package de.fraunhofer.isst.configmanager.api_test;

import de.fraunhofer.isst.configmanager.api.controller.AppController;
import de.fraunhofer.isst.configmanager.api.service.AppService;
import de.fraunhofer.isst.configmanager.model.customapp.CustomApp;
import de.fraunhofer.isst.configmanager.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(AppController.class)
public class AppApiTest {

    @Autowired
    private transient MockMvc mockMvc;

    @MockBean
    private transient AppService appService;

    @Test
    public void should_return_custom_app_list() throws Exception {

        List<CustomApp> customApps = TestUtil.apps();
        Mockito.when(appService.getApps()).thenReturn(customApps);
        MvcResult result = this.mockMvc.perform(get("/api/ui/apps")).andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }
}
