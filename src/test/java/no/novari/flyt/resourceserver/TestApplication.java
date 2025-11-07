package no.novari.flyt.resourceserver;

import no.novari.flyt.resourceserver.security.SecurityConfiguration;
import no.novari.flyt.resourceserver.security.user.permission.UserPermissionConsumerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(
        basePackageClasses = {
                SecurityConfiguration.class,
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        UserPermissionConsumerConfiguration.class
                })
)
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

}
