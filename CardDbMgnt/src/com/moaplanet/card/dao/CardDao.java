package com.moaplanet.card.dao;

import java.util.List;

import com.moaplanet.card.dto.CardChgPwdHisVo;
import com.moaplanet.card.dto.CardVo;

public interface CardDao {


	public List<CardVo> getUserCardList(String memberId);
	public CardVo getUserCardOne(CardVo cardVo);
	public int updateUserCard(CardVo cardVo);
	public int deleteUserCard(CardVo cardVo);
	public int addUserCard(CardVo cardVo);
	public int changePwdSimplePay(CardVo cardVo);
	public int confirmkUserCardPwd( CardVo vo);
	public int addCardChgPwdHis(CardChgPwdHisVo vo);
	
	
}
