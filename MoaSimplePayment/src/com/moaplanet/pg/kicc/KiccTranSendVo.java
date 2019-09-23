package com.moaplanet.pg.kicc;

public class KiccTranSendVo {
    /* -------------------------------------------------------------------------- */
    /* ::: 승인요청 정보 설정                                                     */
    /* -------------------------------------------------------------------------- */
	private String tr_cd          = "";   // [필수]결제창 요청구분
	private String trace_no       = "";   // [필수]추적번호
	private String order_no       = "";   // [필수]가맹점 주문번호
	private String mall_id        = "";   // [필수]가맹점 ID
	//[공통]                = "";
	private String encrypt_data   = "";   // [필수]암호화전문
	private String sessionkey     = "";   // [필수]세션키

	/* --------------------------------------------------------------------------- */
    /* ::: 결제요청 - 가맹점 주문정보                                              */
    /* --------------------------------------------------------------------------- */
	private String memb_user_no   = "";   // [선택]가맹점 고객일련번호
	private String user_id        = "";   // [선택]고객 ID
	private String user_nm        = "";   // [선택]고객명
	private String user_mail      = "";   // [선택]고객 E-mail
	private String user_phone1    = "";   // [선택]가맹점 고객 연락처1
	private String user_phone2    = "";   // [선택]가맹점 고객 연락처2
	private String user_addr      = "";   // [선택]가맹점 고객 주소
	private String product_type   = "";   // [선택]상품정보구분[0:실물,1:컨텐츠]
	private String product_nm     = "";   // [선택]상품명
	private String product_amt    = "";   // [필수]상품금액
	private String user_define1   = "";   // [선택]가맹점필드1
	private String user_define2   = "";   // [선택]가맹점필드2
	private String user_define3   = "";   // [선택]가맹점필드3
	private String user_define4   = "";   // [선택]가맹점필드4
	private String user_define5   = "";   // [선택]가맹점필드5
	private String user_define6   = "";   // [선택]가맹점필드6

	
	/* -------------------------------------------------------------------------- */
    /* ::: 배치결제 정보 설정                                                     */
    /* -------------------------------------------------------------------------- */
	private String pay_type       = "";  // [필수]결제수단
	private String tot_amt        = "";  // [필수]총결제금액
	private String currency       = "";  // [필수]통화코드
	private String client_ip      = "";  // [필수]고객 IP
	private String cli_ver        = "M8";  // [필수]클라이언트 버젼(M8로 고정)
	private String complex_yn     = "N";  // [필수]복합결제유무(배치결제는 사용 안함)
	private String escrow_yn      = "N";  // [필수]에스크로 여부(배치결제는 사용 안함)

	private String card_txtype    = "";  // [필수]처리구분
	private String req_type       = "";  // [필수]결제종류
	private String card_amt       = "";  // [필수]결제금액
	private String wcc            = "";  // [필수]WCC
	private String card_no        = "";  // [필수]배치키
	private String install_period = "";  // [필수]할부개월
	private String noint          = "";  // [필수]무이자여부
    
	/* -------------------------------------------------------------------------- */
    /* ::: 변경관리 정보 설정                                                     */
    /* -------------------------------------------------------------------------- */
	private String mgr_txtype     = "";  // [필수]거래구분
	private String mgr_subtype    = "";  // [선택]변경세부구분
	private String org_cno        = "";  // [필수]원거래고유번호
	private String mgr_amt        = "";  // [선택]금액
	private String mgr_rem_amt    = "";  // [선택]부분취소 잔액

	public String toString() {

		StringBuilder sb = new StringBuilder();

		sb.append("\ntr_cd          : " +tr_cd          );
		sb.append("\ntrace_no       : " +trace_no       );
		sb.append("\norder_no       : " +order_no       );
		sb.append("\nmall_id        : " +mall_id        );
		sb.append("\nencrypt_data   : " +encrypt_data   );
		sb.append("\nsessionkey     : " +sessionkey     );
		sb.append("\nmemb_user_no   : " +memb_user_no   );
		sb.append("\nuser_id        : " +user_id        );
		sb.append("\nuser_nm        : " +user_nm        );
		sb.append("\nuser_mail      : " +user_mail      );
		sb.append("\nuser_phone1    : " +user_phone1    );
		sb.append("\nuser_phone2    : " +user_phone2    );
		sb.append("\nuser_addr      : " +user_addr      );
		sb.append("\nproduct_type   : " +product_type   );
		sb.append("\nproduct_nm     : " +product_nm     );
		sb.append("\nproduct_amt    : " +product_amt    );
		sb.append("\nuser_define1   : " +user_define1   );
		sb.append("\nuser_define2   : " +user_define2   );
		sb.append("\nuser_define3   : " +user_define3   );
		sb.append("\nuser_define4   : " +user_define4   );
		sb.append("\nuser_define5   : " +user_define5   );
		sb.append("\nuser_define6   : " +user_define6   );
		sb.append("\npay_type       : " +pay_type       );
		sb.append("\ntot_amt        : " +tot_amt        );
		sb.append("\ncurrency       : " +currency       );
		sb.append("\nclient_ip      : " +client_ip      );
		sb.append("\ncli_ver        : " +cli_ver        );
		sb.append("\ncomplex_yn     : " +complex_yn     );
		sb.append("\nescrow_yn      : " +escrow_yn      );
		sb.append("\ncard_txtype    : " +card_txtype    );
		sb.append("\nreq_type       : " +req_type       );
		sb.append("\ncard_amt       : " +card_amt       );
		sb.append("\nwcc            : " +wcc            );
		sb.append("\ncard_no        : " +card_no        );
		sb.append("\ninstall_period : " +install_period );
		sb.append("\nnoint          : " +noint          );
		sb.append("\nmgr_txtype     : " +mgr_txtype     );
		sb.append("\nmgr_subtype    : " +mgr_subtype    );
		sb.append("\norg_cno        : " +org_cno        );
		sb.append("\nmgr_amt        : " +mgr_amt        );
		sb.append("\nmgr_rem_amt    : " +mgr_rem_amt    );
		
		return sb.toString();
	}
	
	public String getTr_cd() {
		return tr_cd;
	}
	public void setTr_cd(String tr_cd) {
		this.tr_cd = tr_cd;
	}
	public String getTrace_no() {
		return trace_no;
	}
	public void setTrace_no(String trace_no) {
		this.trace_no = trace_no;
	}
	public String getOrder_no() {
		return order_no;
	}
	public void setOrder_no(String order_no) {
		this.order_no = order_no;
	}
	public String getMall_id() {
		return mall_id;
	}
	public void setMall_id(String mall_id) {
		this.mall_id = mall_id;
	}
	public String getEncrypt_data() {
		return encrypt_data;
	}
	public void setEncrypt_data(String encrypt_data) {
		this.encrypt_data = encrypt_data;
	}
	public String getSessionkey() {
		return sessionkey;
	}
	public void setSessionkey(String sessionkey) {
		this.sessionkey = sessionkey;
	}
	public String getMemb_user_no() {
		return memb_user_no;
	}
	public void setMemb_user_no(String memb_user_no) {
		this.memb_user_no = memb_user_no;
	}
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public String getUser_nm() {
		return user_nm;
	}
	public void setUser_nm(String user_nm) {
		this.user_nm = user_nm;
	}
	public String getUser_mail() {
		return user_mail;
	}
	public void setUser_mail(String user_mail) {
		this.user_mail = user_mail;
	}
	public String getUser_phone1() {
		return user_phone1;
	}
	public void setUser_phone1(String user_phone1) {
		this.user_phone1 = user_phone1;
	}
	public String getUser_phone2() {
		return user_phone2;
	}
	public void setUser_phone2(String user_phone2) {
		this.user_phone2 = user_phone2;
	}
	public String getUser_addr() {
		return user_addr;
	}
	public void setUser_addr(String user_addr) {
		this.user_addr = user_addr;
	}
	public String getProduct_type() {
		return product_type;
	}
	public void setProduct_type(String product_type) {
		this.product_type = product_type;
	}
	public String getProduct_nm() {
		return product_nm;
	}
	public void setProduct_nm(String product_nm) {
		this.product_nm = product_nm;
	}
	public String getProduct_amt() {
		return product_amt;
	}
	public void setProduct_amt(String product_amt) {
		this.product_amt = product_amt;
	}
	public String getUser_define1() {
		return user_define1;
	}
	public void setUser_define1(String user_define1) {
		this.user_define1 = user_define1;
	}
	public String getUser_define2() {
		return user_define2;
	}
	public void setUser_define2(String user_define2) {
		this.user_define2 = user_define2;
	}
	public String getUser_define3() {
		return user_define3;
	}
	public void setUser_define3(String user_define3) {
		this.user_define3 = user_define3;
	}
	public String getUser_define4() {
		return user_define4;
	}
	public void setUser_define4(String user_define4) {
		this.user_define4 = user_define4;
	}
	public String getUser_define5() {
		return user_define5;
	}
	public void setUser_define5(String user_define5) {
		this.user_define5 = user_define5;
	}
	public String getUser_define6() {
		return user_define6;
	}
	public void setUser_define6(String user_define6) {
		this.user_define6 = user_define6;
	}
	public String getPay_type() {
		return pay_type;
	}
	public void setPay_type(String pay_type) {
		this.pay_type = pay_type;
	}
	public String getTot_amt() {
		return tot_amt;
	}
	public void setTot_amt(String tot_amt) {
		this.tot_amt = tot_amt;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getClient_ip() {
		return client_ip;
	}
	public void setClient_ip(String client_ip) {
		this.client_ip = client_ip;
	}
	public String getCli_ver() {
		return cli_ver;
	}
	public void setCli_ver(String cli_ver) {
		this.cli_ver = cli_ver;
	}
	public String getComplex_yn() {
		return complex_yn;
	}
	public void setComplex_yn(String complex_yn) {
		this.complex_yn = complex_yn;
	}
	public String getEscrow_yn() {
		return escrow_yn;
	}
	public void setEscrow_yn(String escrow_yn) {
		this.escrow_yn = escrow_yn;
	}
	public String getCard_txtype() {
		return card_txtype;
	}
	public void setCard_txtype(String card_txtype) {
		this.card_txtype = card_txtype;
	}
	public String getReq_type() {
		return req_type;
	}
	public void setReq_type(String req_type) {
		this.req_type = req_type;
	}
	public String getCard_amt() {
		return card_amt;
	}
	public void setCard_amt(String card_amt) {
		this.card_amt = card_amt;
	}
	public String getWcc() {
		return wcc;
	}
	public void setWcc(String wcc) {
		this.wcc = wcc;
	}
	public String getCard_no() {
		return card_no;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public String getInstall_period() {
		return install_period;
	}
	public void setInstall_period(String install_period) {
		this.install_period = install_period;
	}
	public String getNoint() {
		return noint;
	}
	public void setNoint(String noint) {
		this.noint = noint;
	}
	public String getMgr_txtype() {
		return mgr_txtype;
	}
	public void setMgr_txtype(String mgr_txtype) {
		this.mgr_txtype = mgr_txtype;
	}
	public String getMgr_subtype() {
		return mgr_subtype;
	}
	public void setMgr_subtype(String mgr_subtype) {
		this.mgr_subtype = mgr_subtype;
	}
	public String getOrg_cno() {
		return org_cno;
	}
	public void setOrg_cno(String org_cno) {
		this.org_cno = org_cno;
	}
	public String getMgr_amt() {
		return mgr_amt;
	}
	public void setMgr_amt(String mgr_amt) {
		this.mgr_amt = mgr_amt;
	}
	public String getMgr_rem_amt() {
		return mgr_rem_amt;
	}
	public void setMgr_rem_amt(String mgr_rem_amt) {
		this.mgr_rem_amt = mgr_rem_amt;
	}
}
