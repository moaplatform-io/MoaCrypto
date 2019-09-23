package com.moaplanet.pg;

public class ResultInfo {
	private int resultCode;
	private String resultMessage;
	
	public int getResult() {
		return resultCode;
	}
	public void setResult(int result) {
		this.resultCode = result;
	}
	public String getMessage() {
		return resultMessage;
	}
	public void setMessage(String message) {
		this.resultMessage = message;
	}
	
}
