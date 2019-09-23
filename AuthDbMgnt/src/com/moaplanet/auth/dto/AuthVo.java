package com.moaplanet.auth.dto;



import java.text.SimpleDateFormat;
import java.util.Date;

public class AuthVo {
	private String member_id;
	private String ami;
	private String ckmi;
	private String salt;
	private String ciphered_pwd;
	private String auth_token;
	private String public_key;
	private int auth_fail_cnt;
	private String ciphered_wpsw;
	private String ciphered_wprk;
	private String ciphered_wpuk_wsalt;
	private Date create_dt;
	private Date last_modified_dt;
	private String moapay_pwd;

	

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("+==========================================+");
		sb.append("\ntoString          : " + this.getClass().getName());
		sb.append("\n member_id        : " + member_id);
		sb.append("\n ami              : " + ami);
		sb.append("\n ckmi             : " + ckmi);
		sb.append("\n salt             : " + salt);
		sb.append("\n ciphered_pwd     : " + ciphered_pwd);
		sb.append("\n auth_token       : " + auth_token);
		sb.append("\n public_key       : " + public_key);
		sb.append("\n auth_fail_cnt    : " + auth_fail_cnt);
		sb.append("\n ciphered_wpsw    : " + ciphered_wpsw);
		sb.append("\n ciphered_wprk    : " + ciphered_wprk);
		sb.append("\n ciphered_wpuk_wsalt : " + ciphered_wpuk_wsalt);
		sb.append("\n moapay_pwd : " + moapay_pwd);
		sb.append("\n create_dt        : " + ((create_dt == null)? "": new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(create_dt)));
		sb.append("\n last_modified_dt :" + ((last_modified_dt == null)? "": new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(last_modified_dt)));
		sb.append("\n+==========================================+");
		return sb.toString();
	}

	
	public String getCiphered_wpsw() {
		return ciphered_wpsw;
	}
	public void setCiphered_wpsw(String ciphered_wpsw) {
		this.ciphered_wpsw = ciphered_wpsw;
	}
	public String getCiphered_wprk() {
		return ciphered_wprk;
	}
	public void setCiphered_wprk(String ciphered_wprk) {
		this.ciphered_wprk = ciphered_wprk;
	}
	public String getCiphered_wpuk_wsalt() {
		return ciphered_wpuk_wsalt;
	}
	public void setCiphered_wpuk_wsalt(String ciphered_wpuk_wsalt) {
		this.ciphered_wpuk_wsalt = ciphered_wpuk_wsalt;
	}
	public String getMember_id() {
		return member_id;
	}
	public void setMember_id(String member_id) {
		this.member_id = member_id;
	}
	public String getAmi() {
		return ami;
	}
	public void setAmi(String ami) {
		this.ami = ami;
	}
	public String getCkmi() {
		return ckmi;
	}
	public void setCkmi(String ckmi) {
		this.ckmi = ckmi;
	}
	public String getSalt() {
		return salt;
	}
	public void setSalt(String salt) {
		this.salt = salt;
	}
	public String getCiphered_pwd() {
		return ciphered_pwd;
	}
	public void setCiphered_pwd(String ciphered_pwd) {
		this.ciphered_pwd = ciphered_pwd;
	}
	public String getAuth_token() {
		return auth_token;
	}
	public void setAuth_token(String auth_token) {
		this.auth_token = auth_token;
	}
	public String getPublic_key() {
		return public_key;
	}
	public void setPublic_key(String public_key) {
		this.public_key = public_key;
	}
	public int getAuth_fail_cnt() {
		return auth_fail_cnt;
	}
	public void setAuth_fail_cnt(int auth_fail_cnt) {
		this.auth_fail_cnt = auth_fail_cnt;
	}
	public Date getCreate_dt() {
		return create_dt;
	}
	public void setCreate_dt(Date create_date) {
		this.create_dt = create_date;
	}
	public Date getLast_modified_dt() {
		return last_modified_dt;
	}
	public void setLast_modified_dt(Date last_modified_dt) {
		this.last_modified_dt = last_modified_dt;
	}
	public String getMoapay_pwd() {
		return moapay_pwd;
	}
	public void setMoapay_pwd(String moapay_pwd) {
		this.moapay_pwd = moapay_pwd;
	}

	
}
