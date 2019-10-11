package moa.blockchain.client.transaction;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import moa.blockchain.common.crypto.Ed25519;
import moa.blockchain.common.util.Pair;
import moa.blockchain.client.network.Curl;
import moa.blockchain.common.PublicBytesSave;

public class SendTransactionToServer {
	/**
	 * send a transaction as json to server
	 * get jsons' information from JSONArrayInput.java and JSONArrayOutput.java
	 * 
	 * @throws Exception
	 */
	public void sendToServer() throws Exception { 
		
		PublicBytesSave pbs = new PublicBytesSave();
		Pair src_keyPair = new Pair(pbs.readPrivateKey(), pbs.readPublicKey());

		JSONArray output_array = new JSONArray();
		JSONArray input_array = new JSONArray();
		
		//set output array
		JSONTransactionArrayOutput jao = new JSONTransactionArrayOutput();
		output_array = jao.getJsonArray();
		
		String totalVal = "0";
		totalVal = jao.getTotalValue().toString();
		
		//set input array
		JSONTransactionArrayInput jai = new JSONTransactionArrayInput();
		input_array = jai.utxoSelector(Hex.encodeHexString(src_keyPair.getB()), new BigDecimal(totalVal));
		
		//send message to server
		Curl curl = new Curl();
		//System.out.println(setMSG(src_keyPair, input_array, output_array).toString());
		curl.sendToServer(setMSG(src_keyPair, input_array, output_array).toString());
	}
	
		
	/**
	 * transaction message constructor.
	 * if send a proper transaction, server will execute the transaction.
	 * 
	 * @param src_keyPair ~ Pair : it's account of sender
	 * @param inputJSONs ~ JSONArray : sender's hash_UTXOes. it's publicKey must same as src_keyPair's publicKey.
	 * @param outputJSONs ~ JsonArray : receiver's publicKey, and value. you could send to multiple different accounts at the same time.
	 * @return ~ JSONObject : transaction message
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */

	public JSONObject setMSG(Pair src_keyPair, JSONArray inputJSONs, JSONArray outputJSONs) throws NoSuchAlgorithmException, IOException {
		
		Message msg = new Message(Hex.encodeHexString(src_keyPair.getB()), inputJSONs, outputJSONs);
		Transaction tx = new Transaction(src_keyPair, msg);
		
		return tx.getJson();
	}
	
}

class Transaction {
	private JSONObject txMessage;
	private byte[] signBytes;
	
	public Transaction(Pair src_keyPair, Message msg_obj) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		this.txMessage = msg_obj.getJson();
		
		this.signBytes = Ed25519.sign(src_keyPair, this.txMessage.toString().getBytes("utf-8"));
		
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject getJson() {
		JSONObject j = new JSONObject();
		j.put("message", this.txMessage);
		j.put("signature", Hex.encodeHexString(this.signBytes));
		
		return j;
	}
}

class Message {
	private String publicKey;
	private JSONArray inputs;
	private JSONArray outputs;
	
	public Message(String publicKey, JSONArray inputs, JSONArray outputs) {
		this.publicKey = publicKey;
		this.inputs = inputs;
		this.outputs = outputs;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject getJson() {
		JSONObject msg_obj = new JSONObject();
		
		msg_obj.put("publicKey", this.publicKey);
		msg_obj.put("inputs", this.inputs);
		msg_obj.put("outputs", this.outputs);
		
		return msg_obj;
	}
}

