package com.moaplanet.pg.kicc;

import javax.servlet.http.HttpServletRequest;

public class HandleParameter {

	public static KiccTranSendVo getSendVo(HttpServletRequest request) {
		KiccTranSendVo sendVo = new KiccTranSendVo();
		
	    sendVo.setTr_cd         ((request.getParameter("sp_tr_cd"	)));           // [필수]결제창 요청구분
	    sendVo.setTrace_no      ((request.getParameter("sp_trace_no")));        // [필수]추적번호
	    sendVo.setOrder_no      ((request.getParameter("sp_order_no")));        // [필수]가맹점 주문번호
	    sendVo.setMall_id       ((request.getParameter("sp_mall_id"	)));         // [필수]가맹점 ID
	    sendVo.setEncrypt_data  ((request.getParameter("sp_encrypt_data")));    // [필수]암호화전문
	    sendVo.setSessionkey    ((request.getParameter("sp_sessionkey")));      // [필수]세션키
	    sendVo.setMemb_user_no  ((request.getParameter("sp_memb_user_no" )));   // [선택]가맹점 고객일련번호
	    sendVo.setUser_id       ((request.getParameter("sp_user_id"      )));   // [선택]고객 ID
	    sendVo.setUser_nm       ((request.getParameter("sp_user_nm"      )));   // [선택]고객명
	    sendVo.setUser_mail     ((request.getParameter("sp_user_mail"    )));   // [선택]고객 E-mail
	    sendVo.setUser_phone1   ((request.getParameter("sp_user_phone1"  )));   // [선택]가맹점 고객 연락처1
	    sendVo.setUser_phone2   ((request.getParameter("sp_user_phone2"  )));   // [선택]가맹점 고객 연락처2
	    sendVo.setUser_addr     ((request.getParameter("sp_user_addr"    )));   // [선택]가맹점 고객 주소
	    sendVo.setProduct_type  ((request.getParameter("sp_product_type" )));   // [선택]상품정보구분[0:실물,1:컨텐츠]
	    sendVo.setProduct_nm    ((request.getParameter("sp_product_nm"   )));   // [선택]상품명
	    sendVo.setProduct_amt   ((request.getParameter("sp_product_amt"  )));   // [필수]상품금액
	    sendVo.setUser_define1  ((request.getParameter("sp_user_define1" )));   // [선택]가맹점필드1
	    sendVo.setUser_define2  ((request.getParameter("sp_user_define2" )));   // [선택]가맹점필드2
	    sendVo.setUser_define3  ((request.getParameter("sp_user_define3" )));   // [선택]가맹점필드3
	    sendVo.setUser_define4  ((request.getParameter("sp_user_define4" )));   // [선택]가맹점필드4
	    sendVo.setUser_define5  ((request.getParameter("sp_user_define5" )));   // [선택]가맹점필드5
	    sendVo.setUser_define6  ((request.getParameter("sp_user_define6" )));   // [선택]가맹점필드6
	    sendVo.setPay_type      ((request.getParameter("sp_ret_pay_type")));    // [필수]결제수단
	    sendVo.setTot_amt       ((request.getParameter("sp_tot_amt")));         // [필수]총결제금액
	    sendVo.setCurrency      ((request.getParameter("sp_currency")));        // [필수]통화코드
	    sendVo.setClient_ip     ( request.getRemoteAddr());                     // [필수]고객 IP
	    sendVo.setCli_ver       ("M8");                                         // [필수]클라이언트 버젼(M8로 고정)
	    sendVo.setComplex_yn    ("N");                                          // [필수]복합결제유무(배치결제는 사용 안함)
	    sendVo.setEscrow_yn     ("N");                                          // [필수]에스크로 여부(배치결제는 사용 안함)
	    sendVo.setCard_txtype   ((request.getParameter("sp_card_txtype")));     // [필수]처리구분
	    sendVo.setReq_type      ((request.getParameter("sp_req_type")));        // [필수]결제종류
	    sendVo.setCard_amt      ((request.getParameter("sp_card_amt")));        // [필수]결제금액
	    sendVo.setWcc           ((request.getParameter("sp_wcc")));             // [필수]WCC
	    sendVo.setCard_no       ((request.getParameter("sp_card_no")));         // [필수]배치키
	    sendVo.setInstall_period((request.getParameter("sp_install_period")));  // [필수]할부개월
	    sendVo.setNoint         ((request.getParameter("sp_noint")));           // [필수]무이자여부
	    sendVo.setMgr_txtype    ((request.getParameter("mgr_txtype")));         // [필수]거래구분
	    sendVo.setMgr_subtype   ((request.getParameter("mgr_subtype")));        // [선택]변경세부구분
	    sendVo.setOrg_cno       ((request.getParameter("org_cno")));            // [필수]원거래고유번호
	    sendVo.setMgr_amt       ((request.getParameter("mgr_amt")));            // [선택]금액
	    sendVo.setMgr_rem_amt   ((request.getParameter("mgr_rem_amt")));        // [선택]부분취소 잔액

		return sendVo;
	}
}
