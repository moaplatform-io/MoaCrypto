package com.moaplanet.card.dao.sqlmap;

import java.util.List;
import org.apache.ibatis.session.SqlSession;

import com.moaplanet.card.dao.CardDao;
import com.moaplanet.card.dbconn.CardConnectionFactory;
import com.moaplanet.card.dto.CardChgPwdHisVo;
import com.moaplanet.card.dto.CardVo;

public class SqlMapCardDao implements CardDao {

	private SqlSession getSession() {
		return CardConnectionFactory.getSqlSessionFactory().openSession(); 
	}
	
	@Override
	public int addUserCard(CardVo vo) {
		SqlSession session = getSession();
		try {
			return session.insert("addCardInfo", vo);
		} finally {
			session.commit();
			session.close();
		}
	}
	
	
	
	@Override
	// select by Token only one 
	public CardVo getUserCardOne( CardVo vo) {
		SqlSession session = null;
		try {
			session = getSession();
			CardVo cardVo = session.selectOne("getCardInfo", vo);
			System.out.println("### cardVo : " + cardVo.toString());
			
			return cardVo;
		} finally {
			session.close();
		}
	}

	@Override
	// select by memberId, secret_key
	public int confirmkUserCardPwd( CardVo vo) {
		SqlSession session = null;
		try {
			session = getSession();
			int matchCount = session.selectOne("confirmkUserCardPwd", vo);
			System.out.println("### pwd match count : " + matchCount);
			
			return matchCount;
		} finally {
			session.close();
		}
	}

	@Override
	// update by MEMBER_ID only (multi row update)
	public int changePwdSimplePay(CardVo cardVo) {
		SqlSession session = getSession();
		try {
			return session.update("changePwdSimplePay", cardVo);
		
		} finally {
			if (session != null){
				session.commit();
				session.close();
			}
		}		
	}
	
	
	
	@Override
	// update only one by Token 
	public int updateUserCard(CardVo vo) {
		SqlSession session = null;
		try {
			session = getSession();
			return session.update("updateCardInfo", vo);
		} finally {
			if (session != null){
				session.commit();
				session.close();
			}
		}
	}

	@Override
	// no action
	public int deleteUserCard(CardVo vo) {
		SqlSession session = getSession();
		try {
			return session.update("deleteCardInfo", vo);
		} finally {
			if (session != null){
				session.commit();
				session.close();
			}
		}
	}

	@Override
	// MEMBER_ID=#{MEMBER_ID} AND USE_FLAG = "Y"
     
	public List<CardVo> getUserCardList(String memberId) {
		SqlSession session = null;
		
		try {
			session = getSession();
			return session.selectList("getUserCardList", memberId);
		} finally {
			if (session != null){
				session.close();
			}
		}
	}

	@Override
	public int addCardChgPwdHis(CardChgPwdHisVo vo) {
		SqlSession session = getSession();
		try {
			return session.insert("addCardChgPwdHis", vo);
		} finally {
			session.commit();
			session.close();
		}
	}



}
