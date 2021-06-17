package de.fraunhofer.isst.configmanager.api_test;

import de.fraunhofer.isst.configmanager.extensions.apps.util.TestUtil;
import de.fraunhofer.isst.configmanager.extensions.components.broker.api.controller.BrokerController;
import de.fraunhofer.isst.configmanager.extensions.components.broker.api.service.BrokerService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@WebMvcTest(BrokerController.class)
class BrokerUIAPITest {

    @Autowired
    transient MockMvc mockMvc;

    @MockBean
    transient BrokerService brokerService;

    @Test
    void should_add_new_broker() throws Exception {
        final LinkedMultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("brokerUri", "https://example.com");
        requestParams.add("title", "CustomBroker");

        doNothing().when(brokerService).createCustomBroker(URI.create("https://example.com"), "CustomBroker");

        this.mockMvc.perform(post("/api/ui/broker").params(requestParams)).andExpect(status().isOk());
    }

    @Test
    void should_update_broker() throws Exception {
        final var broker = TestUtil.createCustomBroker();

        Mockito.when(brokerService.updateBroker(broker.getBrokerUri(), "titleNew")).thenReturn(true);

        final var result = this.mockMvc.perform(put("/api/ui/broker")
                .param("brokerUri", String.valueOf(broker.getBrokerUri()))
                .param("title", "titleNew"))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    void should_return_broker_list() throws Exception {
        final var brokers = TestUtil.brokers();
        Mockito.when(brokerService.getCustomBrokers()).thenReturn(brokers);

        final var result = this.mockMvc.perform(get("/api/ui/brokers")).andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    void should_delete_a_broker() throws Exception {
        final var customBroker = TestUtil.createCustomBroker();
        Mockito.when(brokerService.deleteBroker(Mockito.any(URI.class))).thenReturn(true);
        final var result = this.mockMvc.perform(delete("/api/ui/broker")
                .param("brokerUri", String.valueOf(customBroker.getBrokerUri()))).andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

}
