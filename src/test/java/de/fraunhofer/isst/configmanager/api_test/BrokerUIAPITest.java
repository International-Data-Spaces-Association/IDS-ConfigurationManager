/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.isst.configmanager.api_test;

import de.fraunhofer.isst.configmanager.api.controller.BrokerController;
import de.fraunhofer.isst.configmanager.api.service.BrokerService;
import de.fraunhofer.isst.configmanager.connector.clients.DefaultBrokerClient;
import de.fraunhofer.isst.configmanager.data.entities.CustomBroker;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BrokerController.class)
class BrokerUIAPITest {

    @Autowired
    private transient MockMvc mockMvc;

    @MockBean
    private transient BrokerService brokerService;

    @MockBean
    private DefaultBrokerClient defaultBrokerClient;

    @Test
    void should_add_new_broker() throws Exception {

        LinkedMultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("brokerUri", "https://example.com");
        requestParams.add("title", "CustomBroker");

        doNothing().when(brokerService).createCustomBroker(URI.create("https://example.com"), "CustomBroker");

        this.mockMvc.perform(post("/api/ui/broker").params(requestParams)).andExpect(status().isOk());
    }

    @Test
    void should_update_broker() throws Exception {

        CustomBroker broker = TestUtil.createCustomBroker();

        Mockito.when(brokerService.updateBroker(broker.getBrokerUri(), "titleNew")).thenReturn(true);

        MvcResult result = this.mockMvc.perform(put("/api/ui/broker")
                .param("brokerUri", String.valueOf(broker.getBrokerUri()))
                .param("title", "titleNew"))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    void should_return_broker_list() throws Exception {

        List<CustomBroker> brokers = TestUtil.brokers();
        Mockito.when(brokerService.getCustomBrokers()).thenReturn(brokers);
        MvcResult result = this.mockMvc.perform(get("/api/ui/brokers")).andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    void should_delete_a_broker() throws Exception {

        CustomBroker customBroker = TestUtil.createCustomBroker();
        Mockito.when(brokerService.deleteBroker(Mockito.any(URI.class))).thenReturn(true);
        MvcResult result = this.mockMvc.perform(delete("/api/ui/broker")
                .param("brokerUri", String.valueOf(customBroker.getBrokerUri()))).andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

}
