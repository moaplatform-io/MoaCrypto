package moa.blockchain.client.transaction;

import java.io.IOException;
import java.math.BigDecimal;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import moa.blockchain.client.network.Curl;

public class JSONTransactionArrayInput {
	public JSONArray inputJsonArray() {
		JSONArray input_array = new JSONArray();
		
		//TODO
		//replace to selector
		//UtxoSingleInput UTXO_obj = new UtxoSingleInput("7cc6b4daa23ee0555d860cc7b586f63d346e8f6645b2d5b4490709b67f0ce113"); 
		//input_array.add(UTXO_obj.getJson());
		//TODOEND
		
		return input_array;
	}	

	/**
	 * request to server, autoUTXOSelector is process this.
	 * server will return proper utxoes that publicKey has,
	 * if the publicKey has not enough balance, return empty array.
	 * 
	 * @param src_publicKey ~ String : public key that you want to search.
	 * @param value ~ BigDecimal : value how much you want to use
	 * @return ~ JSONArray : returned JSONArray from server
	 */
	@SuppressWarnings("unchecked")
	public JSONArray utxoSelector(String src_publicKey, BigDecimal value) { //input_array
		JSONObject json = new JSONObject();
		
		json.put("publicKey", src_publicKey);
		json.put("value", value);
		
		JSONObject jsonPackage = new JSONObject();
		jsonPackage.put("AutoUTXOSelector", json);
		////////
		String jsonString = "";
		try {
			Curl curl = new Curl();
			jsonString = curl.sendToServer(jsonPackage.toString()).replace("\'", "\"");
		} catch (IOException e) {
			e.printStackTrace();
		}
    	JSONParser parser = new JSONParser();
    	JSONArray jsonrt = new JSONArray();

    	try {
			jsonrt = (JSONArray) parser.parse(jsonString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return jsonrt;
	}

}

class UtxoSingleInput {
	private String hashUtxo;
	
	public UtxoSingleInput(String hashUtxo) {
		this.hashUtxo = hashUtxo;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject getJson() {
		JSONObject j = new JSONObject();
		j.put("hash_UTXO", this.hashUtxo);
		return j;
	}
}
