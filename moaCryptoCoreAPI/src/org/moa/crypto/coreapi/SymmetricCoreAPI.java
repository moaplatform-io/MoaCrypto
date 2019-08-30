package org.moa.crypto.coreapi;

//import java.security.InvalidKeyException;
import java.util.*;

//---BC & SunJCE
//import javax.crypto.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import javax.crypto.spec.IvParameterSpec;

/**
 * [*] 기능 정의 : 비밀키암호 기반 암호화 및 복호화를 수행하는 핵심 API를 제공한다. <br>
 * [*] 파일명 : SymmetricCoreAPI.java <br>
 * [*] 최초 작성일 : 2018.10.15 <br>
 * [*] 최근 수정일 : 2018.11.08 <br>
 * @author Bob <br>  */
public class SymmetricCoreAPI
{

	static Cipher cipher = null;
	static IvParameterSpec ivSpec = null;

    //----- [BC & SunJCE Key Factory Declare Start ------------------
	//SecretKey des3Key; 			// 3DES Key Generation
	static SecretKeySpec keySpec;	// AES, SEED, LEA Key Generation
    //----- [BC & SunJCE Key Factory Declare End --------------------
	
	// CryptonameModePadtype = AES/CBC/PKCS7Padding, AES/CTR/NoPadding, ..., etc.
	static String atCryptoAlgName 	= null; // Crypto Alg Name
	static String atModeType 		= null; // Crypto Alg Mode

	
	public SymmetricCoreAPI() {
		
	}
	
	/**
	 * 비밀키 암호화(AES, SEED, ..., etc.)를 위한 설정 생성자이다.
	 * @param CryptonameModePadtype 비밀키 암호화를 위한 문자열 정보(ex: AES/CBC/PKCS7Padding, AES/CTR/NoPadding, ..., etc.) 입력
	 * @param ivBytes 비밀키 암호모드중 ECB 모드를 제외한 다른 모드에 필요한 초기벡터 바이트 배열 데이터를 입력
	 * @param keyBytes 비밀키 암호화를 위한 키 바이트 배열 데이터를 입력 
	 * @param providerStr 암호모듈 제공 프로바이더명 문자열 입력
	 * @throws NullPointerException 파라미터 입력이 Null 인 경우 오류 발생
	 * @throws Exception 기타 오류
	 */
	public SymmetricCoreAPI(String CryptonameModePadtype, byte[] ivBytes, byte[] keyBytes, String providerStr)
		   throws NullPointerException, Exception
	{
		try {
		
			StringTokenizer st = new StringTokenizer(CryptonameModePadtype, "/"); // String Tokenizer("/")
			atCryptoAlgName = st.nextToken();
			atModeType 		= st.nextToken();
			/*
			if(atCryptoAlgName.equals("DESede")) 
			{
				SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(atCryptoAlgName);
				DESedeKeySpec des3KeySpec = new DESedeKeySpec(keyBytes);
				des3Key = keyFactory.generateSecret(des3KeySpec);
				
			} */
			
			// CryptoAlgName / ModeType / PaddingType (BC JCE, SunJCE, etc.)
			cipher = Cipher.getInstance(CryptonameModePadtype, providerStr); 
			
			int blockSize = cipher.getBlockSize();
			int keySize = keyBytes.length;
			
			// System.out.println("[*] --- " + atCryptoAlgName + " BlockSize : " + blockSize);
			// System.out.println("[*] --- keySize : "+ keySize);
			
			if( (blockSize != keySize) && ((blockSize + 8) != keySize) && ((blockSize + 16) != keySize) ) {
					throw new Exception("Invalid Key Size Error -> Using 128/192/256bit");
			}
			
			keySpec = new SecretKeySpec(keyBytes, atCryptoAlgName);
			
			if (!(atModeType.equals("ECB"))) 	ivSpec = new IvParameterSpec(ivBytes);
			//System.out.println("[*] --- Provider Name : " + providerStr + "-JCE");
			//System.out.println("[*] --- Crypto Alg/Mode/PaddingType/KeySize(Bytes) : " + CryptonameModePadtype + "/" + keyBytes.length);
			
			//Arrays.fill(keyBytes, (byte)0xFF); // Encrypt & Decrypt Key Clear in Memory
			//System.out.println("[*] --- Crypto Key Clear(Bytes) : " + Hex.toHexString(keyBytes));
	
		} catch(Exception e) {
		    //e.printStackTrace();
			System.out.println("[*] --- Error Message : " + e.getMessage()+ ".");
		}
	}


	/**
	 * 입력받은 데이터를 비밀키암호알고리즘을 이용하여 암호화를 수행하고,결과로 생성된 암호화된 바이트 데이터를 반환한다.
	 * @param data 암호화 대상 바이트 배열 데이터를 입력 
	 * @return byte[]- 암호화 결과 바이트 배열 반환
	 */
	synchronized public static byte[] SymmetricEncryptData(byte[] data)
    {
		byte[] buffer = null;
		
    	try 
    	{
    		/*
			StringTokenizer st = new StringTokenizer(CryptonameModePadtype, "/"); // 읽은 문자열을 토큰화하기(구분자:$)
			String atCryptoAlgName 	= st.nextToken();
			String atModeType 		= st.nextToken();

			if (atCryptoAlgName.equals("DESede"))
			{ 

				if(atModeType.equals("ECB"))
					cipher.init(Cipher.ENCRYPT_MODE, des3Key);
				else
			 		cipher.init(Cipher.ENCRYPT_MODE, des3Key, ivSpec);
			 
			} */
			
			if(atModeType.equals("ECB")) cipher.init(Cipher.ENCRYPT_MODE, keySpec);
				
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
						
			int outputLength = cipher.getOutputSize(data.length);
			buffer = new byte[outputLength];
			buffer = cipher.doFinal(data);
			
       	} catch(Exception e) { 
         	    e.printStackTrace();
        }
    	
      	return buffer;   // Return of Byte Array Result
    }
	

	/**
	 * 입력받은 데이터를 비밀키암호알고리즘을 이용하여 복호화를 수행하고,결과로 생성된 복호화된 바이트 데이터를 반환한다.
	 * @param data 복호화 대상 바이트 배열 데이터를 입력 
	 * @return byte[]- 복호화 결과 바이트 배열 반환
	 */
	synchronized public byte[] SymmetricDecryptData(byte[] data)
    {
	
		byte[] buffer = null;
		
    	try {
    		
    		/*
			StringTokenizer st = new StringTokenizer(CryptonameModePadtype, "/"); // 읽은 문자열을 토큰화하기(구분자:$)
			String atCryptoAlgName 	= st.nextToken();
			String atModeType 		= st.nextToken();

			if (atCryptoAlgName.equals("DESede"))
			{ 
				
				if(atModeType.equals("ECB"))
					cipher.init(Cipher.DECRYPT_MODE, des3Key);
				else
					cipher.init(Cipher.DECRYPT_MODE, des3Key, ivSpec);
				
			} */
			
			if(atModeType.equals("ECB")) cipher.init(Cipher.DECRYPT_MODE, keySpec);
				
			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
			
			int outputLength = cipher.getOutputSize(data.length);
			buffer = new byte[outputLength];
			buffer = cipher.doFinal(data);
        	
		}   catch(Exception e) { 
			 e.printStackTrace();
		}

      	return buffer;   // Return of Byte Array Result 

    }
	
	synchronized public static byte[] SymmetricCipherSuiteEncryptData(String cryptoCipherSuite, byte[] encKeyBytes, byte[] ivBytesbyte, byte[] data,  String providerStr)
    {
		
		Cipher cipherSuite = null;
		IvParameterSpec ivSpecSuite = null;

		byte[] buffer = null;

		
    	try 
    	{
    		StringTokenizer st = new StringTokenizer(cryptoCipherSuite, "/"); 
			String atCryptoAlgName	= st.nextToken();
			String atModeType		= st.nextToken();
			
    		cipherSuite = Cipher.getInstance(cryptoCipherSuite, providerStr); 
			
			int blockSize = cipherSuite.getBlockSize();
			int keySize = encKeyBytes.length;
			
			if( (blockSize != keySize) && ((blockSize + 8) != keySize) && ((blockSize + 16) != keySize) ) {
					throw new Exception("Invalid Key Size Error -> Using 128/192/256bit");
			}
			
			SecretKeySpec keySpecSuite = new SecretKeySpec(encKeyBytes, atCryptoAlgName);
			
			if (!(atModeType.equals("ECB"))) ivSpecSuite = new IvParameterSpec(ivBytesbyte);
			
			
			if(atModeType.equals("ECB")) cipherSuite.init(cipherSuite.ENCRYPT_MODE, keySpecSuite);
				
			cipherSuite.init(cipherSuite.ENCRYPT_MODE, keySpecSuite, ivSpecSuite);
						
			int outputLength = cipherSuite.getOutputSize(data.length);
			buffer = new byte[outputLength];
			buffer = cipherSuite.doFinal(data);
			
       	} catch(Exception e) { 
         	    e.printStackTrace();
        }
    	
      	return buffer;   // Return of Byte Array Result
    }

	synchronized public static byte[] SymmetricCipherSuiteDecryptData(String cryptoCipherSuite, byte[] decKeyBytes, byte[] ivBytesbyte, byte[] data,  String providerStr)
    {
    	
		Cipher cipherSuite = null;
    	IvParameterSpec ivSpecSuite = null;

    	byte[] buffer = null;
    		
       	try 
       	{
       		StringTokenizer st = new StringTokenizer(cryptoCipherSuite, "/"); 
   			String atCryptoAlgName	= st.nextToken();
   			String atModeType		= st.nextToken();
    			
       		cipherSuite = Cipher.getInstance(cryptoCipherSuite, providerStr); 
    			
   			int blockSize = cipherSuite.getBlockSize();
   			int keySize = decKeyBytes.length;
    			
   			if( (blockSize != keySize) && ((blockSize + 8) != keySize) && ((blockSize + 16) != keySize) ) {
  					throw new Exception("Invalid Key Size Error -> Using 128/192/256bit");
   			}
    			
   			SecretKeySpec keySpecSuite = new SecretKeySpec(decKeyBytes, atCryptoAlgName);
    			
   			if (!(atModeType.equals("ECB"))) ivSpecSuite = new IvParameterSpec(ivBytesbyte);
    			
    		if(atModeType.equals("ECB")) cipherSuite.init(cipherSuite.DECRYPT_MODE, keySpecSuite);
    				
    		cipherSuite.init(cipherSuite.DECRYPT_MODE, keySpecSuite, ivSpecSuite);
    						
    		int outputLength = cipherSuite.getOutputSize(data.length);
    		buffer = new byte[outputLength];
    		buffer = cipherSuite.doFinal(data);
			
       	} catch(Exception e) { 
         	    e.printStackTrace();
        }
    	
      	return buffer;   // Return of Byte Array Result
    }
   

}
/**************************************************************
 *                 End of "AESCoreAPI.java"
 **************************************************************/