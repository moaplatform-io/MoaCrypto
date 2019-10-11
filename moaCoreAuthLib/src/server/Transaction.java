package moa.blockchain.server;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;

import org.apache.commons.codec.binary.Hex;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import moa.blockchain.common.crypto.Crypto;
import moa.blockchain.common.crypto.Ed25519;
import moa.blockchain.server.db.Query;
import moa.blockchain.server.db.Table;

public class Transaction {
	/**
	 * it is used when client request a transaction.
	 * 
	 * check if 'json_TX' is valid transaction.
	 * if it is, execute the transaction.
	 * 
	 * @param json_TX ~ JSONObject : received json file from client
	 * {
	 * "signature":"",
	 * ""message":{
	 * "outputs":[{"publicKey":"","value":""}],
	 * "inputs"[{"hash_UTXO":""}],
	 * "publicKey":""}
	 * }
	 * @return ~ String : return result to client
	 * "Valid Sign" or
	 * "Invalid Sign"
	 * 
	 * @throws UnsupportedEncodingException
	 * @throws Exception
	 */
	public String receiveFromClient(JSONObject json_TX) throws UnsupportedEncodingException, Exception { //recive from client, and verify check, and if true, load to db and retrieve ok sign to client.
		Table table = new Table();
		
		String ret = "Invalid TX";
		
		////check all conditions, if it is valid
		
		final JSONObject msgObj = (JSONObject) json_TX.get("message");

		final byte[] publicKey = Hex.decodeHex(msgObj.get("publicKey").toString());
		final JSONArray inputArray = (JSONArray) msgObj.get("inputs");
		final JSONArray outputArray = (JSONArray) msgObj.get("outputs");

		final String sign_obj = json_TX.get("signature").toString();
		String msg = msgObj.toString();

		TXValidationCheck validCheck = new TXValidationCheck(sign_obj, msg, publicKey, inputArray, outputArray, table);
		
		if (validCheck.isValid()) {
			String query;
			//save transaction
			String TX = Hex.encodeHexString(Crypto.getInstance().doubleDigest(msgObj.toString().getBytes()));
			query = Query.Transaction.insertInto(TX, msg);
			
			table.query(query);
			
			//change used UTXO's tail_TX to currently saved TX
			for (Object input : inputArray) {
				query = Query.UTXO.update(TX, ((JSONObject)input).get("hash_UTXO").toString());
				
				table.query(query);
			}
			
			//save new UTXO
		
			int i = 1;
			for (Object output : outputArray) {
				String val = ((JSONObject)output).get("value").toString();
				String dst_public = ((JSONObject)output).get("publicKey").toString();
				
				byte[] hash_UTXO = (i + val + dst_public + TX).getBytes(); // value.toString + publicKey + head_hash_TX;
				query = Query.UTXO.insertInto(Hex.encodeHexString(Crypto.getInstance().doubleDigest(hash_UTXO)), val, dst_public, TX, Integer.toString(i));
				
				table.query(query);
				i++;
			}
			
			//retrieve remained value to itself
			String remainVal = validCheck.remainVal();
			if (Integer.valueOf(remainVal) > 0) {
				String src_public = Hex.encodeHexString(publicKey);
				
				byte[] hash_UTXO = ("0" + remainVal + src_public + TX).getBytes(); // value.toString + publicKey + head_hash_TX;
				query = Query.UTXO.insertInto(Hex.encodeHexString(Crypto.getInstance().doubleDigest(hash_UTXO)), remainVal, src_public, TX, "0");
						
				table.query(query);
			}

			ret = "Valid TX";
		}
		return ret;
	}
}

class TXValidationCheck{

	private static final Logger logger = LoggerFactory.getLogger( TXValidationCheck.class);
	private String remainVal;
	private boolean isValid;
	
	public TXValidationCheck(String sign_obj, String msg, byte[] publicKey, JSONArray inputArray, JSONArray outputArray, Table table) {
		
		this.isValid = signCheck(sign_obj, msg, publicKey) & 
				utxoCheck(publicKey, inputArray, outputArray, table) &
				valueCheck(inputArray, outputArray, table);
	}
	
	public String remainVal() {
		return this.remainVal;
	}
	
	public boolean isValid() {
		return this.isValid;
	}
	
	private boolean signCheck(String sign_obj, String msg, byte[] publicKey) {
		//signature check
		boolean isTSign = false;
		try {
			isTSign = Ed25519.verify(
					Hex.decodeHex(sign_obj), 
					msg.getBytes("utf-8"), 
					publicKey
					);
		} catch (Exception e) {
			logger.error("verify in signCheck", e);
		}
		logger.info("is Valid Sign : " + isTSign);
		
		return isTSign;
	}

	private boolean utxoCheck(byte[] publicKey, JSONArray inputArray, JSONArray outputArray, Table table) {
		//utxo existence
		boolean isTExist = true;
		
		String query = "";
		int count = 0;
		for (Object input : inputArray) {
			String hasValue = "0";
		
			query = Query.UTXO.select_count_of_HASH(((JSONObject)input).get("hash_UTXO").toString(), Hex.encodeHexString(publicKey));
			hasValue = table.getFirst(query);
					//Table.getFirst("SELECT count(hash_UTXO) FROM utxo_tbl where tail_hash_TX = \"0\" and hash_UTXO = \"" + ((JSONObject)input).get("hash_UTXO").toString() + "\" and publicKey = \""+ Hex.encodeHexString(publicKey) +"\";");
			
			if (Integer.valueOf(hasValue) == 0) {
				isTExist = false;
			} else {
				count++;
			}
		}
		if(inputArray.toArray().length != count) isTExist = false;
		
		logger.info("is All UTXO itself : " + isTExist);

		return isTExist;
	}
	
	private boolean valueCheck(JSONArray inputArray, JSONArray outputArray, Table table) {
		//proper value 
		boolean isTValue = true;
		
		BigDecimal sum = BigDecimal.ZERO;
		BigDecimal value = BigDecimal.ZERO;
		String query = "";
		
		for (Object input : inputArray) {
			query = Query.UTXO.select_VALUE(((JSONObject)input).get("hash_UTXO").toString());
			String s = table.getFirst(query);
					//Table.getFirst("SELECT value FROM utxo_tbl where tail_hash_TX = \"0\" and hash_UTXO = \"" + ((JSONObject)input).get("hash_UTXO").toString() + "\";");

			sum = sum.add(new BigDecimal(s));
		}
		
		for (Object output : outputArray) {
			value = value.add(new BigDecimal(((JSONObject)output).get("value").toString()));
		}
		if (sum.compareTo(value) < 0) {
			isTValue = false;
		}
		this.remainVal = sum.subtract(value).toString();
		logger.info("total input : " + sum);
		logger.info("total output : " + value);
		logger.info("is Valid Value : " + isTValue);

		return isTValue;
	}
}
