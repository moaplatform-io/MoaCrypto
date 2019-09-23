package com.moaplanet.pg;

import com.google.gson.JsonObject;

public interface ActionHandler {
	public static final String ACTION_TYPE = "actionType";
	public static final String ACTION_STEP = "step";
	public static final String ACTION_RESULT = "result";
	public static final String ACTION_RESULT_MSG = "resultMsg";
	public static final String ACTION_SUCCESS = "200";
	public static final String ACTION_SUCCESS_MSG = "성공";
	
	public static final String ACTION_FAIL = "400";
	
	public static final String ACTION_YES = "Y";
	
	public static final String ACTION_INITIAL_PASSWORD_CODE = "001"; //KG_MobileAuth 
	public static final String ACTION_CHANGE_PASSWORD_CODE = "002"; //UserRequestChangePassword
	public static final String ACTION_CHANGE_PASSWORD_REASON = "UserRequestChangePassword";
	
	public static final String ACTION_TYPE_CARD_REGIST = "CardRegist";
	public static final String ACTION_TYPE_SELECT_CARD_LIST = "SelectCardList";
	public static final String ACTION_TYPE_REQUEST_PAY = "RequestPay";
	public static final String ACTION_TYPE_CHANGE_PASSWORD = "ChangePwdSimplePay";
	public static final String ACTION_TYPE_DELETE_CARD_ONE = "DeleteCardOne";
	public static final String ACTION_TYPE_CHANGE_CARDNICK = "ChangeCardNickname";
	public static final String ACTION_TYPE_INITIALIZE_PASSWORD = "InitializePwdSimplePay";
	
	
	
	
	public static final String ACTION_PARAM_MEMBERID = "memberId";
	public static final String ACTION_PARAM_CARD_NICK = "cardNick";
	public static final String ACTION_PARAM_TOKEN = "token";
	public static final String ACTION_PARAM_CARD_LIST = "cardList";
	public static final String ACTION_PARAM_PAY_KIND = "payKind";
	public static final String ACTION_PARAM_ORDER_ID = "orderId";
	public static final String ACTION_PARAM_PAYMENT_CARD = "CARD";
	public static final String ACTION_PARAM_SECURE_KEY = "secureKey";
	public static final String ACTION_PARAM_SECRET_KEY = "secretKey";
	public static final String ACTION_PARAM_CARD_TOKEN = "cardToken";
	public static final String ACTION_PARAM_CLIENT_IP = "client_ip";
	public static final String ACTION_PARAM_ORDERID_ABAILABLE = "orderIdAbailable";
	public static final String ACTION_PARAM_SECRETKEY_ORIGIN = "secretKeyOrg";
	public static final String ACTION_PARAM_SECRETKEY_NEW = "secretKeyNew";
	public static final String ACTION_PARAM_NONCE = "nonce";
	
	public static final String ACTION_COLUMN_SUCCESS = "0000";
	public static final String ACTION_COLUMN_CARD_ISSUER_CD = "r_issuer_cd";
	public static final String ACTION_COLUMN_CARD_ISSUER_NM = "r_issuer_nm";
	public static final String ACTION_COLUMN_CARD_BILLKEY = "r_card_no";
	public static final String ACTION_COLUMN_CARD_ORDER_NO= "r_order_no";
	public static final String ACTION_COLUMN_CARD_CNO = "r_cno";
	public static final String ACTION_COLUMN_RES_CD = "r_res_cd";
	public static final String ACTION_COLUMN_RES_MSG = "r_res_msg";
	
	
	
	
	public static final String ACTION_DELETE_FLAG = "D";
	
	
		
	
	
	
	public void switchAction(JsonObject request, JsonObject result);
	public void registCard(JsonObject request, JsonObject result);
	public void deleteCardOne(JsonObject request, JsonObject result);
	public void changePasswordSimplePay(JsonObject request, JsonObject result);
	public void requestPayment(JsonObject request, JsonObject result);
	public void cancelPayment(JsonObject request, JsonObject result);
	public void getCardList(JsonObject request, JsonObject result);
	public void changeCardNick(JsonObject request, JsonObject result);
	public void initializePassword(JsonObject request, JsonObject result);
	public String initializePasswordStep2(String userId, String kgId, String nonce);
	
}
