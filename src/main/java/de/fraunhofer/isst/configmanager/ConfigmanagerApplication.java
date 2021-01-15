package de.fraunhofer.isst.configmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Main class for starting the configuration manager application.
 */
@SpringBootApplication
public class ConfigmanagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigmanagerApplication.class, args);
    }

    /**
     * This method creates for the open api a custom description, which fits with the configuration manager.
     *
     * @return OpenAPi
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Configuration manager API")
                        .description("This is the IDS Configurationmanager backend API using springdoc-openapi and OpenAPI 3.")
                        .version("v2.0")
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
                );
    }


    /**
     * This method creates a serializer bean to autowire the serializer in other places.
     *
     * @return infomodel serializer as bean for autowiring
     */
    @Bean
    public Serializer getSerializer() {
        var serializer = new Serializer();
        return serializer;
    }

    /**
     * This method creates a object mapper bean to autowire it in other places.
     *
     * @return Object mapper as bean for autowiring
     */
    @Bean
    public ObjectMapper getObjectMapper() {
        var objectMapper = new ObjectMapper();
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder().build();
        objectMapper.activateDefaultTyping(ptv);
        return objectMapper;
    }

}
