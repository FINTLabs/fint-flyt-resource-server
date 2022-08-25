package no.fintlabs.resourceserver;

import no.fintlabs.resourceserver.security.SecurityConfiguration;
import no.fintlabs.resourceserver.security.client.ClientJwtConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackageClasses = {
        ClientJwtConverter.class,
        SecurityConfiguration.class,
})
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

}
