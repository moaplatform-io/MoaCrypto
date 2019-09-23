package com.moaplanet.test;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ReadingPropertyCopy {

	public static void main(String[] args) throws IOException {
		ReadingPropertyCopy handleConfig = new ReadingPropertyCopy();
		
		System.out.println(handleConfig.getProps("AuthSqlMapConfig.xml"));
	}
	

	private Properties getProps(String propertiesFileName) throws IOException {
        return loadProperties(this.getClass().getClassLoader().getResource("META-INF/" + propertiesFileName));
		
    }
	
	private Properties loadProperties(URL incoming) throws IOException {
		    if (incoming != null) {
		        Properties properties = new Properties();
		        properties.load(incoming.openStream());
		        return properties;
		        
		    } else {
		    	System.out.println("No URL!!");
		    	throw new IOException();
		    }
	}
}
