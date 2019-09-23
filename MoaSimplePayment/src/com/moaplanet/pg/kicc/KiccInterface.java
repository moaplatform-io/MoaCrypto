package com.moaplanet.pg.kicc;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface KiccInterface {

	public String getCardRegistReturnUrl();

	public Map<String, String> getUserPayInfo(String userId);

	public int cancle(String orderId, String clientIp);

	public int payCallBack(String kiccJson);
		

}
