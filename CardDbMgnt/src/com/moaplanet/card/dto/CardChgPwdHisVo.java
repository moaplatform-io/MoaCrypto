package com.moaplanet.card.dto;

import java.util.Date;

public class CardChgPwdHisVo {
	private String MEMBER_ID			= ""; 
	private String CHANGE_CODE			= ""; 
	private String CHANGE_REASON		= ""; 
	private Date CREATE_DATE			= null;
	
	
	public String getMEMBER_ID() {
		return MEMBER_ID;
	}
	public void setMEMBER_ID(String mEMBER_ID) {
		MEMBER_ID = mEMBER_ID;
	}
	public String getCHANGE_CODE() {
		return CHANGE_CODE;
	}
	public void setCHANGE_CODE(String cHANGE_CODE) {
		CHANGE_CODE = cHANGE_CODE;
	}
	public String getCHANGE_REASON() {
		return CHANGE_REASON;
	}
	public void setCHANGE_REASON(String cHANGE_REASON) {
		CHANGE_REASON = cHANGE_REASON;
	}
	public Date getCREATE_DATE() {
		return CREATE_DATE;
	}
	public void setCREATE_DATE(Date cREATE_DATE) {
		CREATE_DATE = cREATE_DATE;
	} 

	
}
