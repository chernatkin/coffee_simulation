package org.chernatkin.coffee.simulation;

import java.util.Properties;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class ApplicationConfig extends ResourceConfig {
    
    public ApplicationConfig(Properties props) {
        register(new ApplicationBinder(props));
        packages("org.chernatkin.coffee.simulation");
        register(ObjectMapperProvider.class);
        register(JacksonFeature.class);
    }
}
