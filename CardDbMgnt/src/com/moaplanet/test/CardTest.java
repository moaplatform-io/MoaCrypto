package com.moaplanet.test;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import com.moaplanet.card.dao.CardDao;
import com.moaplanet.card.dao.sqlmap.SqlMapCardDao;
import com.moaplanet.card.dto.*;


public class CardTest {

	public static void main(String[] args) {
		org.apache.ibatis.logging.LogFactory.useStdOutLogging();
		
		
		String strMemId = "aaaa@naver.com";
		CardDao cardDao = new SqlMapCardDao();
		CardVo cardVo = new CardVo();
		
		
		try {
			// Insert User card 
			cardVo.setMEMBER_ID(strMemId);
			cardVo.setBILL_KEY(UUID.randomUUID().toString());
			cardVo.setCARD_TOKEN(UUID.randomUUID().toString());
			cardVo.setUSE_FLAG("Y");
			cardVo.setCARD_NICK("테스트 카드");
			
			int iRet = cardDao.addUserCard(cardVo);
			System.out.println("Insert Return Code : " + iRet);
			System.out.println(cardVo);
			System.out.println("\n\n");
			 
			
			// Select User Card List
	        List<CardVo> cardVoList = cardDao.getUserCardList(strMemId);
	        cardVo = cardVoList.get(0);
	        System.out.println(cardVo);
	        cardVo.setUSE_FLAG("Y");
	        cardVo.setBILL_KEY(cardVo.getCARD_TOKEN());
	        cardDao.updateUserCard(cardVoList.get(0));
	        printCardVo(cardVoList);

	        
	        // select user card Info One
//	        Map<String, Object> param = new HashMap<>();
//	        param.put("MEMBER_ID", strMemId);
//	        param.put("CARD_TOKEN", "9edd4d4d-522b-4cf6-a883-38ea306aa9dd");
//	        System.out.println("\n\n ## Select user Card Info Only One ##");
//	        cardVoList.clear();
//	        cardVoList.add(cardDao.getUserCardOne(param));
//	        printCardVo(cardVoList);

	    	
		}catch(Exception e) {
			e.printStackTrace();
		}
            
	}
	
	private static void printCardVo(List<CardVo> voList) {
        for(CardVo cVo : voList) {
        	System.out.println(cVo);
        }

	}

}
