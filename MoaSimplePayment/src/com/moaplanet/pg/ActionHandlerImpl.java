package com.moaplanet.pg;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;


import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.moaplanet.card.dao.CardDao;
import com.moaplanet.card.dto.CardChgPwdHisVo;
import com.moaplanet.card.dto.CardVo;
import com.moaplanet.pg.exception.PaymentException;
import com.moaplanet.pg.kicc.KiccInterface;
import com.moaplanet.pg.kicc.KiccTranResultVo;
import com.moaplanet.pg.kicc.KiccTranSendVo;
import com.moaplanet.pg.kicc.KiccTransaction;
import com.moaplanet.pg.memorial.MoaMemorial;
import com.moaplanet.pg.memorial.MoaSimplePayMemoryFactory;


import com.moaplanet.pg.memorial.MoaMemoryVo;

public class ActionHandlerImpl implements ActionHandler{
	private static CardDao cardDao = null;

	public ActionHandlerImpl(CardDao dao) {
		if (cardDao == null)
			cardDao = dao;
	}
	
	
	@Override
	public void switchAction(JsonObject request, JsonObject result) {
		String strActType = request.get(ACTION_TYPE).getAsString();

		if (strActType != null)
		{
			switch (strActType) {
				case ACTION_TYPE_CARD_REGIST :
					registCard(request, result);
					break;
					
				case ACTION_TYPE_SELECT_CARD_LIST :
					getCardList(request, result);
					break;
					
				case ACTION_TYPE_REQUEST_PAY :
					requestPayment(request, result);
					break;
			
				
			/* wallet 비밀번호와 통합으로 TB_AUTH.MOAPAY_PWD 에 비밀번호 저장됨. 저장/변경은 UserAPI 에서 진행
				case ACTION_TYPE_CHANGE_PASSWORD :
					changePasswordSimplePay(request, result);
					break;
			*/
					
				case ACTION_TYPE_DELETE_CARD_ONE :
					deleteCardOne(request, result);
					break;
					
				case ACTION_TYPE_CHANGE_CARDNICK :
					changeCardNick(request, result);
					break;
					
				case ACTION_TYPE_INITIALIZE_PASSWORD : 
					initializePassword(request, result);
					break;
					
				default :
					System.out.println("sa default");
					setBadRequest(result);
			}
		}
		else {
			System.out.println("sa acttype else");
			setBadRequest(result);
		}
		
		System.out.println("switchAction result :" + result.toString());
	}
	
	@Override
	public void initializePassword(JsonObject request, JsonObject result) {
		try {
			String strActStep = request.get(ACTION_STEP).getAsString();
			switch (strActStep) {
			case "1" :
				String memberId = getMemberId(request);

				
				List<CardVo> listCardVo = cardDao.getUserCardList(memberId);
				if (listCardVo != null && listCardVo.size() > 0)
				{
					// TODO 공유 메모리 읽고 쓰기 리팩토링 필요 
					
					// 공유메모리 저장
					System.out.println(ACTION_TYPE_INITIALIZE_PASSWORD + " Step 1 memberId : "+ memberId);
					
					MoaMemorial authMem = MoaSimplePayMemoryFactory.getMemoryWorker();
					MoaMemoryVo memoryVo = new MoaMemoryVo();
					
					memoryVo.setActionType(ACTION_TYPE_INITIALIZE_PASSWORD);  
					memoryVo.setActionStep("1"); 
					
					// nonce 생성
					memoryVo.setMemory1(UUID.randomUUID().toString());
					memoryVo.setMemory2(request.get(ACTION_PARAM_SECRETKEY_NEW).getAsString());
					
					memoryVo.setCreateDate(System.currentTimeMillis());
					
					System.out.println("\nStep 1 memoryVo ###############");
					System.out.println(memoryVo.toString());
					
					authMem.put(memberId,  memoryVo);
					result.addProperty("nonce", memoryVo.getMemory1());
					setSuccessRequest(result);
				} else {
					throw new Exception("001");
				
				}
				
				break;
			case "ini" :
				System.out.println("###@in " + ACTION_TYPE_INITIALIZE_PASSWORD);
				System.out.println(result);
				String iniResult = initializePasswordStep2(getMemberId(request), "test_KGID", request.get(ACTION_PARAM_NONCE).getAsString());
				JsonParser jsonParser = new JsonParser();
		       	JsonObject requestJsonObj = (JsonObject) jsonParser.parse(iniResult);
				if (requestJsonObj.get(ACTION_RESULT).getAsString().equals("200"))
					setSuccessRequest(result);
				else
					setBadRequest(result);
				
				System.out.println("ini result = " + result.toString());
				System.out.println(result);
				
				break;
			default :
				setBadRequest(result, "W001");
			}
		} catch(Exception e) {
			setBadRequest(result, ACTION_TYPE_INITIALIZE_PASSWORD + " Bad Reqeust 025 : " + ((e.getMessage() == null)? "" : e.getMessage()));
		}
	
		
	}
	
	public String initializePasswordStep2(String userId, String kgId, String nonce) {

		MoaMemorial authMem = MoaSimplePayMemoryFactory.getMemoryWorker();
		MoaMemoryVo memVo = authMem.get(userId);
		
		JsonObject json = new JsonObject();
		try {
			json.addProperty(ACTION_TYPE, ACTION_TYPE_INITIALIZE_PASSWORD);
			json.addProperty(ACTION_STEP, "2");
	
			
			if (memVo.getActionType().equals(ACTION_TYPE_INITIALIZE_PASSWORD)) {
				if (memVo.getMemory1().equals(nonce)) {
					// TODO Save Initialization Password History

					CardVo cardVo = new CardVo();
					cardVo.setMEMBER_ID(userId);
					cardVo.setSECRET_KEY(memVo.getMemory2());
					changeUserCardPwd(cardVo);

					CardChgPwdHisVo cardHisVo = new CardChgPwdHisVo();
					cardHisVo.setMEMBER_ID(userId);
					cardHisVo.setCHANGE_CODE(ACTION_INITIAL_PASSWORD_CODE);
					cardHisVo.setCHANGE_REASON(kgId);
					addCardChgPwdHis(cardHisVo);
					
					setSuccessRequest(json);
					return json.toString();
				}
			}
			
			setBadRequest(json, ACTION_TYPE_INITIALIZE_PASSWORD + " Bad Request 026");
			return json.toString();
		} finally {
			System.out.println("#initializePasswordStep2 res = " + json.toString());
		}
	}


	@Override
	public void changeCardNick(JsonObject request, JsonObject result) {
		try {
			String strActStep = request.get(ACTION_STEP).getAsString();
			switch (strActStep) {
			case "1" :
				String memberId = getMemberId(request);
				
				String cardToken = request.get(ACTION_PARAM_CARD_TOKEN).getAsString();
				String cardNick = request.get(ACTION_PARAM_CARD_NICK).getAsString();
				
				CardVo cardVo = new CardVo();
				cardVo.setCARD_TOKEN(cardToken);
				 
				cardVo = getUserCardInfoOne(cardVo);
				// 
				System.out.println("memberId : " + memberId);
				System.out.println("selected memberId : " + cardVo.getMEMBER_ID());
				if (!memberId.equals(cardVo.getMEMBER_ID())) {
					throw new Exception("mismatch 001");
				}

				cardVo.setCARD_NICK(cardNick);
				if (updateUserCardInfoOne(cardVo) < 0) {
					throw new Exception("changeCardNick Bad Request : 001");
				}else {
					setSuccessRequest(result);
				}

				break;
			default :
				setBadRequest(result, "W001");
			}
		} catch(Exception e) {
			setBadRequest(result, "changeCardNick Bad Reqeust 025 : " + ((e.getMessage() == null)? "" : e.getMessage()));
		}

	}


	@Override
	public void registCard(JsonObject request, JsonObject result) {
		try {
			
		
			String strActStep = request.get(ACTION_STEP).getAsString();
			CardVo cardVo = new CardVo();
			switch (strActStep) {
				case "1" :
					// Insert User card 
					cardVo.setMEMBER_ID(getMemberId(request));
					cardVo.setCARD_NICK(getCardNick(request));
					cardVo.setSECRET_KEY(request.get(ACTION_PARAM_SECRET_KEY).getAsString());
					
					cardVo.setCARD_TOKEN(UUID.randomUUID().toString());
					cardVo.setBILL_KEY(cardVo.getCARD_TOKEN());
	
					result.addProperty(ACTION_PARAM_TOKEN, cardVo.getCARD_TOKEN());
					result.addProperty("returnURL", PGController.getB2cCallBack().getCardRegistReturnUrl());
					
					if (savaUserCardInfo(cardVo) < 0) {
						setBadRequest(result);
					}else {
						setSuccessRequest(result);
					};
					
					break;
					
				case "2" :
					// TODO joon
					// 카드등록 후 액션에 대하여 로직 추가 가능.
					//

					if ( !ACTION_COLUMN_SUCCESS.equals(request.get(ACTION_COLUMN_RES_CD).getAsString())){
						throw new Exception(request.get(ACTION_COLUMN_RES_MSG).getAsString());
					}
					
					cardVo.setCARD_TOKEN(request.get(ACTION_COLUMN_CARD_ORDER_NO).getAsString());
					System.out.println("registCard step 2 CardToken : " + cardVo.getCARD_TOKEN());

					// 
					cardVo = getUserCardInfoOne(cardVo);
					if (cardVo == null)
						throw new Exception("Invalid CardToken 001");
					
					System.out.println("in registCard cardVo : " + cardVo.toString());
					
					if (!cardVo.getUSE_FLAG().equals("N"))
						throw new Exception("Not avable 001");
					
//					System.out.println(request.toString());
					cardVo.setBILL_KEY(request.get(ACTION_COLUMN_CARD_BILLKEY).getAsString());
					cardVo.setISSUER_CD(request.get(ACTION_COLUMN_CARD_ISSUER_CD).getAsString());
					cardVo.setISSUER_NAME(request.get(ACTION_COLUMN_CARD_ISSUER_NM).getAsString());
					cardVo.setCNO(request.get(ACTION_COLUMN_CARD_CNO).getAsString());
					cardVo.setUSE_FLAG("Y");
					
					System.out.println("## Update CardInfo " + cardVo.toString());
					
					if (updateUserCardInfoOne(cardVo) < 0) {
						throw new Exception("CardRegist Bad Request : 003");
					}else {
						setSuccessRequest(result);
					}
	
					break;
					
				default :
					throw new Exception("CardRegist Bad Request : 001");
			} 
		}catch(Exception e){
			e.printStackTrace();
			setBadRequest(result, (e.getMessage() == null) ? "CardRegist Bad Request : 002" : e.getMessage());
		}
	}

	@Override
	public void getCardList(JsonObject request, JsonObject result) {
		try {
			String strActStep = request.get(ACTION_STEP).getAsString();
			switch (strActStep) {
				case "1" :
					String memberId = getMemberId(request);
					List<CardVo> listCardVo = cardDao.getUserCardList(memberId);
					
					result.addProperty(ACTION_PARAM_MEMBERID, memberId);
					
					Gson gson = new Gson();
					
					System.out.println("getCardList : listCardVo.toString");
					System.out.println(gson.toJson(listCardVo));
					result.addProperty(ACTION_PARAM_CARD_LIST, gson.toJson(listCardVo));
					
					setSuccessRequest(result);
					break;
					
				default :
					setBadRequest(result, "CardList Bad Reqeust 011");
			}
		}catch(Exception e){
			setBadRequest(result, "CardList Bad Reqeust 012");
		}
	}
	
	@Override
	public void requestPayment(JsonObject request, JsonObject result) {
		try {
			String strActStep = request.get(ACTION_STEP).getAsString();
			String memberId = getMemberId(request);
			switch (strActStep) {
				case "1" :
					if (!ACTION_PARAM_PAYMENT_CARD.equals(request.get(ACTION_PARAM_PAY_KIND).getAsString()))
					{
						throw new Exception("(Bad Payment kind)");
					}
					
					// 공유메모리 저장
					String orderId = request.get(ACTION_PARAM_ORDER_ID).getAsString();
					
					System.out.println("requestPayment Step 1 memberId : "+ memberId);
					
					MoaMemorial authMem = MoaSimplePayMemoryFactory.getMemoryWorker();
					MoaMemoryVo memoryVo = new MoaMemoryVo();
					
					memoryVo.setActionType("requestPayment");  
					memoryVo.setActionStep("1"); 
					
					// nonce 생성
					memoryVo.setMemory1(UUID.randomUUID().toString());
					memoryVo.setMemory2(orderId);
					memoryVo.setCreateDate(System.currentTimeMillis());
					
					// Save Vo to memory
					System.out.println("\nStep 1 memoryVo ###############");
					System.out.println(memoryVo.toString());
					
					authMem.put(memberId,  memoryVo);
					MoaMemoryVo testVo = authMem.get(memberId);
					System.out.println("\n@@@@@@ requestPayment Setp1 TestVo");
					System.out.println(testVo.toString());
					result.addProperty("nonce", memoryVo.getMemory1());
					
					setSuccessRequest(result);
						
					break;
					
				case "2" :
					System.out.println("request : " + request.toString());
					
					String billKey = verifySecureKey(request);

					KiccTranSendVo sendVo = getKiccTranSendVo(request, billKey);

					sendRequestToKICC(sendVo);

					
					setSuccessRequest(result);
					break;
					
				default :
					setBadRequest(result);
			}
		
		}catch(PaymentException pe) {
			System.out.println("PaymentException raised : " + pe.getErrCode() + pe.getMessage());
			setBadRequest(result, pe.getErrCode(), pe.getMessage()); 
			
				
		}catch(Exception e){
			e.printStackTrace();
			setBadRequest(result, "Payment Error Code E022 " + ((e.getMessage() == null)? "" : ": " + e.getMessage()));
		}
	}
	
	private KiccTranSendVo getKiccTranSendVo (JsonObject request, String billKey) throws Exception {
		KiccInterface kiccIF = PGController.getB2cCallBack();
		Map<String, String> userPayInfo = kiccIF.getUserPayInfo(request.get(ACTION_PARAM_ORDER_ID).getAsString());
		
		if (userPayInfo == null)
			throw new Exception("Bad ID : " + request.get(ACTION_PARAM_ORDER_ID).getAsString());
		else if (ACTION_YES.equals(userPayInfo.get(ACTION_PARAM_ORDERID_ABAILABLE)))
			throw new Exception("Can not cancel the order ID : " + request.get(ACTION_PARAM_ORDER_ID).getAsString());
		
		System.out.println("\nuserPayInfo ###");
		System.out.println(userPayInfo.toString());
		
		KiccTranSendVo sendVo = getKiccSendVoSetted(userPayInfo);
		sendVo.setCard_no(billKey);
		sendVo.setClient_ip(request.get(ACTION_PARAM_CLIENT_IP).getAsString());
		
		System.out.println("sendVo : " + sendVo.toString());
		
		return sendVo;

	}
	
	private void sendRequestToKICC(KiccTranSendVo sendVo) throws Exception {
		KiccTranResultVo kiccResultVo = kiccSendRequest(sendVo);
		
		System.out.println("kiccResultVo : " + kiccResultVo.toString());
		
		if (ACTION_COLUMN_SUCCESS.equals(kiccResultVo.getR_res_cd()))
		{
			if (!"".equals(kiccResultVo.getR_canc_date())) 
			{
				throw new Exception("Cancel Payment after B2C Server Error");
			}
		}else {
			throw new Exception("[KICC]" + kiccResultVo.getR_res_cd() + " : " + kiccResultVo.getR_res_msg());
		}
	}
	
	private KiccTranSendVo getKiccSendVoSetted(Map<String, String> userPayInfo) {
		KiccTranSendVo sendVo = new KiccTranSendVo();
		sendVo.setCard_txtype("41");
		sendVo.setReq_type("0");
		sendVo.setWcc("@");
		sendVo.setNoint("00");
		sendVo.setInstall_period("00");
		sendVo.setTr_cd("00101000");
		
		sendVo.setMall_id			(userPayInfo.get("mall_id"));  
		sendVo.setOrder_no			(userPayInfo.get("order_no"));
		sendVo.setCurrency			(userPayInfo.get("currency"));
		sendVo.setProduct_nm		(userPayInfo.get("product_nm"));
		sendVo.setProduct_amt		(userPayInfo.get("product_amt"));
		sendVo.setTot_amt			(sendVo.getProduct_amt());
		sendVo.setCard_amt			(sendVo.getProduct_amt());
		
//		sendVo.setLang_flag			(userPayInfo.get("lang_flag"));  
//		sendVo.setCharset			(userPayInfo.get("charset"));  
		sendVo.setUser_id			(userPayInfo.get("user_id"));  
		sendVo.setMemb_user_no		(userPayInfo.get("memb_user_no"));
		sendVo.setUser_nm			(userPayInfo.get("user_nm"));  
		sendVo.setUser_mail			(userPayInfo.get("user_mail"));  
		sendVo.setUser_phone1		(userPayInfo.get("user_phone1"));  
		sendVo.setUser_phone2		(userPayInfo.get("user_phone2"));  
		sendVo.setUser_addr			(userPayInfo.get("user_addr"));  
		sendVo.setProduct_type		(userPayInfo.get("product_type"));
//		sendVo.setProduct_expr		(userPayInfo.get("product_expr"));
//		sendVo.setApp_scheme		(userPayInfo.get("app_scheme"));
		return sendVo;
	}
	
	private String verifySecureKey(JsonObject request) throws Exception{
		MoaMemorial authMem = MoaSimplePayMemoryFactory.getMemoryWorker();
		MoaMemoryVo memoryVo = new MoaMemoryVo();
		
		String memberId = getMemberId(request);
		
		memoryVo = authMem.get(memberId);
		authMem.remove(memberId);
		
		// 검증용 정보 조합 nonce + memberId + OrderId
		String combinedStr = memoryVo.getMemory1() + memberId + memoryVo.getMemory2();
		System.out.println("\n combine Str getMemory1 : " + memoryVo.getMemory1());
		System.out.println("\n combine Str memberId : " + memberId);
		System.out.println("\n combine Str getMemory2 : " + memoryVo.getMemory2());

		// 카드정보조회 by cardToken
		CardVo cardVo = new CardVo();
		cardVo.setCARD_TOKEN(request.get(ACTION_PARAM_CARD_TOKEN).getAsString());
		cardVo = getUserCardInfoOne(cardVo);
		
		String secretKey = request.get(ACTION_PARAM_SECRET_KEY).getAsString();
		
		if (secretKey == null) {
			throw new PaymentException("Secret key is necessary");
			
		}else if (secretKey.length()< 5) {
			throw new PaymentException("The secret key is not valid");
		}
		else if (secretKey.equals(cardVo.getSECRET_KEY()) == false) {
			System.out.println("memberId received : " + memberId);
			System.out.println("secretKey received : " + secretKey);
			System.out.println("cardVo.getSECRET_KEY : " + cardVo.getSECRET_KEY());
			throw new PaymentException("The secret key is not matched", 401);
		}
		
		
		System.out.println("\n combine cardVo.getSECRET_KEY : " + cardVo.getSECRET_KEY());
	
		String encodedString = getMessageDigestForSecureKey(combinedStr, cardVo.getSECRET_KEY());
		
		String secureKey = request.get(ACTION_PARAM_SECURE_KEY).getAsString();
		System.out.println("received secureKey : " + secureKey);
		System.out.println("server secureKey : " + encodedString);
		if (encodedString.equals(secureKey)){
			System.out.println("verifySecureKey Success!!! : " + encodedString);
			return cardVo.getBILL_KEY();
		}
		else {
			throw new PaymentException("verify (002)");
		}
		
	}
	
	private KiccTranResultVo kiccSendRequest(KiccTranSendVo sendVo) {
		//kicc 전문통신
		KiccTransaction kiccTran = new KiccTransaction();
		return kiccTran.easypayRun(sendVo);
	}
	

	private String getMessageDigestForSecureKey(String targetData, String secretKey)
	{
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
			
			// todojoon for test, remove later;
			System.out.println("\n\n ### getMessageDigestForSecureKey : secretKey!@#");
			System.out.println(secretKey);
			Decoder decoder = Base64.getDecoder();
			byte[] decodeKey = decoder.decode(secretKey.getBytes());
			
			
			byte[] mergeByte = new byte[targetData.length() + decodeKey.length];
			System.arraycopy(decodeKey,  0,  mergeByte,  0,  decodeKey.length);
			System.arraycopy(targetData.getBytes(),   0,  mergeByte,  decodeKey.length, targetData.length());
			
			System.out.println("\n ### getMessageDigestForSecureKey : mergeByte");
			System.out.println(bytesToHex(mergeByte));
			byte[] digestData = md.digest(mergeByte);
			System.out.println("\n ### getMessageDigestForSecureKey : digestData");
			System.out.println(bytesToHex(digestData));
			
			
			Encoder encoder = Base64.getEncoder();
			return new String(encoder.encode(digestData));
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			byte[] returnByte = {0x00};
			return new String(returnByte);
		}
	}
	
	
	@Override
	// 간편결제 등록된 카드 삭제
	public void deleteCardOne(JsonObject request, JsonObject result) {
		try {
			String strActStep = request.get(ACTION_STEP).getAsString();
			switch (strActStep) {
				case "1" :
					String memberId = getMemberId(request);
					
					CardVo cardVo = new CardVo();
					cardVo.setCARD_TOKEN(request.get(ACTION_PARAM_CARD_TOKEN).getAsString());
					System.out.println("deleteCardOne step 1 CardToken : " + cardVo.getCARD_TOKEN());

					// 
					cardVo = getUserCardInfoOne(cardVo);
					if (cardVo == null)
						throw new Exception("Invalid CardToken 002");
					
					System.out.println("in registCard cardVo : " + cardVo.toString());
					
					if (!memberId.contentEquals(cardVo.getMEMBER_ID()))
						throw new Exception("Not abable E001");
					if (ACTION_DELETE_FLAG.equals(cardVo.getUSE_FLAG()))
						throw new Exception("Not abable E002");
						
					cardVo.setUSE_FLAG(ACTION_DELETE_FLAG);
					if (updateUserCardInfoOne(cardVo) < 0) 
						throw new Exception("fail to delete E003");
					
					setSuccessRequest(result);
					break;
				default :
					throw new Exception("W001");
			}
		} catch(Exception e) {
			setBadRequest(result, "Payment Bad Reqeust 023 : " + ((e.getMessage() == null)? "" : e.getMessage()));
		}
	}
	


	@Override
	public void changePasswordSimplePay(JsonObject request, JsonObject result) {
		
		try {
			String strActStep = request.get(ACTION_STEP).getAsString();
			switch (strActStep) {
			case "1" :
				String memberId = getMemberId(request);
				
				String secOrgin = request.get(ACTION_PARAM_SECRETKEY_ORIGIN).getAsString();
				String secNew = request.get(ACTION_PARAM_SECRETKEY_NEW).getAsString();
				
				CardVo cardVo = new CardVo();
				cardVo.setMEMBER_ID(memberId);
				cardVo.setSECRET_KEY(secOrgin);
				System.out.println("changePasswordSimplePay step 1 secOrgin : " + secOrgin);

				// where memberId and secretKey match
				int matchCount = confirmkUserCardPwd(cardVo);
				if (matchCount < 1) {
					// TODO mismatch 5회인 경우도 조회하여 간편결제 lock처리 필요
					
					throw new Exception("mismatch password 002");
				}
				
				cardVo.setMEMBER_ID(memberId);
				cardVo.setSECRET_KEY(secNew);
				changeUserCardPwd(cardVo);
				
				CardChgPwdHisVo cardHisVo = new CardChgPwdHisVo();
				cardHisVo.setMEMBER_ID(memberId);
				cardHisVo.setCHANGE_CODE(ACTION_CHANGE_PASSWORD_CODE);
				cardHisVo.setCHANGE_REASON(ACTION_CHANGE_PASSWORD_REASON);
				addCardChgPwdHis(cardHisVo);

				setSuccessRequest(result);
				break;
				
			default :
				setBadRequest(result, "W001");
			}
		} catch(Exception e) {
			e.printStackTrace();
			setBadRequest(result, "Payment Bad Reqeust 024 : " + ((e.getMessage() == null)? "" : e.getMessage()));
		}
		
	}
	
	@Override
	// B2C에서 카드취소 구현 및 사용 중
	public void cancelPayment(JsonObject request, JsonObject result) {
		String strActStep = request.get(ACTION_STEP).getAsString();
		switch (strActStep) {
			case "1" :
				break;
			case "2" :
				break;
			default :
				setBadRequest(result);
		} 
	}



	private void setSuccessRequest(JsonObject jsonObj) {
		jsonObj.addProperty(ActionHandler.ACTION_RESULT, ACTION_SUCCESS);
		jsonObj.addProperty(ActionHandler.ACTION_RESULT_MSG, ACTION_SUCCESS_MSG);
		
	}
	
	private void setBadRequest(JsonObject jsonObj){
		setBadRequest(jsonObj, "400", "Bad Request");
	}
	
	private void setBadRequest(JsonObject jsonObj, String message){
		if (message == null)
			message = "bad request";
		
		setBadRequest(jsonObj, "400", message);
	}
	
	private void setBadRequest(JsonObject jsonObj, String ErrorCode, String message){
		jsonObj.addProperty(ActionHandler.ACTION_RESULT, ErrorCode);
		jsonObj.addProperty(ActionHandler.ACTION_RESULT_MSG, message);
	}
	
	private void setBadRequest(JsonObject jsonObj, int ErrorCode, String message){
		setBadRequest(jsonObj, Integer.toString(ErrorCode), message);
	}

	
	private int savaUserCardInfo(CardVo cardVo) {
		
		int iRet = 0;
		
		try {
			
			iRet = cardDao.addUserCard(cardVo);
	    	
		}catch(Exception e) {
			e.printStackTrace();
			iRet = -1;
		}
		
		return iRet;
	}
	
	private CardVo getUserCardInfoOne(CardVo cardVo) {
		CardVo retVo = null;
		try {
			retVo = cardDao.getUserCardOne(cardVo);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return retVo;
	}
	
	private int confirmkUserCardPwd(CardVo cardVo) {
		
		try {
			return cardDao.confirmkUserCardPwd(cardVo);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	
	private int updateUserCardInfoOne(CardVo cardVo) {
		int ret = -1;
		
		try {
			ret = cardDao.updateUserCard(cardVo);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	
	private int changeUserCardPwd(CardVo cardVo) {
		int ret = -1;
		
		try {
			ret = cardDao.changePwdSimplePay(cardVo);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return ret;
	}

	private int addCardChgPwdHis(CardChgPwdHisVo vo) {
		int ret = -1;
		
		try {
			ret = cardDao.addCardChgPwdHis(vo);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return ret;
	}

    public static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b: bytes) {
          builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }


	
	private String getMemberId(JsonObject request) {
		return request.get(ACTION_PARAM_MEMBERID).getAsString();
	}
	
	private String getCardNick(JsonObject request) {
		return request.get(ACTION_PARAM_CARD_NICK).getAsString();
	}
}
