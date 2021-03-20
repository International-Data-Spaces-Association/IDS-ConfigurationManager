package de.fraunhofer.isst.configmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
    public OpenAPI customOpenAPI() throws IOException {

        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            // This function may crash (e.g. ill-formatted file). Let it bubble up.
            properties.load(inputStream);
        }

        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title(properties.getProperty("title"))
                        .description(properties.getProperty("project_desc"))
                        .version(properties.getProperty("version"))
                        .contact(new Contact()
                                .name(properties.getProperty("organization_name"))
                        )
                        .license(new License()
                                .name(properties.getProperty("licence"))
                                .url(properties.getProperty("licence_url")))
                );
    }


    /**
     * This method creates a serializer bean to autowire the serializer in other places.
     *
     * @return infomodel serializer as bean for autowiring
     */
    @Bean
    public Serializer getSerializer() {
        return new Serializer();
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
