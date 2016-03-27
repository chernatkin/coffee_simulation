package org.chernatkin.coffee.simulation;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {
 
    private final ObjectMapper MAPPER;
 
    public ObjectMapperProvider() {
        MAPPER = new ObjectMapper();
    }
 
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return MAPPER;
    }
}
