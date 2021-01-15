package de.fraunhofer.isst.configmanager.service_test;

import de.fraunhofer.isst.configmanager.configmanagement.service.BrokerService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class BrokerServiceTest {

    @Autowired
    private BrokerService brokerService;
}
