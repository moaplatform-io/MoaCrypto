package com.moaplanet.card.dbconn;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;


public class CardConnectionFactory {
	private static SqlSessionFactory sqlSessionFactory;
	 
    static {
        try {
        	String res="META-INF/CardSqlMapConfig.xml";
        	 
            System.out.println("####################### allocate sqlSessionFactory@CardConnectionFactory ##########");
            if (sqlSessionFactory == null) {
            	InputStream inputStream = new CardConnectionFactory().getClass().getClassLoader().getResource(res).openStream();
            	
            	sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, "envCard");
            	System.out.println("sqlSessionFactory : " + sqlSessionFactory.toString());
            }
        }
        catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }
    
    public static SqlSessionFactory getSqlSessionFactory() {
    	System.out.println("in getSqlSessionFaction! ");
        return sqlSessionFactory;
    }
}
