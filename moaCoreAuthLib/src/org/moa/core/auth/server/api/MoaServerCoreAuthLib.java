package org.moa.core.auth.server.api;

//import java.io.*;
import java.security.*;
import java.sql.Date;
import java.util.Calendar;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bouncycastle.util.encoders.Base64;
import org.moa.crypto.coreapi.DigestCoreAPI;
import org.moa.crypto.coreapi.SecureRNGCoreAPI;

import com.moaplanet.manager_authdb.AuthRepository;
import com.moaplanet.manager_authdb.AuthService;
import com.moaplanet.manager_authdb.FranchiseAuthDTO;
import com.moaplanet.manager_authdb.ManagerAuthDTO;


public class MoaServerCoreAuthLib {

	// Moa Crypto Core API Call
	static SecureRNGCoreAPI srng = new SecureRNGCoreAPI();	// Moa Crypto Core API - Secure Random Library Call
	static DigestCoreAPI dg = new DigestCoreAPI();			// Moa Crypto Core API - Digest(Hash & Hmac) Library Call
	
/*	// Define about MOA Manager ID Parsing for ID & Password : ID/Psw Regist 
	protected static final String moaMANAGER_IDExist_STR				= "4D4F410001";  // Msg Manager ID : MOA0001 ***
	protected static final String moaMANAGER_IDExistAck_STR				= "4D4F410002";  // Msg Manager ID : MOA0002
	protected static final String moaMANAGER_IdPswRegReq_STR			= "4D4F410003";  // Msg Manager ID : MOA0003 ***
	protected static final String moaMANAGER_IdPswRegRes_STR			= "4D4F410004";  // Msg Manager ID : MOA0004
	
	// Define about MOA Manager ID Parsing for ID & Password : ID/Psw LogIn
	protected static final String moaMANAGER_IdPswLogInStartReq_STR		= "4D4F412001";  // Msg Manager ID : MOA2001
	protected static final String moaMANAGER_IdPswLogInSTartRes_STR		= "4D4F412002";  // Msg Manager ID : MOA2002
	protected static final String moaMANAGER_IdPswLogInReq_STR			= "4D4F412003";  // Msg Manager ID : MOA2003
	protected static final String moaMANAGER_IdPswLogInRes_STR			= "4D4F412004";  // Msg Manager ID : MOA2004
	
	protected static final String moaMANAGER_ChagePswReq_STR			= "4D4F413001";  // Msg Manager ID : MOA3001
	protected static final String moaMANAGER_ChagePswRes_STR			= "4D4F413002";  // Msg Manager ID : MOA3002
	protected static final String moaMANAGER_UnRegistReq_STR			= "4D4F413007";  // Msg Manager ID : MOA3007
	protected static final String moaMANAGER_UnRegistRes_STR			= "4D4F413008";  // Msg Manager ID : MOA3008

*/
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

	protected static final String  moaCORE_BCWalletRegistReq_STR	= "4D4F414003";  // Msg Manager ID : MOA4003
	protected static final String  moaCORE_BCWalletRegistRes_STR	= "4D4F414004";  // Msg Manager ID : MOA4004
	protected static final String  moaCORE_BCWalletRestoreReq_STR	= "4D4F414007";  // Msg Manager ID : MOA4007
	protected static final String  moaCORE_BCWalletRestoreRes_STR	= "4D4F414008";  // Msg Manager ID : MOA4008
	
	protected static final String moaCore_WalletRegistSuccess		= "6080";
	protected static final String moaCore_WalletRegistFail 			= "6081";
	protected static final String moaCore_WalletRestoreSuccess		= "6084";
	protected static final String moaCore_WalletRestoreFail 		= "6085";
	
	protected static final String mParserTokenizer = "$";
		
		
	static Charset charset = Charset.forName("UTF-8");
	static String strToken = "$";
	static int SaltSize = 32;
	static int NonceSize = 32;
	
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
	

	public MoaServerCoreAuthLib(String moaB2CManagerPropertiesFileName) //throws FileNotFoundException, IOException 
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
	
	public MoaServerCoreAuthLib() {};
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	//--[# MOA Manager ID/PSW Regist & LogIN Packet Message Parser Start ------------------------->
	public static String coreAuthServerMsgPacketParser(String receiveAuthMsgStr, String serverNonceBase64Str) 
		   throws GeneralSecurityException 
	{
		
		StringTokenizer idPswRegistLoginST = new StringTokenizer(receiveAuthMsgStr, "$");
		String msgHeaderIDStr = idPswRegistLoginST.nextToken();
		
		String resultStr = null; 
		
		switch(msgHeaderIDStr.toUpperCase()) {
		
			//---[ Server PIN Regist & LogIn Parser
				 //L---> Server PIN Regist Parser
			case moaCORE_IDExist_STR 	: 			// IdPsw ID Exist Msg[4D4F410001]Parser Process	
				
				String atRegistIDBase64Str  = idPswRegistLoginST.nextToken();
				
				byte[] idBytes = Base64.decode(atRegistIDBase64Str);
				String atRegistIdStr = new String(idBytes, 0, idBytes.length, charset);
				
				resultStr = atRegistIdStr;
				
				break;

			case moaCORE_IdPswRegReq_STR 	: 		// IdPsw Regist Request Msg[4D4F410003] Parser Process 
				
				String atRegistIdBase64Str 		 	= idPswRegistLoginST.nextToken();
				String atRegistHashPswBase64Str  	= idPswRegistLoginST.nextToken();
				String atRegistHmacPswBase64Str  	= idPswRegistLoginST.nextToken();
					
				String saltBase64Str = coreSaltGen(SaltSize);	// +++ Salt 32Bytes Gen -> Storage in AuthDB using MoaAuthDao.jar
				String cipheredPswBase64Str = coreIdPswRegistAuthInfoGen(saltBase64Str, atRegistHashPswBase64Str, atRegistHmacPswBase64Str);	

				resultStr = atRegistIdBase64Str.concat(strToken) + saltBase64Str.concat(strToken) + cipheredPswBase64Str;
				break;
			
			//~~~~~~[ Only Manager MSG Parser 	
			case moaMANAGER_IdPswRegReq_STR 	: 		// IdPsw Regist Request Msg[4D4F410013] Parser Process 
				
				String atRegistManagerLevelInfoStr		= idPswRegistLoginST.nextToken();
				String atRegistManagerIdBase64Str		= idPswRegistLoginST.nextToken();
				String atRegistManagerHashPswBase64Str 	= idPswRegistLoginST.nextToken();
				String atRegistManagerHmacPswBase64Str 	= idPswRegistLoginST.nextToken();
					
				String saltManagerBase64Str = coreSaltGen(SaltSize);	// +++ Salt 32Bytes Gen -> Storage in AuthDB using MoaAuthDao.jar
				String cipheredPswManagerBase64Str = coreIdPswRegistAuthInfoGen(saltManagerBase64Str, atRegistManagerHashPswBase64Str, atRegistManagerHmacPswBase64Str);	

				resultStr = atRegistManagerLevelInfoStr.concat(strToken) + atRegistManagerIdBase64Str.concat(strToken) + 
						saltManagerBase64Str.concat(strToken) + cipheredPswManagerBase64Str;
				break;

			
			//+++++++++++++++++++++++++++++++++++++[ Server ID/PSW Login Parser
			case moaCORE_IdPswLogInStartReq_STR 	: 	// IdPsw Login Request Msg[4D4F412001] Parser Process 

				 byte[] nonceBytes = coreNonceOTPGen(NonceSize);
				 String nonceBase64Str = Base64.toBase64String(nonceBytes);
				 //System.out.println("[S:ID/PSW LogIn] nonceBytes : " + Hex.toHexString(nonceBytes));
				
				 resultStr = nonceBase64Str; 			// nonceBase64 return
				 break;

			case moaCORE_IdPswLogInReq_STR 		: 	// IDPsw Login Response Msg[4D4F412013] Parser Process

				 String atFranchiseIDBase64ResultStr 	= idPswRegistLoginST.nextToken(); 
				 String atFranchiseHashPswBase64Str		= idPswRegistLoginST.nextToken();
				 String atFranchiseHmacPSWBase64Str 	= idPswRegistLoginST.nextToken();
				 String atFranchiseNonceBase64Str 		= idPswRegistLoginST.nextToken();
				 
				 String franchiseNonceVerifySuccess 	= "0x6030"; 
				 String franchisenonceVerifyFail 		= "0x6031";
						
				 System.out.println("[**] Orginal  nonceOTP : " + serverNonceBase64Str);
				 System.out.println("[**] Received nonceOTP : " + atFranchiseNonceBase64Str);
						
				 if ( !atFranchiseNonceBase64Str.equals(serverNonceBase64Str) ) {	// if OTP Check Fail, Stop
					 System.out.println("Nonce Check Fail, PIN Login Progress Stop!");
					 resultStr = franchisenonceVerifyFail.concat(strToken) + "0000".concat(strToken) + "0000".concat(strToken) + "0000"; 	
					 return resultStr;
					 
				 }	else {
				 
					 System.out.println("[**] Nonce Check Success");
					 resultStr = franchiseNonceVerifySuccess.concat(strToken) + atFranchiseIDBase64ResultStr.concat(strToken) + 
							 	 atFranchiseHashPswBase64Str.concat(strToken) + atFranchiseHmacPSWBase64Str; 	
				 }
						
				 break;
	 
			case moaMANAGER_IdPswLogInReq_STR 		: 	// IDPsw Login Response Msg[4D4F412013] Parser Process

				 String atManagerIDBase64ResultStr = idPswRegistLoginST.nextToken(); 
				 String atHashPswBase64ResultStr = idPswRegistLoginST.nextToken();
				 String atHmacPSWBase64Str = idPswRegistLoginST.nextToken();
				 String atNonceBase64Str = idPswRegistLoginST.nextToken();
				 
				 String nonceVerifySuccess 	= "0x6030"; 
				 String nonceVerifyFail 	= "0x6031";
						
				 System.out.println("[**] Orginal  nonceOTP : " + serverNonceBase64Str);
				 System.out.println("[**] Received nonceOTP : " + atNonceBase64Str);
						
				 if ( !atNonceBase64Str.equals(serverNonceBase64Str) ) {	// if OTP Check Fail, Stop
					 System.out.println("Nonce Check Fail, PIN Login Progress Stop!");
					 resultStr = nonceVerifyFail.concat(strToken) + "0000".concat(strToken) + "0000".concat(strToken) + "0000"; 	
					 return resultStr;
					 
				 }	else {
				 
					 System.out.println("[**] Nonce Check Success");
					 resultStr = nonceVerifySuccess.concat(strToken) + atManagerIDBase64ResultStr.concat(strToken) + 
							 	 atHashPswBase64ResultStr.concat(strToken) + atHmacPSWBase64Str; 	
				 }
						
				 break;
				 
			case moaCORE_ChangePswReq_STR			:	// IDPsw Change Psw Login Response Msg[4D4F413001] Parser Process

				 String atChangePswManagerIDBase64ResultStr = idPswRegistLoginST.nextToken(); 
				 String atExistHashPswBase64ResultStr = idPswRegistLoginST.nextToken();
				 String atExistHmacPSWBase64Str = idPswRegistLoginST.nextToken();
				 String atNewHashPswBase64ResultStr = idPswRegistLoginST.nextToken();
				 String atNewHmacPswBase64ResultStr = idPswRegistLoginST.nextToken();
				 
				 resultStr = atChangePswManagerIDBase64ResultStr.concat(strToken) + 
						     atExistHashPswBase64ResultStr.concat(strToken) + atExistHmacPSWBase64Str.concat(strToken) +
						     atNewHashPswBase64ResultStr.concat(strToken) + atNewHmacPswBase64ResultStr;
				 //System.out.println("[S:ID/PSW Change PSW] Received ID/HashPsw/HmacPsw/NewHashPsw/NewHmacPsw from Client PC : " + resultStr);
				 
				 break;
				 
			case moaCORE_UnRegistReq_STR 		: 	// IDPsw UnRegist Request Msg[4D4F413007] Parser Process	 
				
				 String atUnRegistManagerIDBase64Str  = idPswRegistLoginST.nextToken();
				
				 byte[] unRegistIdBytes = Base64.decode(atUnRegistManagerIDBase64Str);
				 String unRegistManagerIdStr = new String(unRegistIdBytes, 0, unRegistIdBytes.length, charset);
				
				 resultStr = unRegistManagerIdStr;
				
				 break;

	 
			default :								
				 break;

		}

		return resultStr;
	}	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//+++++[ ID/PSW Regist MSG Gen ]  
	public static String coreIdExistAckMsgGenProcess(String idExgistInfo) {
		
		// moaMANAGER_IDExistAck_STR[4D4F410002]
		String idExistAckMsgGenStr = moaCORE_IDExistAck_STR + "$" + idExgistInfo;  

		//System.out.println("-------------------------------------------------------------------------------------");
		//System.out.println("[**] ID Exist Ack Message Structure : Header$IDExistAck");
		//System.out.println("[  ] idExistAckMsgGenStr(String)    : " + idExistAckMsgGenStr);
	
		return idExistAckMsgGenStr;
		
	}
	
	private static String coreIdPswRegistAuthInfoGen(String genSaltBase64Str, String hashDigestBase64Str, String hmacDigestBase64Str) 
			   throws NoSuchProviderException, InvalidKeyException, NoSuchAlgorithmException 
	{
				
		byte[] slatHashPswConnectBytes = null;
		byte[] slatHmacIdPswConnectBytes = null;
		byte[] cipheredIdPswHmacDigestBytes = null;
				
		byte[] saltBytes = Base64.decode(genSaltBase64Str);
				
		byte[] hashPswBytes = Base64.decode(hashDigestBase64Str);
		byte[] hmacPswBytes = Base64.decode(hmacDigestBase64Str); 
		//System.out.println("[0x] passwordDigestS(16����) : " + Hex.toHexString(hashPswBytes) + " [����] : " + hashPswBytes.length);
		//System.out.println("[0x] idPswHmacDigestS(16����) : " + Hex.toHexString(hmacPswBytes) + " [����] : " + hmacPswBytes.length);
		//-------------------------------------------------------------------
				
		int connectRule = saltBytes[0] % 2;
		if (connectRule == 0) {	// if even : Data = slat||Hash(Password), IK = Hmac(ID, Hash(Password))  

			slatHashPswConnectBytes = new byte[saltBytes.length + hashPswBytes.length];
			System.arraycopy(saltBytes, 0, slatHashPswConnectBytes, 0, saltBytes.length);
			System.arraycopy(hashPswBytes, 0, slatHashPswConnectBytes, saltBytes.length, hashPswBytes.length);
					
			cipheredIdPswHmacDigestBytes = dg.HmacDigest(strongHmacAlg, slatHashPswConnectBytes, hmacPswBytes, "BC");
			//System.out.println("[0x] Final HmacSHA256 Password(16����) : " + Hex.toHexString(cipheredIdPswHmacDigestBytes) + 
			//				   " [����] : " + cipheredIdPswHmacDigestBytes.length);

		} else {				// if odd  : Data = slat||Hmac(ID, Hash(Password)), IK = Hash(Password)

			slatHmacIdPswConnectBytes = new byte[saltBytes.length + hmacPswBytes.length];
			System.arraycopy(saltBytes, 0, slatHmacIdPswConnectBytes, 0, saltBytes.length);
			System.arraycopy(hmacPswBytes, 0, slatHmacIdPswConnectBytes, saltBytes.length, hmacPswBytes.length);
					
			cipheredIdPswHmacDigestBytes = dg.HmacDigest(strongHmacAlg, slatHmacIdPswConnectBytes, hashPswBytes, "BC");
			//System.out.println("[0x] Final HmacSHA256 Password(16����) : " + Hex.toHexString(cipheredIdPswHmacDigestBytes) + 
			//			   	   " [����] : " + cipheredIdPswHmacDigestBytes.length);
		}	
				
		return Base64.toBase64String(cipheredIdPswHmacDigestBytes);
	}
	
	private static String coreSaltGen(int number) 
			   throws NoSuchAlgorithmException, NoSuchProviderException, NullPointerException {
			
			byte[] saltBytes = srng.SecureRandNumberGen(number);
			
			return Base64.toBase64String(saltBytes);
			
	}
	
	public static String coreIdPswRegistResponseMsgGenProcess(String idPswRegistSFInfo, boolean fmFlag) {
		
		String idPswRegResMsgGenStr = "";
		if (fmFlag == false) { 
			// moaCORE_IdPswRegRes_STR[4D4F410004]
			idPswRegResMsgGenStr = moaCORE_IdPswRegRes_STR.concat(strToken) + idPswRegistSFInfo;  

			//System.out.println("-------------------------------------------------------------------------------------");
			//System.out.println("[**] ID/PSW Regist Res Message Structure : Header$FranchieResultStr");
			//System.out.println("[  ] idExistAckMsgGenStr(String)         : " + idPswRegResMsgGenStr);
		} else {
			// moaMANAGER_IdPswRegRes_STR[4D4F410014]
			idPswRegResMsgGenStr = moaMANAGER_IdPswRegRes_STR.concat(strToken) + idPswRegistSFInfo;  

			//System.out.println("-------------------------------------------------------------------------------------");
			//System.out.println("[**] ID/PSW Regist Res Message Structure : Header$ManagerResultStr");
			//System.out.println("[  ] idExistAckMsgGenStr(String)         : " + idPswRegResMsgGenStr);
		}
		return idPswRegResMsgGenStr;		
	}
	
/*
	public static String managerIdPswRegistResponseMsgGenProcess(String idPswRegistSFInfo) {
		
		// moaMANAGER_IdPswRegRes_STR[4D4F410014]
		String idPswRegResMsgGenStr = moaMANAGER_IdPswRegRes_STR.concat(strToken) + idPswRegistSFInfo;  

		//System.out.println("-------------------------------------------------------------------------------------");
		//System.out.println("[**] ID/PSW Regist Res Message Structure : Header$ResultStr");
		//System.out.println("[  ] idExistAckMsgGenStr(String)         : " + idPswRegResMsgGenStr);
	
		return idPswRegResMsgGenStr;		
	}
*/
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//+++++[ ID/PSW LogIn MSG Gen ]
	private static byte[] coreNonceOTPGen(int number) 
			   throws NoSuchAlgorithmException, NoSuchProviderException, NullPointerException 
	{ 
		byte[] nonceOTPBytes = srng.SecureRandNumberGen(number);

		return nonceOTPBytes;
	}
	
	public static String coreIdPswLoginStartResponseMsgGenProcess(String nonceBase64Str) { 
		
		// moaMANAGER_IdPswLogInSTartRes_STR[4D4F412002]
		String idPswLoginStartResMsgGenStr = moaCORE_IdPswLogInSTartRes_STR.concat(strToken) + nonceBase64Str;  

		//System.out.println("-------------------------------------------------------------------------------------");
		//System.out.println("[**] ID/PSW LogIn Start Response Message Structure : Header$NonceBase64Str");
		//System.out.println("[  ] idPswLoginStartResMsgGenStr(String)           : " + idPswLoginStartResMsgGenStr);
	
		return idPswLoginStartResMsgGenStr;		
	}
	
	
	public static String coreIdPswLogInAuthInfoGen(String storagedSaltBase64Str, String hashPswBase64Str, String hmacPswBase64Str) 
			   throws NoSuchProviderException, InvalidKeyException, NoSuchAlgorithmException 
	{
				
		byte[] saltHashPswConnectBytes = null;
		byte[] saltHmacIdPswConnectBytes = null;
		byte[] sendFinalIdPswHmacDigest = null;
					
		byte[] saltBytes = Base64.decode(storagedSaltBase64Str);
					
		byte[] hashPswBytes = Base64.decode(hashPswBase64Str);
		byte[] hmacPswBytes = Base64.decode(hmacPswBase64Str); 
		//System.out.println("[0x] passwordDigestS(16����) : " + Hex.toHexString(hashPswBytes) + " [����] : " + hashPswBytes.length);
		//System.out.println("[0x] idPswHmacDigestS(16����) : " + Hex.toHexString(hmacPswBytes) + " [����] : " + hmacPswBytes.length);
		//-------------------------------------------------------------------
					
		int connectRule = saltBytes[0] % 2;
		if (connectRule == 0) {	// if even : Data = salt||Hash(Password), IK = Hmac(ID, Hash(Password))  

			saltHashPswConnectBytes = new byte[saltBytes.length + hashPswBytes.length];
			System.arraycopy(saltBytes, 0, saltHashPswConnectBytes, 0, saltBytes.length);
			System.arraycopy(hashPswBytes, 0, saltHashPswConnectBytes, saltBytes.length, hashPswBytes.length);
						
			sendFinalIdPswHmacDigest = dg.HmacDigest(strongHmacAlg, saltHashPswConnectBytes, hmacPswBytes, "BC");
			//System.out.println("[0x] Final HmacSHA256 Password(16����) : " + Hex.toHexString(sendFinalIdPswHmacDigest) + 
			//				   " [����] : " + sendFinalIdPswHmacDigest.length);

		} else {				// if odd  : Data = salt||Hmac(ID, Hash(Password)), IK = Hash(Password)

			saltHmacIdPswConnectBytes = new byte[saltBytes.length + hmacPswBytes.length];
			System.arraycopy(saltBytes, 0, saltHmacIdPswConnectBytes, 0, saltBytes.length);
			System.arraycopy(hmacPswBytes, 0, saltHmacIdPswConnectBytes, saltBytes.length, hmacPswBytes.length);
				
			sendFinalIdPswHmacDigest = dg.HmacDigest(strongHmacAlg, saltHmacIdPswConnectBytes, hashPswBytes, "BC");
			//System.out.println("[0x] Final HmacSHA256 Password(16����) : " + Hex.toHexString(sendFinalIdPswHmacDigest) + 
			//				   " [����] : " + sendFinalIdPswHmacDigest.length);

		}	
					
		return Base64.toBase64String(sendFinalIdPswHmacDigest);
	}

	//~~~~~~[In case of Manager : managerLevelStr is not null. 
	public static String coreIdPswLogInResponseMsgGenProcess(String nonceVerifySFStr, String managerFranchiseAuthSFStr, String managerLevelStr) {
		
		String idPswLoginResMsgGenStr ="";
		
		if (managerLevelStr == null) {		// Franchise process 
			// moaMANAGER_IdPswLogInRes_STR[4D4F412004]
			idPswLoginResMsgGenStr = moaCORE_IdPswLogInRes_STR.concat(strToken) + nonceVerifySFStr.concat(strToken) + managerFranchiseAuthSFStr;  

			//System.out.println("-------------------------------------------------------------------------------------");
			//System.out.println("[**] ID/PSW LogIn Response Message Structure : Header$NonceVerifySFStr$FranchiseAuthSFStr");
			//System.out.println("[  ] idPswLoginResMsgGenStr(String)          : " + idPswLoginResMsgGenStr);
		} else {								// Manager process
			// moaMANAGER_IdPswLogInRes_STR[4D4F412014]
			idPswLoginResMsgGenStr = moaMANAGER_IdPswLogInRes_STR.concat(strToken) + 
											nonceVerifySFStr.concat(strToken) + managerFranchiseAuthSFStr.concat(strToken) + managerLevelStr;  

			//System.out.println("-------------------------------------------------------------------------------------");
			//System.out.println("[**] ID/PSW LogIn Response Message Structure : Header$NonceVerifySFStr$managerAuthSFStr$managerLevelStr");
			//System.out.println("[  ] idPswLoginResMsgGenStr(String)          : " + idPswLoginResMsgGenStr);
			
		}
		return idPswLoginResMsgGenStr;		
	}

	//~~~~~~~~~~~[ Only Manager MSG Gen Process 
/*	public static String ManagerIdPswLogInResponseMsgGenProcess(String nonceVerifySFStr, String managerAuthSFStr, String managerLevelStr) {
		
		// moaMANAGER_IdPswLogInRes_STR[4D4F412014]
		String idPswLoginResMsgGenStr = moaMANAGER_IdPswLogInRes_STR.concat(strToken) + 
										nonceVerifySFStr.concat(strToken) + managerAuthSFStr.concat(strToken) + managerLevelStr;  

		//System.out.println("-------------------------------------------------------------------------------------");
		//System.out.println("[**] ID/PSW LogIn Response Message Structure : Header$NonceVerifySFStr$managerAuthSFStr$managerLevelStr");
		//System.out.println("[  ] idPswLoginResMsgGenStr(String)          : " + idPswLoginResMsgGenStr);
	
		return idPswLoginResMsgGenStr;		
	}
*/	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//+++++[ ID/PSW Change PSW MSG Gen ]~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public static String coreIdPswChangePswResponseMsgGenProcess(String existPswVerifyResultStr, String cipheredPswUpdateResultStr) {
		
		// moaMANAGER_ChagePswRes_STR[4D4F413002]
		String idPswChagePswResMsgGenStr = moaCORE_ChangePswRes_STR.concat(strToken) + 
										   existPswVerifyResultStr.concat(strToken) + cipheredPswUpdateResultStr;   

		//System.out.println("-------------------------------------------------------------------------------------");
		//System.out.println("[**] ID/PSW Change Psw Response Message Structure : Header$existPswVerifyResultStr$cipheredPswUpdateResultStr");
		//System.out.println("[  ] idPswChagePswResMsgGenStr(String)            : " + idPswChagePswResMsgGenStr);
	
		return idPswChagePswResMsgGenStr;		
		
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//+++++[ ID/PSW UnRegist MSG Gen ]~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public static String coreIdPswUnRegistResponseMsgGenProcess(String managerOrFranchiseIdAuthDBRemoveSFStr) {
		
		// moaMANAGER_UnRegistRes_STR[4D4F413008]
		String idPswUnRegistResMsgGenStr = moaCORE_UnRegistRes_STR.concat(strToken) + managerOrFranchiseIdAuthDBRemoveSFStr;  

		//System.out.println("-------------------------------------------------------------------------------------");
		//System.out.println("[**] ID/PSW UnRegist Response Message Structure : Header$managerOrFranchiseIdAuthDBRemoveSFStr");
		//System.out.println("[  ] idPswUnRegistResMsgGenStr(String)          : " + managerOrFranchiseIdAuthDBRemoveSFStr);
	
		return idPswUnRegistResMsgGenStr;		
	}
	

	public static String handleWalletReq(String message) {
		try {
			StringTokenizer walletReq = new StringTokenizer(message, mParserTokenizer);
			String msgHeaderIDStr = walletReq.nextToken();
	
			switch (msgHeaderIDStr) {
			case moaCORE_BCWalletRegistReq_STR :
				return registFranchWallet(walletReq);
				
			case moaCORE_BCWalletRestoreReq_STR :
				return SelectFranchWallet(walletReq);
				
			default :								
				throw new Exception();
			}
		} catch (Exception e) {
			return "Request not allowed (E0001)";
		}
	}
	
	public static String handleFranchUserReq(String message) {
		try {
			StringTokenizer walletReq = new StringTokenizer(message, mParserTokenizer);
			String msgHeaderIDStr = walletReq.nextToken();
	
			switch (msgHeaderIDStr) {
			case moaCORE_BCWalletRegistReq_STR :
				return registFranchWallet(walletReq);
				
			case moaCORE_BCWalletRestoreReq_STR :
				return SelectFranchWallet(walletReq);
				
			default :								
				throw new Exception();
			}
		} catch (Exception e) {
			return "Request not allowed (E0001)";
		}
	}
	

	private static String registFranchWallet(StringTokenizer walletReq) {
		String msgBodyId  = new String(Base64.decode(walletReq.nextToken()));
		
		AuthService<FranchiseAuthDTO> authService = null;
		try {
			authService = new AuthRepository<FranchiseAuthDTO>(FranchiseAuthDTO.class);
			System.out.println("registFranchWallet id :" + msgBodyId);
			FranchiseAuthDTO authDto = authService.selectOne(msgBodyId);
			
			if (authDto == null)
				throw new Exception("E001001 There is no wallet to save with ID " + msgBodyId);
			
			String msgBodyPsw = walletReq.nextToken();
			String msgBodyPrk = walletReq.nextToken();
			String msgBodyPuk = walletReq.nextToken();
			
			authDto.setCipheredWalletPrk(msgBodyPrk);
			authDto.setCipheredWalletPuk(msgBodyPuk);

			System.out.println(authDto);

			authService.updateEntity(authDto);
			
		} catch (Exception e) {
			e.printStackTrace();
			return moaCORE_BCWalletRegistRes_STR + mParserTokenizer + moaCore_WalletRegistFail + mParserTokenizer + e.getMessage();
			
		} finally {
			//종료 시 close() 호출
			try { authService.close(); } catch (Exception e) {}
		}
		
		return moaCORE_BCWalletRegistRes_STR + mParserTokenizer + moaCore_WalletRegistSuccess;
	}
	
	private static String SelectFranchWallet(StringTokenizer walletReq) {
		String msgBodyId  = new String(Base64.decode(walletReq.nextToken()));
		String resultStr = null;
		AuthService<FranchiseAuthDTO> authService = new AuthRepository<FranchiseAuthDTO>(FranchiseAuthDTO.class);
		
		try {
			System.out.println("SelectFranchWallet id :" + msgBodyId);
			FranchiseAuthDTO authDto = authService.selectOne(msgBodyId);
		
			System.out.println(authDto);
			
			resultStr = authDto.getCipheredWalletPrk()
						+ mParserTokenizer + authDto.getCipheredWalletPuk();
			
		} catch (Exception e) {
			e.printStackTrace();
			return moaCORE_BCWalletRestoreRes_STR + mParserTokenizer + moaCore_WalletRestoreFail;
		} finally {
			//종료 시 close() 호출
			try { authService.close(); } catch (Exception e) {}
		}
		
		
		return moaCORE_BCWalletRestoreRes_STR + mParserTokenizer + moaCore_WalletRestoreSuccess + mParserTokenizer + resultStr;
	}
	
	//String idPswRegReqMsgGenStr = moaClientFranchiseAuthLib.coreIdPswRegistRequestMsgGenProcess(null, idStr, passwordStr);		// ID/PSW RegReq Msg Gen
	private static String updateFranchUserPwd(FranchiseAuthDTO authDto) {
		AuthService<FranchiseAuthDTO> authService = null;
		try {
			authService = new AuthRepository<FranchiseAuthDTO>(FranchiseAuthDTO.class);
			System.out.println("updateFranchUser id :" + authDto.getId());
			FranchiseAuthDTO selectedFranchUser = authService.selectOne(authDto.getId());
			
			if (selectedFranchUser == null)
				throw new Exception("E001001 There is no UserData to save with ID " + authDto.getId());
			
			Calendar calendar = Calendar.getInstance();
            java.util.Date date = calendar.getTime();
            // TODO 변경가능 시간 여부 확인.
            
            selectedFranchUser.setCipheredPwd(authDto.getCipheredPwd());
            selectedFranchUser.setSalt(authDto.getSalt());


			authService.updateEntity(selectedFranchUser);
			
		} catch (Exception e) {
			e.printStackTrace();
			return moaCORE_BCWalletRegistRes_STR + mParserTokenizer + moaCore_WalletRegistFail + mParserTokenizer + e.getMessage();
			
		} finally {
			//종료 시 close() 호출
			try { authService.close(); } catch (Exception e) {}
		}
		
		return moaCORE_BCWalletRegistRes_STR + mParserTokenizer + moaCore_WalletRegistSuccess;
	}
	
	
} // End of MoaServerMangerAuthLib CLASS

