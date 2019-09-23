package com.moaplanet.pg.memorial;

public class MoaMemoryVo {

	private String actionType		;
	private String actionStep		;
	private String memory1		;
	private String memory2		;
	private String memory3		;
	private String memory4		;
	private long   createDate;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("actionType"	+ actionType);
		sb.append("\nactionStep :"	+ actionStep);
		sb.append("\nmemory1 :"		+ memory1	);
		sb.append("\nmemory2 :"		+ memory2	);
		sb.append("\nmemory3 :"		+ memory3	);
		sb.append("\nmemory4 :"		+ memory4	);
		sb.append("\ncreateDate :"	+ createDate);
				
		return sb.toString();
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getActionStep() {
		return actionStep;
	}

	public void setActionStep(String actionStep) {
		this.actionStep = actionStep;
	}

	public String getMemory1() {
		return memory1;
	}

	public void setMemory1(String memory1) {
		this.memory1 = memory1;
	}

	public String getMemory2() {
		return memory2;
	}

	public void setMemory2(String memory2) {
		this.memory2 = memory2;
	}

	public String getMemory3() {
		return memory3;
	}

	public void setMemory3(String memory3) {
		this.memory3 = memory3;
	}

	public String getMemory4() {
		return memory4;
	}

	public void setMemory4(String memory4) {
		this.memory4 = memory4;
	}

	public long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}
}
