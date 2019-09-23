package com.moaplanet.pg.kicc;

import com.moaplanet.pg.PGController;
import com.moaplanet.util.property.ReadingProperty;

import java.util.Map;

import com.google.gson.Gson;
import com.kicc.*;
import com.moaplanet.pg.ActionHandler;

public class KiccTransaction {
	private static final Map<String, String> propertyMap = new ReadingProperty().getKiccInfoFromProperty("KiccConfig.properties");
	private static final String CERT_FILE = propertyMap.get("CERT_FILE");
	private static final String LOG_DIR = propertyMap.get("LOG_DIR");
	private static final String GW_URL = propertyMap.get("GW_URL");
	
	private enum RunningType { REGIST, PAYMENT}
	private RunningType ActionType = RunningType.PAYMENT; 

	public KiccTranResultVo easypayRunForRegist(KiccTranSendVo sendVo) {
		ActionType = RunningType.REGIST;
		return easypayRun(sendVo);
	}
	
    public KiccTranResultVo easypayRun(KiccTranSendVo sendVo) {
    
//    	System.out.println("cert file : " + CERT_FILE);
//    	System.out.println("log dir : " + LOG_DIR);
//    	System.out.println("gw url : " + GW_URL);
    	
    	KiccTranResultVo resultVo = new KiccTranResultVo();
    	
	    /* -------------------------------------------------------------------------- */
	    /* ::: 처리구분 설정                                                          */
	    /* -------------------------------------------------------------------------- */
	    final String TRAN_CD_NOR_PAYMENT  = "00101000";   // 승인(일반, 에스크로)
	    final String TRAN_CD_NOR_MGR      = "00201000";   // 변경(일반, 에스크로)
	
	    /* -------------------------------------------------------------------------- */
	    /* ::: 지불 정보 설정                                                         */
	    /* -------------------------------------------------------------------------- */
//	    final String GW_URL               = "testgw.easypay.co.kr";  // Gateway URL ( test )
	   //final String GW_URL              = "gw.easypay.co.kr";      // Gateway URL ( real )
	    final String GW_PORT              = "80";                    // 포트번호(변경불가)
	
	    /* -------------------------------------------------------------------------- */
	    /* ::: 지불 데이터 셋업 (업체에 맞게 수정)                                    */
	    /* -------------------------------------------------------------------------- */
	    /*     ※ 주의 ※                                                             */
	    /*     #cert_file 변수 설정                                                   */
	    /*       - pg_cert.pem 파일이 있는 디렉토리의 절대 경로 설정                  */
	    /*     #log_dir 변수 설정                                                     */
	    /*       - log 디렉토리 설정                                                  */
	    /*     #log_level 변수 설정                                                   */
	    /*       - log 레벨 설정                                                      */
	    /* -------------------------------------------------------------------------- */
//	    final String CERT_FILE            = "C:\\00.development\\eclipseWorkspace\\kiccBatch\\WebContent\\cert";
//	    final String LOG_DIR              = "C:\\00.development\\eclipseWorkspace\\kiccBatch\\WebContent\\log";
//	    final String CERT_FILE            = "C:\\Users\\moa_0\\eclipse-workspace_test\\MOAB2CBackEndMVC_20190305\\WebContent\\payment\\cert\\";
//	    final String LOG_DIR              = "C:\\Users\\moa_0\\eclipse-workspace_test\\MOAB2CBackEndMVC_20190305\\WebContent\\payment\\log\\";
//	    final String CERT_FILE            = "/application/apache-backend/webapps/ROOT/payment/cert";
//	    final String LOG_DIR              = "/application/apache-backend/webapps/ROOT/payment/log";
	    									 
	    final int LOG_LEVEL               = 1;
	
	    /* -------------------------------------------------------------------------- */
	    /* ::: 승인요청 정보 설정                                                     */
	    /* -------------------------------------------------------------------------- */
	    //[헤더]
	    String tr_cd             = sendVo.getTr_cd();           // [필수]결제창 요청구분
	    String trace_no          = sendVo.getTrace_no();        // [필수]추적번호
	    String order_no          = sendVo.getOrder_no();        // [필수]가맹점 주문번호
	    String mall_id           = sendVo.getMall_id();         // [필수]가맹점 ID
	    //[공통]
	    String encrypt_data      = sendVo.getEncrypt_data();    // [필수]암호화전문
	    String sessionkey        = sendVo.getSessionkey();      // [필수]세션키
	
	
	    /* --------------------------------------------------------------------------- */
	    /* ::: 결제요청 - 가맹점 주문정보                                              */
	    /* --------------------------------------------------------------------------- */
	    String memb_user_no      = sendVo.getMemb_user_no();   // [선택]가맹점 고객일련번호
	    String user_id           = sendVo.getUser_id();   // [선택]고객 ID
	    String user_nm           = sendVo.getUser_nm();   // [선택]고객명
	    String user_mail         = sendVo.getUser_mail();   // [선택]고객 E-mail
	    String user_phone1       = sendVo.getUser_phone1();   // [선택]가맹점 고객 연락처1
	    String user_phone2       = sendVo.getUser_phone2();   // [선택]가맹점 고객 연락처2
	    String user_addr         = sendVo.getUser_addr();   // [선택]가맹점 고객 주소
	    String product_type      = sendVo.getProduct_type();   // [선택]상품정보구분[0:실물,1:컨텐츠]
	    String product_nm        = sendVo.getProduct_nm();   // [선택]상품명
	    String product_amt       = sendVo.getProduct_amt();   // [필수]상품금액
//	    String user_define1      = sendVo.getUser_define1();   // [선택]가맹점필드1
//	    String user_define2      = sendVo.getUser_define2();   // [선택]가맹점필드2
//	    String user_define3      = sendVo.getUser_define3();   // [선택]가맹점필드3
//	    String user_define4      = sendVo.getUser_define4();   // [선택]가맹점필드4
//	    String user_define5      = sendVo.getUser_define5();   // [선택]가맹점필드5
//	    String user_define6      = sendVo.getUser_define6();   // [선택]가맹점필드6
	
	
	    /* -------------------------------------------------------------------------- */
	    /* ::: 배치결제 정보 설정                                                     */
	    /* -------------------------------------------------------------------------- */
	    String pay_type         = sendVo.getPay_type();     // [필수]결제수단
	    String tot_amt          = sendVo.getTot_amt();      // [필수]총결제금액
	    String currency         = sendVo.getCurrency();    	// [필수]통화코드
	    String client_ip        = sendVo.getClient_ip();      // [필수]고객 IP
//	    String cli_ver          = sendVo.getCli_ver();        // [필수]클라이언트 버젼(M8로 고정)
	    String complex_yn       = sendVo.getComplex_yn();     // [필수]복합결제유무(배치결제는 사용 안함)
	    String escrow_yn        = sendVo.getEscrow_yn();      // [필수]에스크로 여부(배치결제는 사용 안함)
	
	    String card_txtype      = sendVo.getCard_txtype();     // [필수]처리구분
	    String req_type         = sendVo.getReq_type();        // [필수]결제종류
	    String card_amt         = sendVo.getCard_amt();        // [필수]결제금액
	    String wcc              = sendVo.getWcc();             // [필수]WCC
	    String card_no          = sendVo.getCard_no();         // [필수]배치키
	    String install_period   = sendVo.getInstall_period();  // [필수]할부개월
	    String noint            = sendVo.getNoint();           // [필수]무이자여부
	
	
	    /* -------------------------------------------------------------------------- */
	    /* ::: 변경관리 정보 설정                                                     */
	    /* -------------------------------------------------------------------------- */
	    String mgr_txtype       = sendVo.getMgr_txtype();         // [필수]거래구분
	    String mgr_subtype      = sendVo.getMgr_subtype();        // [선택]변경세부구분
	    String org_cno          = sendVo.getOrg_cno();            // [필수]원거래고유번호
	    String mgr_amt          = sendVo.getMgr_amt();            // [선택]금액
	    String mgr_rem_amt      = sendVo.getMgr_rem_amt();        // [선택]부분취소 잔액
	
	    /* -------------------------------------------------------------------------- */
	    /* ::: 결제 결과                                                              */
	    /* -------------------------------------------------------------------------- */
	
	    
	    /* -------------------------------------------------------------------------- */
	    /* ::: EasyPayClient 인스턴스 생성 [변경불가 !!].                             */
	    /* -------------------------------------------------------------------------- */
	    EasyPayClient easyPayClient = new EasyPayClient();
	    easyPayClient.easypay_setenv_init( GW_URL, GW_PORT, CERT_FILE, LOG_DIR, LOG_LEVEL );
	    easyPayClient.easypay_reqdata_init();
	
	    /* -------------------------------------------------------------------------- */
	    /* ::: 승인요청                                                               */
	    /* -------------------------------------------------------------------------- */
	    if( TRAN_CD_NOR_PAYMENT.equals(tr_cd) ){
	
	        if( pay_type.equals("81") ) { // 배치인증
	
	            easyPayClient.easypay_set_trace_no(trace_no);
	            easyPayClient.easypay_encdata_set(encrypt_data, sessionkey);
	
	        }
	        else  //배치승인
	        {
	            /* ---------------------------------------------------------------------- */
	            /* ::: 승인요청                                                           */
	            /* ---------------------------------------------------------------------- */
	                // 결제 주문 정보 DATA
	                int easypay_order_data_item;
	                easypay_order_data_item = easyPayClient.easypay_item( "order_data" );
	
	                easyPayClient.easypay_deli_us( easypay_order_data_item, "order_no"          , order_no      );
	                easyPayClient.easypay_deli_us( easypay_order_data_item, "memb_user_no"      , memb_user_no  );
	                easyPayClient.easypay_deli_us( easypay_order_data_item, "user_id"           , user_id       );
	                easyPayClient.easypay_deli_us( easypay_order_data_item, "user_nm"           , user_nm       );
	                easyPayClient.easypay_deli_us( easypay_order_data_item, "user_mail"         , user_mail     );
	                easyPayClient.easypay_deli_us( easypay_order_data_item, "user_phone1"       , user_phone1   );
	                easyPayClient.easypay_deli_us( easypay_order_data_item, "user_phone2"       , user_phone2   );
	                easyPayClient.easypay_deli_us( easypay_order_data_item, "user_addr"         , user_addr     );
	                easyPayClient.easypay_deli_us( easypay_order_data_item, "product_type"      , product_type  );
	                easyPayClient.easypay_deli_us( easypay_order_data_item, "product_nm"        , product_nm    );
	                easyPayClient.easypay_deli_us( easypay_order_data_item, "product_amt"       , product_amt   );
	
	                // 결제정보 DATA
	                int easypay_pay_data_item;
	                easypay_pay_data_item = easyPayClient.easypay_item( "pay_data" );
	
	                // 결제정보 DATA - 공통
	                int easypay_common_item;
	                easypay_common_item = easyPayClient.easypay_item( "common" );
	
	                easyPayClient.easypay_deli_us( easypay_common_item, "tot_amt"   , tot_amt       );
	                easyPayClient.easypay_deli_us( easypay_common_item, "currency"  , currency      );
	                easyPayClient.easypay_deli_us( easypay_common_item, "client_ip" , client_ip ); // joon remote ip
	                easyPayClient.easypay_deli_us( easypay_common_item, "cli_ver"   , "M8"          );
	                easyPayClient.easypay_deli_us( easypay_common_item, "escrow_yn" , escrow_yn     );
	                easyPayClient.easypay_deli_us( easypay_common_item, "complex_yn", complex_yn    );
	                easyPayClient.easypay_deli_rs( easypay_pay_data_item , easypay_common_item );
	
	                // 결제정보 DATA - 신용카드
	                int easypay_card_item;
	                easypay_card_item = easyPayClient.easypay_item( "card" );
	
	                easyPayClient.easypay_deli_us( easypay_card_item, "card_txtype"    , card_txtype       );
	                easyPayClient.easypay_deli_us( easypay_card_item, "req_type"       , req_type          );
	                easyPayClient.easypay_deli_us( easypay_card_item, "card_amt"       , card_amt          );
	                easyPayClient.easypay_deli_us( easypay_card_item, "card_no"        , card_no           );
	                easyPayClient.easypay_deli_us( easypay_card_item, "noint"          , noint             );
	                easyPayClient.easypay_deli_us( easypay_card_item, "wcc"            , wcc               );
	                easyPayClient.easypay_deli_us( easypay_card_item, "install_period" , install_period    );
	
	                easyPayClient.easypay_deli_rs( easypay_pay_data_item , easypay_card_item );
	
	            }
	
	    /* -------------------------------------------------------------------------- */
	    /* ::: 변경관리 요청                                                          */
	    /* -------------------------------------------------------------------------- */
	    }else if( TRAN_CD_NOR_MGR.equals( tr_cd ) ) {

	        int easypay_mgr_data_item;
	        easypay_mgr_data_item = easyPayClient.easypay_item( "mgr_data" );
	
	        easyPayClient.easypay_deli_us( easypay_mgr_data_item, "mgr_txtype"    , mgr_txtype    );           // [필수]거래구분
	        easyPayClient.easypay_deli_us( easypay_mgr_data_item, "mgr_subtype"   , mgr_subtype   );           // [선택]변경세부구분
	        easyPayClient.easypay_deli_us( easypay_mgr_data_item, "org_cno"       , org_cno       );           // [필수]원거래고유번호
	        easyPayClient.easypay_deli_us( easypay_mgr_data_item, "mgr_amt"       , mgr_amt       );           // [선택]금액
	        easyPayClient.easypay_deli_us( easypay_mgr_data_item, "mgr_rem_amt"   , mgr_rem_amt   );           // [선택]부분취소 잔액
	        easyPayClient.easypay_deli_us( easypay_mgr_data_item, "req_ip"        , client_ip ); // joon [필수]요청자 IP
	    }
	    /* -------------------------------------------------------------------------- */
	    /* ::: 실행                                                                   */
	    /* -------------------------------------------------------------------------- */
	    if ( tr_cd.length() > 0 ) {
	        easyPayClient.easypay_run( mall_id, tr_cd, order_no );
	
	        resultVo.setR_res_cd(easyPayClient.res_cd);
	        resultVo.setR_res_msg(easyPayClient.res_msg);
	    }
	    else {
	    	resultVo.setR_res_cd ("M114");
	    	resultVo.setR_res_msg("연동 오류|tr_cd값이 설정되지 않았습니다.");
	    }
	
	    /* -------------------------------------------------------------------------- */
	    /* ::: 결과 처리                                                              */
	    /* -------------------------------------------------------------------------- */
	
	
	    resultVo.setR_cno           (easyPayClient.easypay_get_res( "cno"             ));     //PG거래번호
	    resultVo.setR_amount        (easyPayClient.easypay_get_res( "amount"          ));     //총 결제금액
	    resultVo.setR_order_no      (easyPayClient.easypay_get_res( "order_no"        ));     //주문번호
	    resultVo.setR_auth_no       (easyPayClient.easypay_get_res( "auth_no"         ));     //승인번호
	    resultVo.setR_tran_date     (easyPayClient.easypay_get_res( "tran_date"       ));     //승인일시
	    resultVo.setR_escrow_yn     (easyPayClient.easypay_get_res( "escrow_yn"       ));     //에스크로 사용유무
	    resultVo.setR_complex_yn    (easyPayClient.easypay_get_res( "complex_yn"      ));     //복합결제 유무
	    resultVo.setR_stat_cd       (easyPayClient.easypay_get_res( "stat_cd"         ));     //상태코드
	    resultVo.setR_stat_msg      (easyPayClient.easypay_get_res( "stat_msg"        ));     //상태메시지
	    resultVo.setR_pay_type      (easyPayClient.easypay_get_res( "pay_type"        ));     //결제수단
	    resultVo.setR_card_no       (easyPayClient.easypay_get_res( "card_no"         ));     //카드번호
	    resultVo.setR_issuer_cd     (easyPayClient.easypay_get_res( "issuer_cd"       ));     //발급사코드
	    resultVo.setR_issuer_nm     (easyPayClient.easypay_get_res( "issuer_nm"       ));     //발급사명
	    resultVo.setR_acquirer_cd   (easyPayClient.easypay_get_res( "acquirer_cd"     ));     //매입사코드
	    resultVo.setR_acquirer_nm   (easyPayClient.easypay_get_res( "acquirer_nm"     ));     //매입사명
	    resultVo.setR_install_period(easyPayClient.easypay_get_res( "install_period"  ));     //할부개월
	    resultVo.setR_noint         (easyPayClient.easypay_get_res( "noint"           ));     //무이자여부
	    resultVo.setR_part_cancel_yn(easyPayClient.easypay_get_res( "part_cancel_yn"  ));     //부분취소 가능여부
	    resultVo.setR_card_gubun    (easyPayClient.easypay_get_res( "card_gubun"      ));     //신용카드 종류
	    resultVo.setR_card_biz_gubun(easyPayClient.easypay_get_res( "card_biz_gubun"  ));     //신용카드 구분
	    resultVo.setR_bk_pay_yn     (easyPayClient.easypay_get_res( "bk_pay_yn"       ));     //장바구니 결제여부
	    resultVo.setR_canc_acq_date (easyPayClient.easypay_get_res( "canc_acq_date"   ));     //매입취소일시
	    resultVo.setR_canc_date     (easyPayClient.easypay_get_res( "canc_date"       ));     //취소일시
	    resultVo.setR_refund_date   (easyPayClient.easypay_get_res( "refund_date"     ));     //환불예정일시
	

	    System.out.println("##### KiccTransction res_cd : " +resultVo.getR_res_cd() );
	    System.out.println("##### KiccTransction res_msg : " +resultVo.getR_res_msg() );
	    
	    if ( resultVo.getR_res_cd().equals(ActionHandler.ACTION_COLUMN_SUCCESS) ) {
	    	
    		KiccInterface B2CKiccIF = PGController.getB2cCallBack();
    		
    		
	    	Gson gson = new Gson();
	    	
	    	// 승인요청 실패 시 취소 전문 수행
	    	
			if (ActionType == RunningType.PAYMENT && B2CKiccIF.payCallBack(gson.toJson(resultVo)) < 1)
			{
	            if( TRAN_CD_NOR_PAYMENT.equals(tr_cd) ) {
	            	
	            	B2CKiccIF.cancle(resultVo.getR_order_no(), client_ip);
	                resultVo.setR_canc_date ("cancle");   
	            }
	        }
	    }
	    
	    return resultVo;
    }

}
