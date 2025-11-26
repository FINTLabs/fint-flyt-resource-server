package no.novari.flyt.resourceserver.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

/**
 * Loads the library's bundled flyt-resource-server-defaults.yml as default properties so consuming applications
 * get sensible defaults while still being able to override them.
 */
public class ResourceServerDefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "flyt-resource-server-defaults";
    private static final Resource DEFAULTS = new ClassPathResource("flyt-resource-server-defaults.yml");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!DEFAULTS.exists()) {
            return;
        }

        try {
            List<PropertySource<?>> propertySources = new YamlPropertySourceLoader()
                    .load(PROPERTY_SOURCE_NAME, DEFAULTS);

            propertySources.forEach(propertySource -> environment.getPropertySources().addLast(propertySource));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load default properties from flyt-resource-server-defaults.yml", exception);
        }
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }
}
