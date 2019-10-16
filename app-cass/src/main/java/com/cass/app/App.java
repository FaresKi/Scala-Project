package com.cass.app;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * Hello world!
 *
 */ 
public class App 
{
  public static void main(String[] args){
    Cluster cluster;
    cluster = Cluster.builder().addContactPoint("127.0.0.1").withCredentials("cassandra","cassandra").build();
    Session session;
    session = cluster.connect();
    ResultSet rs = session.execute("Select Count(*) as total from scala_project.drone_messages where status = '0' ALLOW FILTERING");
    for(Row row:rs){
      System.out.println((int) (row.getLong("total")));
    }
      
  }       
}