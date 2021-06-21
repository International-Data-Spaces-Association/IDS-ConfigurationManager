package de.fraunhofer.isst.configmanager.api_test;

import de.fraunhofer.isst.configmanager.extensions.components.broker.api.controller.BrokerController;
import de.fraunhofer.isst.configmanager.extensions.components.broker.api.service.BrokerService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

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
        //TODO: test vs DB
    }

    @Test
    void should_update_broker() throws Exception {
        //TODO: test vs DB
    }

    @Test
    void should_return_broker_list() throws Exception {
        //TODO: test vs DB
    }

    @Test
    void should_delete_a_broker() throws Exception {
        //TODO: test vs DB
    }
}
