package com.moaplanet.auth.dao.sqlmap;

import java.sql.SQLException;

import org.apache.ibatis.session.SqlSession;

import com.moaplanet.auth.dao.AuthDao;
import com.moaplanet.auth.dbconn.AuthConnectionFactory;
import com.moaplanet.auth.dto.AuthVo;

public class SqlMapAuthDao implements AuthDao {

	private SqlSession getSession() {
		return getSession(true); 
	}
	
	private SqlSession getSession(Boolean bCommit) {
		return AuthConnectionFactory.getSqlSessionFactory().openSession(bCommit); 
	}
	
	@Override
	public int addUserAuth(AuthVo authVo) {
		SqlSession session = getSession();
		try {
			return session.insert("addAuthInfo", authVo);
			
		} finally {
			session.commit();
			session.close();
		}
		
		
	}
	
	@Override
	public AuthVo getUserAuth(String memberId) {
		SqlSession session = null;
		try {
			session = getSession();
			System.out.println("session : " + session.toString());
			return session.selectOne("getAuthInfo", memberId);
			
		} finally {
			session.close();
		}
	}

	@Override
	public int updateUserAuth(AuthVo authVo) {
		SqlSession session = null;
		try {
			session = getSession();
			return session.update("updateAuthInfo", authVo);
			
		} finally {
			session.commit();
			session.close();
		}
	}

	@Override
	public int deleteUserAuth(AuthVo authVo) {
		SqlSession session = getSession(false);
		
		try {
			if (session.update("addAuthInfoHist", authVo) == 0 
			   || session.update("deleteAuthInfo", authVo) ==0) {
				
				session.rollback();
				return 0;
				
			} else {
				return 1;
			}
			
		} finally {
			session.commit();
			session.close();
		}
	}

}
