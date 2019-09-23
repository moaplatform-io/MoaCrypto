package com.moaplanet.auth.dbconn;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;


public class AuthConnectionFactory {
	private static  SqlSessionFactory sqlSessionFactory;
	 
    static {
        try {
        	String res="META-INF/AuthSqlMapConfig.xml";
 
            System.out.println("####################### allocate sqlSessionFactory ##########");
            if (sqlSessionFactory == null) {
            	InputStream inputStream = new AuthConnectionFactory().getClass().getClassLoader().getResource(res).openStream();
            	
            	sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, "AuthDBConnection");
            	System.out.println("sqlSessionFactory : " + sqlSessionFactory.toString());
            	
            }
        }
        catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
    public static SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }
}
