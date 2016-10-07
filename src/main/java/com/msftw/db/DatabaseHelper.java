package com.msftw.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

public class DatabaseHelper implements AutoCloseable {
  private static Map<String, HikariDataSource> pools = new ConcurrentHashMap<String, HikariDataSource>();

  /**
   * Method to return a given datasource.
   *
   * @param config
   *          config file
   * @return Datasource
   */
  public static DataSource getDataSource(String config) {
    if (!pools.containsKey(config)) {
      createDataSource(config);
    }
    return pools.get(config);
  }

  /**
   * Private method to create new datasources.
   *
   * @param config
   *          datasource config file
   */
  @SuppressWarnings("resource")
  private static synchronized void createDataSource(String config) {
    if (!pools.containsKey(config)) {
      // create and store a datasource based on it's configuration file
      pools.put(config, new HikariDataSource(new HikariConfig(config)));
    }
  }

  /**
   * Cleanup method.
   */
  public void close() throws Exception {
    // clear connection pool for GC
    pools.clear();
  }
}
