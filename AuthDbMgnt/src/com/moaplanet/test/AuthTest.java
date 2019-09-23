package com.moaplanet.test;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.ibatis.exceptions.PersistenceException;

import com.moaplanet.auth.dao.AuthDao;
import com.moaplanet.auth.dao.sqlmap.SqlMapAuthDao;
import com.moaplanet.auth.dto.*;
import com.moaplanet.util.MoaUtils;


public class AuthTest {

	AuthDao authDao = new SqlMapAuthDao();
	public static void main(String[] args) throws IOException {

		AuthTest authTest = new AuthTest();
		AuthVo authVo = null;
		
//		AuthVo authVo = authTest.saveAuth();
		
		authVo = authTest.selectAuth("ID_20191225_111225");
		authVo.setMoapay_pwd("변경된 pwd");
//		authTest.updateAuth(authVo);
		
    	
//	  	authTest.deleteAuth(authVo);
	}
	
	private Properties getProps(String propertiesFileName) throws IOException {
        
        return loadProperties(this.getClass().getClassLoader().getResource(propertiesFileName));
		
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

	private AuthVo saveAuth() {

		AuthVo authVo = new AuthVo();
    	authVo.setAmi("AMI 2003");
    	authVo.setAuth_fail_cnt(1);
    	authVo.setMember_id("ID_" + MoaUtils.getCurrentTimeStr("yyyymmdd_HHmmss"));
    	authVo.setCiphered_wpuk_wsalt("wsalt");
    	authVo.setCiphered_wprk("wprk");
    	authVo.setCiphered_wpsw("wpsw");
    	try {
    		int iRet = authDao.addUserAuth(authVo);
    		System.out.println("Insert Return Code : " + iRet);
    		System.out.println(authVo.getMember_id());
    	}catch(PersistenceException e) {
    		System.out.println(e.getMessage());
    	}

    	return authVo;
	}
	
	private void updateAuth(AuthVo authVo) {
		
        authDao.updateUserAuth(authVo);

	}
	
	private AuthVo selectAuth(String id) {
		
		AuthVo authVo = authDao.getUserAuth(id);
	  	System.out.println("\n" + authVo.getMember_id());
	  	System.out.println(authVo.getAmi());
	  	System.out.println(authVo.getCkmi());
	  	System.out.println(authVo.getMoapay_pwd());
	  	System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(authVo.getLast_modified_dt()));

		return authVo;
	}
	
	private void deleteAuth(AuthVo authVo) {
		authDao.deleteUserAuth(authVo);
	}
}
