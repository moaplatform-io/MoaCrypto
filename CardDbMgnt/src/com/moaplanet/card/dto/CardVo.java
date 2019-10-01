package com.moaplanet.card.dto;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CardVo {
	private String MEMBER_ID			= ""; 
	private int	   SEQ					= -1; 
	private String CNO					= "";
	private String BILL_KEY				= ""; 
	private String CARD_TOKEN			= ""; 
	private String SECRET_KEY			= ""; 
	private String CARD_NICK			= ""; 
	private String USE_FLAG				= ""; 
	private Date   CREATE_DATE			= null; 
	private Date   DELETE_DATE			= null; 
	private Date   LAST_MODIFY_DATE 		= null;
	private String ISSUER_NM 		= "";
	private String ISSUER_CD 		= "";

	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
    	sb.append("\n###################");
    	sb.append("\n MEMBER_ID : ");
    	sb.append(MEMBER_ID);
    	
    	sb.append("\n SEQ : ");
    	sb.append(SEQ);
    	
    	sb.append("\n CNO : ");
    	sb.append(CNO);
    	
    	sb.append("\n BILL_KEY : ");
    	sb.append(BILL_KEY);
    	
    	sb.append("\n CARD_TOKEN : ");
    	sb.append(CARD_TOKEN);
    	
    	sb.append("\n SECRET_KEY : ");
    	sb.append(SECRET_KEY);
    	
    	sb.append("\n CARD_NICK : ");
    	sb.append(CARD_NICK);
    	
    	sb.append("\n USE_FLAG : ");
    	sb.append(USE_FLAG);
    	
    	sb.append("\n CREATE_DATE : ");
    	if (CREATE_DATE != null) sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(CREATE_DATE));
    	
    	sb.append("\n DELETE_DATE : ");
    	if (DELETE_DATE != null) sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(DELETE_DATE));
    	
    	sb.append("\n LAST_MODIFY_DATE : ");
    	if (LAST_MODIFY_DATE != null) sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(LAST_MODIFY_DATE));
    	
    	sb.append("\n ISSUER_NM : ");
    	sb.append(ISSUER_NM);
    	
    	sb.append("\n ISSUER_CD : ");
    	sb.append(ISSUER_CD);

    	return sb.toString();
	}

	public String getCNO() {
		return CNO;
	}
	public void setCNO(String cNO) {
		CNO = cNO;
	}

	public String getISSUER_CD() {
		return ISSUER_CD;
	}
	public void setISSUER_CD(String iSSUER_CD) {
		ISSUER_CD = iSSUER_CD;
	}
	public String getISSUER_NAME() {
		return ISSUER_NM;
	}
	public void setISSUER_NAME(String iSSUER_NAME) {
		ISSUER_NM = iSSUER_NAME;
	}
	public String getMEMBER_ID() {
		return MEMBER_ID;
	}
	public void setMEMBER_ID(String mEMBER_ID) {
		MEMBER_ID = mEMBER_ID;
	}
	public int getSEQ() {
		return SEQ;
	}
	public void setSEQ(int sEQ) {
		SEQ = sEQ;
	}
	public String getBILL_KEY() {
		return BILL_KEY;
	}
	public void setBILL_KEY(String bILL_KEY) {
		BILL_KEY = bILL_KEY;
	}
	public String getCARD_TOKEN() {
		return CARD_TOKEN;
	}
	public void setCARD_TOKEN(String cARD_TOKEN) {
		CARD_TOKEN = cARD_TOKEN;
	}
	public String getSECRET_KEY() {
		return SECRET_KEY;
	}
	public void setSECRET_KEY(String sECRET_KEY) {
		SECRET_KEY = sECRET_KEY;
	}
	public String getCARD_NICK() {
		return CARD_NICK;
	}
	public void setCARD_NICK(String cARD_NICK) {
		CARD_NICK = cARD_NICK;
	}
	public String getUSE_FLAG() {
		return USE_FLAG;
	}
	public void setUSE_FLAG(String uSE_FLAG) {
		USE_FLAG = uSE_FLAG;
	}
	public Date getCREATE_DATE() {
		return CREATE_DATE;
	}
	public void setCREATE_DATE(Date cREATE_DATE) {
		CREATE_DATE = cREATE_DATE;
	}
	public Date getDELETE_DATE() {
		return DELETE_DATE;
	}
	public void setDELETE_DATE(Date dELETE_DATE) {
		DELETE_DATE = dELETE_DATE;
	}
	public Date getLAST_MODIFY_DATE() {
		return LAST_MODIFY_DATE;
	}
	public void setLAST_MODIFY_DATE(Date lAST_MODIFY_DATE) {
		LAST_MODIFY_DATE = lAST_MODIFY_DATE;
	}
	
	
}
