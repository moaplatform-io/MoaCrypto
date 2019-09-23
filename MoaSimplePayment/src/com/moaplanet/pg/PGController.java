package com.moaplanet.pg;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.moaplanet.card.dao.CardDao;
import com.moaplanet.card.dao.sqlmap.SqlMapCardDao;
import com.moaplanet.pg.kicc.KiccInterface;



/*
// 카드 관리(빌키발급,결제,취소 등)
// Card Table 관리
// PG사 변경에 대비한 Interface 생성 및 사용
*/
public class PGController {

	private static KiccInterface b2cCallBack = null;
	private static ActionHandler actionHandler = null;
	private String clientIp;

	public PGController() {
		
	}
	public PGController(KiccInterface b2cCallback) {
		System.out.println("b2cCallback : " + b2cCallback);
		if (b2cCallBack == null)
			b2cCallBack = b2cCallback;
		
		if (actionHandler == null)
		{
			System.out.println("actionHandler create##");
			CardDao cardDao = new SqlMapCardDao();
			actionHandler = new ActionHandlerImpl(cardDao);
		}
	}
	
	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}
	
	public static KiccInterface getB2cCallBack() {
		System.out.println("b2cCallBack : " + b2cCallBack);
		return b2cCallBack;
	}

	
	// 
	public String handleCardRequest(String jsonRequestData) {
		
		if (jsonRequestData.length() < 10){
			return "Request is Worng";
		}

		try {
			JsonParser jsonParser = new JsonParser();
	       	JsonObject requestJsonObj = (JsonObject) jsonParser.parse(jsonRequestData);
	       	return handleCardRequest(requestJsonObj);
		}catch (JsonSyntaxException e) {
			return "{result=400, resultMsg=JsonSyntaxException}";
		}
	}
	
	public String handleCardRequest(JsonObject jsonObjectRequest) {
        try {
        	JsonObject requestJsonObj = jsonObjectRequest;
        	requestJsonObj.addProperty("client_ip", clientIp);
        	
        	JsonObject resultJsonObj = cloneJsonCommon(requestJsonObj);
        	
        	actionHandler.switchAction(requestJsonObj, resultJsonObj);
			
			return resultJsonObj.toString();
			
		} catch ( Exception e) {
			JsonObject resultJsonObj = new JsonObject();
			resultJsonObj.addProperty(ActionHandler.ACTION_RESULT, ActionHandler.ACTION_FAIL);
			resultJsonObj.addProperty(ActionHandler.ACTION_RESULT_MSG, "Handler Bad Request : " + e.getMessage());
        	
			return resultJsonObj.toString();
		}
	}
	
	
	private JsonObject cloneJsonCommon(JsonObject jsonObj) throws Exception{
		try {
			JsonObject returnObject = new JsonObject();
			
			returnObject.addProperty(ActionHandler.ACTION_TYPE, jsonObj.get(ActionHandler.ACTION_TYPE).getAsString());
			returnObject.addProperty(ActionHandler.ACTION_STEP, jsonObj.get(ActionHandler.ACTION_STEP).getAsString());
			
			return returnObject;
		}catch(Exception e) {
			throw new Exception("PGE001");
		}
	}
			

	// KICC로부터 빌키 발급 후 호출
	public String handleRegistBillKey(String jsonReqeustData) {
		JsonParser jsonParser = new JsonParser();
		JsonObject requestJsonObj = (JsonObject) jsonParser.parse(jsonReqeustData);
		
		requestJsonObj.addProperty(ActionHandler.ACTION_TYPE, ActionHandler.ACTION_TYPE_CARD_REGIST);
		requestJsonObj.addProperty(ActionHandler.ACTION_STEP, "2");
		
		
		return handleCardRequest(requestJsonObj);
	}
	
	public String initializePasswordStep2(String userId, String kgId, String nonce) {
		return actionHandler.initializePasswordStep2(userId, kgId, nonce);
	}
	
	public String initializePasswordFail(String msg) {
		
		JsonObject responseJsonObj = new JsonObject();
		
		responseJsonObj.addProperty(ActionHandler.ACTION_TYPE, ActionHandler.ACTION_TYPE_INITIALIZE_PASSWORD);
		responseJsonObj.addProperty(ActionHandler.ACTION_STEP, "2");
		responseJsonObj.addProperty(ActionHandler.ACTION_RESULT, ActionHandler.ACTION_FAIL);
		responseJsonObj.addProperty(ActionHandler.ACTION_RESULT_MSG, "Server Msg : " + msg);		
		
		return responseJsonObj.toString();
	}
}
