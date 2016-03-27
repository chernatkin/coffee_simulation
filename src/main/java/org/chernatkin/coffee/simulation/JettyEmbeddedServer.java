package org.chernatkin.coffee.simulation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.Properties;

import org.glassfish.jersey.jetty.JettyHttpContainerFactory;

public class JettyEmbeddedServer {

    private static final String PROPERTIES_FILE = "settings.properties";
    
    public static void main(String[] args) throws Exception {
        
        final Properties props = readProperties();
        final URI baseUri = URI.create(props.getProperty("base.uri"));
        
        JettyHttpContainerFactory.createServer(baseUri, new ApplicationConfig(props))
                                 .join();
    }

    private static Properties readProperties() throws IOException {
        final InputStream inputStream = Optional.ofNullable(JettyEmbeddedServer.class
                                                                               .getClassLoader()
                                                                               .getResourceAsStream(PROPERTIES_FILE))
                                                .orElseThrow(() -> new IllegalStateException("Properties file not found"));
        
        final Properties properties = new Properties();
        properties.load(inputStream);
        inputStream.close();
        return properties;
    }

}
