package org.moa.core.auth.client.api;

import java.security.*;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.encoders.Base64;

import org.moa.crypto.coreapi.DigestCoreAPI;
import org.moa.crypto.coreapi.SymmetricCoreAPI;

public class MoaClientCoreAuthLib {
	
	// Define about MOA Manager or Franchise ID Parsing for ID & Password : ID/Psw Regist 
	protected static final String  moaCORE_IDExist_STR				= "4D4F410001";  // Msg Manager ID : MOA0001 ***
	protected static final String  moaCORE_IDExistAck_STR			= "4D4F410002";  // Msg Manager ID : MOA0002
	protected static final String  moaCORE_IdPswRegReq_STR			= "4D4F410003";  // Msg Manager ID : MOA0003 ***
	protected static final String  moaCORE_IdPswRegRes_STR			= "4D4F410004";  // Msg Manager ID : MOA0004
	
	protected static final String  moaMANAGER_IdPswRegReq_STR		= "4D4F410013";  // Msg Manager ID : MOA0013 ***
	protected static final String  moaMANAGER_IdPswRegRes_STR		= "4D4F410014";  // Msg Manager ID : MOA0014
	
	// Define about MOA Manager or Franchise ID Parsing for ID & Password : ID/Psw LogIn
	protected static final String  moaCORE_IdPswLogInStartReq_STR	= "4D4F412001";  // Msg Manager ID : MOA2001
	protected static final String  moaCORE_IdPswLogInSTartRes_STR	= "4D4F412002";  // Msg Manager ID : MOA2002
	protected static final String  moaCORE_IdPswLogInReq_STR		= "4D4F412003";  // Msg Manager ID : MOA2003
	protected static final String  moaCORE_IdPswLogInRes_STR		= "4D4F412004";  // Msg Manager ID : MOA2004

	protected static final String  moaMANAGER_IdPswLogInReq_STR		= "4D4F412013";  // Msg Manager ID : MOA2013
	protected static final String  moaMANAGER_IdPswLogInRes_STR		= "4D4F412014";  // Msg Manager ID : MOA2014

	
	protected static final String  moaCORE_ChangePswReq_STR			= "4D4F413001";  // Msg Manager ID : MOA3001
	protected static final String  moaCORE_ChangePswRes_STR			= "4D4F413002";  // Msg Manager ID : MOA3002
	protected static final String  moaCORE_UnRegistReq_STR			= "4D4F413007";  // Msg Manager ID : MOA3007
	protected static final String  moaCORE_UnRegistRes_STR			= "4D4F413008";  // Msg Manager ID : MOA3008
	
	// Moa Crypto Core API Call
	static DigestCoreAPI dg = new DigestCoreAPI();
	static SymmetricCoreAPI sc = null;
	
	static Charset charset = Charset.forName("UTF-8");
	static String strToken = "$";

	// Declaration of Moa Crypto Alg Policy 
	static int 		versionInfo = 0;
	static String 	symmetricAlg = null;
	static int 		symmetricKeySize = 256;
	static String 	hashAlg = null;
	static String 	strongHashAlg = null;
	static String 	hmacAlg = null;
	static String 	strongHmacAlg = null;
	static String 	asymmetricAlg = null;
	static int 		asymmetricKeySize = 2048;

	
	public MoaClientCoreAuthLib(String moaB2CManagerPropertiesFileName) //throws FileNotFoundException, IOException 
    {

		File algPolicyFile = new File(moaB2CManagerPropertiesFileName);

		try {

			if ( !algPolicyFile.exists() ) {
				System.out.println(moaB2CManagerPropertiesFileName + " file Not found");
				return;
			}
		
			Path algPolicyFilePath = Paths.get(algPolicyFile.getAbsolutePath());
		
			// Properties Initialization of MoaWalletAPI Constructor 
			Properties propAlg = new Properties();
			propAlg.load(new FileInputStream(algPolicyFilePath.toString()));
			versionInfo = Integer.parseInt(propAlg.getProperty("Version.Info"));
			symmetricAlg = propAlg.getProperty("Symmetric.Alg");
			symmetricKeySize = Integer.parseInt(propAlg.getProperty("Symmetric.KeySize"));

			hashAlg = propAlg.getProperty("Hash.Alg");
			strongHashAlg = propAlg.getProperty("Hash.Strong.Alg");
		
			hmacAlg = propAlg.getProperty("Hmac.Alg");
			strongHmacAlg = propAlg.getProperty("Hmac.Strong.Alg");
		
			asymmetricAlg = propAlg.getProperty("Asymmetric.Alg");
			asymmetricKeySize = Integer.parseInt(propAlg.getProperty("Asymmetric.KeySize"));
		
    	} catch (FileNotFoundException fn) {
    		fn.printStackTrace();
    	} catch (IOException e) { 
    		e.printStackTrace();
    	}	
    	
    }

	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//--[# ID/PSW Regist, LogIn, ChangePsw & UnRegist Packet Message Parser Start --------------------->
	public static String coreAuthClientMsgPacketParser(String receiveManagerAuthMsgStr) 
		   throws InvalidKeyException, NoSuchProviderException, NoSuchAlgorithmException 
	{

		StringTokenizer idPswRegistLogInST = new StringTokenizer(receiveManagerAuthMsgStr, "$");
		String msgHeaderIDStr = idPswRegistLogInST.nextToken();
		
		String resultStr = null;
		
		switch(msgHeaderIDStr.toUpperCase()) {
			
			//---[ Client ID/PSW Regist & LogIn Parser Start ]-------------------------------

			//-----[ Client ID/PSW Regist Parser Process
			case moaCORE_IDExistAck_STR :				// IdExistAck Msg[4D4F410002] Parser Process
				 String atIDExistAckStr  = idPswRegistLogInST.nextToken(); 
				 
				 resultStr = atIDExistAckStr; 			// ID Exist Verify Result return
				 //System.out.println("[C:ManagerID/PSW Regist Parser Result] Received ID Exist Ack from Server : " + resultStr);
				 break;

			case moaCORE_IdPswRegRes_STR 	:			// ID/PSW Regist Response Msg[4D4F410004] Parser Process 
				 String atIdPswRegistResultStr  = idPswRegistLogInST.nextToken(); 
				 
				 resultStr = atIdPswRegistResultStr; 	// ID/PSW Regist Result return
				 //System.out.println("[C:ManagerID/PSW Regist Parser Result] Received ID/PSW Regist Result from Server : " + resultStr);
				 break;

			//---[ @@@ Manager Only Routine ------------------------------------------------------------------>		 
			case moaMANAGER_IdPswRegRes_STR 	:		// Manager - ID/PSW Regist Response Msg[4D4F410014] Parser Process 
				 String atManagerIdPswRegistResultStr  = idPswRegistLogInST.nextToken(); 
				 
				 resultStr = atManagerIdPswRegistResultStr; // ID/PSW Regist Result return
				 //System.out.println("[C:ManagerID/PSW Regist Parser Result] Received ID/PSW Regist Result from Server : " + resultStr);
				 break;
			 
			//-----[ Client ID/PSW LogIn Parser Process	 
			case moaCORE_IdPswLogInSTartRes_STR 	:	// ID/PSW Login Start Response Msg[4D4F412002] Parser Process 
				 String atIdPswLogInStartResResultStr  = idPswRegistLogInST.nextToken();
				 
				 resultStr = atIdPswLogInStartResResultStr; 
				 //System.out.println("[C:ManagerID/PSW Login Parser Result] Received LogIn Start Res Nonce Result from Server : " + resultStr);
				 break;

				 
			case moaCORE_IdPswLogInRes_STR :			// ID/PSW Login Response  Msg[4D4F412004] Parser Process
				 String nonceVefirySFResultStr  = idPswRegistLogInST.nextToken();
				 String authSFResultStr  = idPswRegistLogInST.nextToken();
				 //String authDBManagerLevelStr  = idPswRegistLogInST.nextToken();
				 
				 resultStr = nonceVefirySFResultStr.concat(strToken) + authSFResultStr; 
				 //System.out.println("[C:Franchise ID/PSW Login Parser Result] Received LogIn Res Auth Result from Server : " + resultStr);
				 break;

			//---[ @@@ Manager Only Routine ------------------------------------------------------------------>	 
			case moaMANAGER_IdPswLogInRes_STR :			// Manager - ID/PSW Login Response Msg[4D4F412014] Parser Process
				 String managerNonceVefirySFResultStr  = idPswRegistLogInST.nextToken();
				 String managerAuthSFResultStr  = idPswRegistLogInST.nextToken();
				 String ManagerAuthDBManagerLevelStr  = idPswRegistLogInST.nextToken();
				 
				 resultStr = managerNonceVefirySFResultStr.concat(strToken) + managerAuthSFResultStr.concat(strToken) + ManagerAuthDBManagerLevelStr; 
				 //System.out.println("[C:ManagerID/PSW Login Parser Result] Received LogIn Res Auth Result from Server : " + resultStr);
				 break;
				 
				 
			// Manager or Franchise ID CipheredPsw Update Remove in Manager AuthDB 	 
			case moaCORE_ChangePswRes_STR :				// ID/PSW Change PSW Response Msg[4D4F413002] Parser Process
				 String existPswVerifyResultStr  	= idPswRegistLogInST.nextToken();
				 String cipheredPswUpdateResultStr  = idPswRegistLogInST.nextToken();
				 
				 resultStr = existPswVerifyResultStr.concat(strToken) + cipheredPswUpdateResultStr; 
				 //System.out.println("[C:ManagerID/PSW Change Psw Parser Result] Received Change Psw Res Result from Server : " + resultStr);
				 break;
				 
			// ManagerID Record Remove in Manager AuthDB 	 
			case moaCORE_UnRegistRes_STR 	:			// ID/PSW UnRegist Response Msg[4D4F413008] Parser Process 
				 String atIdPswUnRegistResResultStr  = idPswRegistLogInST.nextToken();
				 
				 resultStr = atIdPswUnRegistResResultStr; 
				 //System.out.println("[C:Manager & Franchise ID/PSW UnRegist Parser Result] Received UnRegist Res Result from Server : " + resultStr);
				 break;
		
			default :						  
				 break;
		}
		
		return resultStr;
	
	}	
	

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//+++++[ MOA Manager & Franchise Regist Message Gen Methods
	//-----[ MOA Manager & Franchise Regist ID Exist Regist Request (for Regist)
	public static String coreIdExistRegistRequestMsgGenProcess(String idStr) {
		
		byte[] idBytes = idStr.getBytes(charset);
		// moaMANAGER_IDExist_STR [4D4F410001]
		String idExistMsgGenStr = moaCORE_IDExist_STR.concat(strToken) + Base64.toBase64String(idBytes);  

		//System.out.println("-------------------------------------------------------------------------------------");
		//System.out.println("[**] ID Exist Message Structure : Header$IDBase64");
		//System.out.println("[  ] idExistMsgGenStr(String)   : " + idExistMsgGenStr);
	
		return idExistMsgGenStr;
		
	}
	
	//-----[ MOA Manager Regist ID/PSW Regist Request (for Regist) 
	public static String coreIdPswRegistRequestMsgGenProcess(String managerLevelStr, String idStr, String passwordStr) throws Exception {
		
		byte[] idBytes =idStr.getBytes(charset);
		byte[] passwordBytes = passwordStr.getBytes(charset); 
		byte[] ivBytes = Hex.decode("00FF0000FF00FF000000FFFF000000FF");    						// 16Bytes
		byte[] keyBytes = new byte[ivBytes.length];

		byte[] idBytesDigestM = dg.HashDigest(hashAlg, idBytes, "BC"); 
		//System.out.println("[0x] idBytesDigestM(16����) : " + Hex.toHexString(idBytesDigestM));
		System.arraycopy(idBytesDigestM, 0, keyBytes, 0, ivBytes.length);
		//System.out.println("[0x] keyBytes(16����) : " + Hex.toHexString(keyBytes));
		sc = new SymmetricCoreAPI(symmetricAlg, ivBytes, keyBytes, "BC");
		byte[] encPswBytes = sc.SymmetricEncryptData(passwordBytes);
		//System.out.println("[0x] encPswBytes(16����) : " + Hex.toHexString(encPswBytes));
		
		byte[] pswDigestBytes = dg.HashDigest(hashAlg,  encPswBytes, "BC"); 						// Hash(AES(Key, Password)) 
		//System.out.println("[0x] hashPswBytes(16����) : " + Hex.toHexString(pswDigestBytes));
		byte[] idPswHmacDigestBytes = dg.HmacDigest(hmacAlg, idBytes, pswDigestBytes, "BC");	// Hmac(ID, Hash(Password))
		//System.out.println("[0x] hmacPswBytes(16����) : " + Hex.toHexString(idPswHmacDigestBytes));
		
		String idPswRegReqMsgStr = null;
		if (managerLevelStr == null) {
			// ID/PSW Regist Request MSG Gen [4D4F410003]
			idPswRegReqMsgStr = moaCORE_IdPswRegReq_STR.concat(strToken) + Base64.toBase64String(idBytes).concat(strToken) +
								Base64.toBase64String(pswDigestBytes).concat(strToken) + Base64.toBase64String(idPswHmacDigestBytes);  
			//System.out.println("-------------------------------------------------------------------------------------");
			//System.out.println("[**] ID/PSW Regist Request Message Structure : Header$IDBase64Str$HashPswBase64Str$HmacPswBase64Str");
			//System.out.println("[  ] idPswRegReqMsgStr(String)               : " + idPswRegReqMsgStr);
		} else {
			// ID/PSW Regist Request MSG Gen [4D4F410013]
			idPswRegReqMsgStr = moaMANAGER_IdPswRegReq_STR.concat(strToken) + managerLevelStr.concat(strToken) + Base64.toBase64String(idBytes).concat(strToken) +
								Base64.toBase64String(pswDigestBytes).concat(strToken) + Base64.toBase64String(idPswHmacDigestBytes);  
			//System.out.println("-------------------------------------------------------------------------------------");
			//System.out.println("[**] ID/PSW Regist Request Message Structure : Header$managerLevelStr$IDBase64Str$HashPswBase64Str$HmacPswBase64Str");
			//System.out.println("[  ] idPswRegReqMsgStr(String)               : " + idPswRegReqMsgStr);
		}	
		return idPswRegReqMsgStr;
		
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//+++++[ MOA Manager & Franchise LogIn Message Gen Methods
	public static String coreIdPswLogInStartRequestMsgGenProcess() {
		
		// moaMANAGER_IdPswLogInStartReq_STR [4D4F412001]
		String idPswLoginStartReqMsgGenStr = moaCORE_IdPswLogInStartReq_STR;  

		//System.out.println("-------------------------------------------------------------------------------------");
		//System.out.println("[**] ID/PSW LogIn Start Reqest Message Structure : Header");
		//System.out.println("[  ] idPswLoginStartReqMsgGenStr(String)         : " + idPswLoginStartReqMsgGenStr);
	
		return idPswLoginStartReqMsgGenStr;

	}
	
	public static String coreIdPswLogInRequestMsgGenProcess(String idStr, String passwordStr, String nonceBase64Str, boolean trueFlasFlag) throws Exception {
		
		byte[] idBytes =idStr.getBytes(charset);
		byte[] passwordBytes = passwordStr.getBytes(charset); 
		byte[] ivBytes = Hex.decode("00FF0000FF00FF000000FFFF000000FF");
		byte[] keyBytes = new byte[ivBytes.length];

		byte[] idBytesDigestM = dg.HashDigest(hashAlg, idBytes, "BC"); 

		System.arraycopy(idBytesDigestM, 0, keyBytes, 0, ivBytes.length);
		sc = new SymmetricCoreAPI(symmetricAlg, ivBytes, keyBytes, "BC");
		byte[] encPswBytes = sc.SymmetricEncryptData(passwordBytes);
		
		
		
		byte[] hashPswBytes = dg.HashDigest(hashAlg,  encPswBytes, "BC"); 			// Hash(AES(Key, Password)) 
		
		byte[] hmacPswBytes = dg.HmacDigest(hmacAlg, idBytes, hashPswBytes, "BC");	// Hmac(ID, Hash(Password))
		
		String idPswLogInReqMsgStr = null;
		if (trueFlasFlag == false) { 	// Franchise Routine
			// ID/PSW LogIn Request MSG Gen [4D4F412003]
			idPswLogInReqMsgStr = moaCORE_IdPswLogInReq_STR.concat(strToken) + Base64.toBase64String(idBytes).concat(strToken) +
								  Base64.toBase64String(hashPswBytes).concat(strToken) + Base64.toBase64String(hmacPswBytes).concat(strToken) +
								  nonceBase64Str;  
		} else {						// Manager Routine
			// ID/PSW LogIn Request MSG Gen [4D4F412013]
			idPswLogInReqMsgStr = moaMANAGER_IdPswLogInReq_STR.concat(strToken) + Base64.toBase64String(idBytes).concat(strToken) +
								  Base64.toBase64String(hashPswBytes).concat(strToken) + Base64.toBase64String(hmacPswBytes).concat(strToken) +
								  nonceBase64Str;  
			//System.out.println("-------------------------------------------------------------------------------------");
			//System.out.println("[**] ID/PSW Regist Request Message Structure : Header$ManagerIDBase64Str$HashPswBase64Str$HmacPswBase64Str$NonceBase64Str");
			//System.out.println("[  ] idPswRegReqMsgStr(String)               : " + idPswLogInReqMsgStr);
		}
	
		return idPswLogInReqMsgStr;
		
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//+++++[ MOA Manager & Franchise Change Password Message Gen Methods
	public static String coreIdPswChangePswRequestMsgGenProcess(String idStr, String existPswStr, String newPswStr) throws Exception {

		String existHashAndHamcPswConcatStr = coreIdPswHashPswAndHamcPswGen(idStr, existPswStr);
		String newHashAndHamcPswConcatStr   = coreIdPswHashPswAndHamcPswGen(idStr, newPswStr);
		byte[] idBytes =idStr.getBytes(charset);

		// moaMANAGER_ChagePswReq_STR [4D4F413001]
		String idPswChangePswReqMsgGenStr = moaCORE_ChangePswReq_STR.concat(strToken) + Base64.toBase64String(idBytes).concat(strToken) +
										   existHashAndHamcPswConcatStr.concat(strToken) + newHashAndHamcPswConcatStr;  
		//System.out.println("-------------------------------------------------------------------------------------");
		//System.out.println("[**] ID/PSW Change PSW Reqest Message Structure : Header$ManagerIDBase64Str$HashPswBse64Str$HmacPswBse64Str$NewHashPswBse64Str$NewHmacPswBse64Str");
		//System.out.println("[  ] idPswChangePswReqMsgGenStr(String)         : " + idPswChangePswReqMsgGenStr);

		return idPswChangePswReqMsgGenStr;

	}
	
	private static String coreIdPswHashPswAndHamcPswGen(String idStr, String existOrNewPswStr) throws NullPointerException, Exception 
	{
		
		byte[] idBytes =idStr.getBytes(charset);
		byte[] passwordBytes = existOrNewPswStr.getBytes(charset); 
		byte[] ivBytes = Hex.decode("00FF0000FF00FF000000FFFF000000FF");
		byte[] keyBytes = new byte[ivBytes.length];

		byte[] idBytesDigestM = dg.HashDigest(hashAlg, idBytes, "BC"); 

		System.arraycopy(idBytesDigestM, 0, keyBytes, 0, ivBytes.length);
		sc = new SymmetricCoreAPI(symmetricAlg, ivBytes, keyBytes, "BC");
		byte[] encPswBytes = sc.SymmetricEncryptData(passwordBytes);
		
		byte[] hashPswBytes = dg.HashDigest(hashAlg,  encPswBytes, "BC"); 				// Hash(AES(Key, Password)) 
		byte[] hmacIdPswBytes = dg.HmacDigest(hmacAlg, idBytes, hashPswBytes, "BC");	// Hmac(ID, Hash(Password))
		
		return Base64.toBase64String(hashPswBytes).concat(strToken) + Base64.toBase64String(hmacIdPswBytes);
		
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//+++++[ MOA Manager & Franchise UnRegist Message Gen Methods
	public static String coreIdPswUnRegistRequestMsgGenProcess(String idStr) throws Exception {
		
		byte[] idBytes =idStr.getBytes(charset);
		
		// moaMANAGER_UnRegistReq_STR [4D4F413007]
		String idPswUnRegistReqMsgGenStr = moaCORE_UnRegistReq_STR.concat(strToken) + Base64.toBase64String(idBytes);  

		//System.out.println("-------------------------------------------------------------------------------------");
		//System.out.println("[**] ID/PSW UnRegist(managerID Record Delete) Reqest Message Structure : Header$ManagerIDBase64Str");
		//System.out.println("[  ] idPswUnRegistReqMsgGenStr(String)                                 : " + idPswUnRegistReqMsgGenStr);
	
		return idPswUnRegistReqMsgGenStr;
		
	}
	
	// coreIdPswRegistRequestMsgGenProcess(null, id, pwd)
	
}	// End of MoaClientManagerAuthLib CLASS 

