package com.moaplanet.pg.exception;

public class PaymentException extends Exception{

	private final int ERR_CODE;

	public PaymentException(String message, int errCode){ 
		super(message);
		ERR_CODE = errCode;
	}

	public PaymentException(String message){
		this(message, 400);
	}

	public int getErrCode() {
		return ERR_CODE;
	}
}
