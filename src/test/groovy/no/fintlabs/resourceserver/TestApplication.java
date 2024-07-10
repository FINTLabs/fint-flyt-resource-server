package no.fintlabs.resourceserver;

import no.fintlabs.resourceserver.security.SecurityConfiguration;
import no.fintlabs.resourceserver.security.client.ClientJwtConverter;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationJwtConverter;
import no.fintlabs.resourceserver.security.userpermission.UserPermissionConsumerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(basePackageClasses = {
        ClientJwtConverter.class,
        SourceApplicationJwtConverter.class,
        SecurityConfiguration.class,
}, excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = UserPermissionConsumerConfiguration.class)
)
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

}
