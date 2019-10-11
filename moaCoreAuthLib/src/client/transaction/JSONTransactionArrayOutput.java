package moa.blockchain.client.transaction;

import java.math.BigDecimal;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONTransactionArrayOutput {
	private JSONArray output_array;
	private BigDecimal totalVal;
	
	public JSONTransactionArrayOutput() {
		this.output_array = new JSONArray();
		this.totalVal = BigDecimal.ZERO;
		
		/*
		 * TODO
		 * outputs
		 */
		UtxoSingleOutput so = new UtxoSingleOutput("b1b112f4d0c409db2baca6285b996c45c8ef44b996ff3f72c43eac5d92eff99a", "100");
		
		addToArray(so);
		addToArray(so);
		addToArray(so);
		addToArray(so);
		addToArray(so);
		
		//TODOEND
	}
	
	public JSONArray getJsonArray(){
		
		return this.output_array;
	}
	
	public BigDecimal getTotalValue() {
		
		return this.totalVal;
	}
	
	@SuppressWarnings("unchecked")
	private void addToArray(UtxoSingleOutput utxoSingleOutput) {
		this.output_array.add(utxoSingleOutput.getJson());
		this.totalVal = this.totalVal.add(new BigDecimal(utxoSingleOutput.getIntValue()));
	}
}

class UtxoSingleOutput {
	private String publicKey;
	private String value;
	
	public UtxoSingleOutput(String dst_key, String val) {
		this.publicKey = dst_key;
		this.value = val;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject getJson() {
		JSONObject j = new JSONObject();
		j.put("publicKey", this.publicKey);
		j.put("value", this.value);
		return j;
	}
	
	public String getIntValue() {
		return this.value;
	}
}
