package com.moaplanet.pg.kicc;

public class KiccTranResultVo {

	private String r_res_cd             = "";     //응답코드
	private String r_res_msg            = "";     //응답메시지
	private String r_cno                = "";     //PG거래번호
	private String r_amount             = "";     //총 결제금액
	private String r_order_no           = "";     //주문번호
	private String r_auth_no            = "";     //승인번호
	private String r_tran_date          = "";     //승인일시
	private String r_escrow_yn          = "";     //에스크로 사용유무
    private String r_complex_yn         = "";     //복합결제 유무
    private String r_stat_cd            = "";     //상태코드
    private String r_stat_msg           = "";     //상태메시지
    private String r_pay_type           = "";     //결제수단
    private String r_card_no            = "";     //카드번호
    private String r_issuer_cd          = "";     //발급사코드
    private String r_issuer_nm          = "";     //발급사명
    private String r_acquirer_cd        = "";     //매입사코드
    private String r_acquirer_nm        = "";     //매입사명
    private String r_install_period     = "";     //할부개월
    private String r_noint              = "";     //무이자여부
    private String r_part_cancel_yn     = "";     //부분취소 가능여부
    private String r_card_gubun         = "";     //신용카드 종류
    private String r_card_biz_gubun     = "";     //신용카드 구분
    private String r_bk_pay_yn          = "";     //장바구니 결제여부
    private String r_canc_acq_date      = "";     //매입취소일시
    private String r_canc_date          = "";     //취소일시
    private String r_refund_date        = "";     //환불예정일시
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();


    	sb.append("r_res_cd         : " + r_res_cd         );
    	sb.append("\nr_res_msg        : " + r_res_msg        );
    	sb.append("\nr_cno            : " + r_cno            );
    	sb.append("\nr_amount         : " + r_amount         );
    	sb.append("\nr_order_no       : " + r_order_no       );
    	sb.append("\nr_auth_no        : " + r_auth_no        );
    	sb.append("\nr_tran_date      : " + r_tran_date      );
    	sb.append("\nr_escrow_yn      : " + r_escrow_yn      );
    	sb.append("\nr_complex_yn     : " + r_complex_yn     );
    	sb.append("\nr_stat_cd        : " + r_stat_cd        );
    	sb.append("\nr_stat_msg       : " + r_stat_msg       );
    	sb.append("\nr_pay_type       : " + r_pay_type       );
    	sb.append("\nr_card_no        : " + r_card_no        );
    	sb.append("\nr_issuer_cd      : " + r_issuer_cd      );
    	sb.append("\nr_issuer_nm      : " + r_issuer_nm      );
    	sb.append("\nr_acquirer_cd    : " + r_acquirer_cd    );
    	sb.append("\nr_acquirer_nm    : " + r_acquirer_nm    );
    	sb.append("\nr_install_period : " + r_install_period );
    	sb.append("\nr_noint          : " + r_noint          );
    	sb.append("\nr_part_cancel_yn : " + r_part_cancel_yn );
    	sb.append("\nr_card_gubun     : " + r_card_gubun     );
    	sb.append("\nr_card_biz_gubun : " + r_card_biz_gubun );
    	sb.append("\nr_bk_pay_yn      : " + r_bk_pay_yn      );
    	sb.append("\nr_canc_acq_date  : " + r_canc_acq_date  );
    	sb.append("\nr_canc_date      : " + r_canc_date      );
    	sb.append("\nr_refund_date    : " + r_refund_date    );
    	
    	return sb.toString();
    }
    
	public String getR_res_cd() {
		return r_res_cd;
	}
	public void setR_res_cd(String r_res_cd) {
		this.r_res_cd = r_res_cd;
	}
	
	public String getR_res_msg() {
		return r_res_msg;
	}
	public void setR_res_msg(String r_res_msg) {
		this.r_res_msg = r_res_msg;
	}
	public String getR_cno() {
		return r_cno;
	}
	public void setR_cno(String r_cno) {
		this.r_cno = r_cno;
	}
	public String getR_amount() {
		return r_amount;
	}
	public void setR_amount(String r_amount) {
		this.r_amount = r_amount;
	}
	public String getR_order_no() {
		return r_order_no;
	}
	public void setR_order_no(String r_order_no) {
		this.r_order_no = r_order_no;
	}
	public String getR_auth_no() {
		return r_auth_no;
	}
	public void setR_auth_no(String r_auth_no) {
		this.r_auth_no = r_auth_no;
	}
	public String getR_tran_date() {
		return r_tran_date;
	}
	public void setR_tran_date(String r_tran_date) {
		this.r_tran_date = r_tran_date;
	}
	public String getR_escrow_yn() {
		return r_escrow_yn;
	}
	public void setR_escrow_yn(String r_escrow_yn) {
		this.r_escrow_yn = r_escrow_yn;
	}
	public String getR_complex_yn() {
		return r_complex_yn;
	}
	public void setR_complex_yn(String r_complex_yn) {
		this.r_complex_yn = r_complex_yn;
	}
	public String getR_stat_cd() {
		return r_stat_cd;
	}
	public void setR_stat_cd(String r_stat_cd) {
		this.r_stat_cd = r_stat_cd;
	}
	public String getR_stat_msg() {
		return r_stat_msg;
	}
	public void setR_stat_msg(String r_stat_msg) {
		this.r_stat_msg = r_stat_msg;
	}
	public String getR_pay_type() {
		return r_pay_type;
	}
	public void setR_pay_type(String r_pay_type) {
		this.r_pay_type = r_pay_type;
	}
	public String getR_card_no() {
		return r_card_no;
	}
	public void setR_card_no(String r_card_no) {
		this.r_card_no = r_card_no;
	}
	public String getR_issuer_cd() {
		return r_issuer_cd;
	}
	public void setR_issuer_cd(String r_issuer_cd) {
		this.r_issuer_cd = r_issuer_cd;
	}
	public String getR_issuer_nm() {
		return r_issuer_nm;
	}
	public void setR_issuer_nm(String r_issuer_nm) {
		this.r_issuer_nm = r_issuer_nm;
	}
	public String getR_acquirer_cd() {
		return r_acquirer_cd;
	}
	public void setR_acquirer_cd(String r_acquirer_cd) {
		this.r_acquirer_cd = r_acquirer_cd;
	}
	public String getR_acquirer_nm() {
		return r_acquirer_nm;
	}
	public void setR_acquirer_nm(String r_acquirer_nm) {
		this.r_acquirer_nm = r_acquirer_nm;
	}
	public String getR_install_period() {
		return r_install_period;
	}
	public void setR_install_period(String r_install_period) {
		this.r_install_period = r_install_period;
	}
	public String getR_noint() {
		return r_noint;
	}
	public void setR_noint(String r_noint) {
		this.r_noint = r_noint;
	}
	public String getR_part_cancel_yn() {
		return r_part_cancel_yn;
	}
	public void setR_part_cancel_yn(String r_part_cancel_yn) {
		this.r_part_cancel_yn = r_part_cancel_yn;
	}
	public String getR_card_gubun() {
		return r_card_gubun;
	}
	public void setR_card_gubun(String r_card_gubun) {
		this.r_card_gubun = r_card_gubun;
	}
	public String getR_card_biz_gubun() {
		return r_card_biz_gubun;
	}
	public void setR_card_biz_gubun(String r_card_biz_gubun) {
		this.r_card_biz_gubun = r_card_biz_gubun;
	}
	public String getR_bk_pay_yn() {
		return r_bk_pay_yn;
	}
	public void setR_bk_pay_yn(String r_bk_pay_yn) {
		this.r_bk_pay_yn = r_bk_pay_yn;
	}
	public String getR_canc_acq_date() {
		return r_canc_acq_date;
	}
	public void setR_canc_acq_date(String r_canc_acq_date) {
		this.r_canc_acq_date = r_canc_acq_date;
	}
	public String getR_canc_date() {
		return r_canc_date;
	}
	public void setR_canc_date(String r_canc_date) {
		this.r_canc_date = r_canc_date;
	}
	public String getR_refund_date() {
		return r_refund_date;
	}
	public void setR_refund_date(String r_refund_date) {
		this.r_refund_date = r_refund_date;
	}
    
    

}
