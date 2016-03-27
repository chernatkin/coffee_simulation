package org.chernatkin.coffee.simulation;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.glassfish.hk2.api.Factory;
import org.hsqldb.jdbc.JDBCPool;

public class DataSourceFactory implements Factory<DataSource> {

    private final String dbUrl;
   
    private final String dbLogin;
   
    private final String dbPassword;
   
    private final int poolSize;
    
    public DataSourceFactory(String dbUrl, String dbLogin, String dbPassword, int poolSize) {
        this.dbUrl = dbUrl;
        this.dbLogin = dbLogin;
        this.dbPassword = dbPassword;
        this.poolSize = poolSize;
    }

    @Override
    public DataSource provide() {
       final JDBCPool pool = new JDBCPool(poolSize);
       pool.setUrl(dbUrl);
       pool.setUser(dbLogin);
       pool.setPassword(dbPassword);
       return pool;
    }

    @Override
    public void dispose(DataSource t) {
        try {
            ((JDBCPool)t).close(5);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}