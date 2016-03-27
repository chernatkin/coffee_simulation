package org.chernatkin.coffee.simulation;

import java.util.Properties;

import javax.inject.Singleton;
import javax.sql.DataSource;

import org.chernatkin.coffee.simulation.service.CoffeeDao;
import org.chernatkin.coffee.simulation.service.CoffeeService;
import org.chernatkin.coffee.simulation.service.CoffeeStatsService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class ApplicationBinder extends AbstractBinder {

    private static final String DB_URL = "db.url";
    
    private static final String DB_LOGIN = "db.login";
    
    private static final String DB_PASSWORD = "db.pwd";
    
    private static final String DB_POOL_SIZE = "db.pool.size";
    
    private final Properties props;
    
    public ApplicationBinder(Properties props) {
        this.props = props;
    }

    @Override
    protected void configure() {
        bind(CoffeeDao.class).to(CoffeeDao.class).in(Singleton.class);
        bind(CoffeeService.class).to(CoffeeService.class).in(Singleton.class);
        bind(CoffeeStatsService.class).to(CoffeeStatsService.class).in(Singleton.class);
        bindFactory(new DataSourceFactory(props.getProperty(DB_URL), 
                                          props.getProperty(DB_LOGIN),
                                          props.getProperty(DB_PASSWORD),
                                          Integer.parseInt(props.getProperty(DB_POOL_SIZE))))
                   .to(DataSource.class)
                   .in(Singleton.class);
    }
}
