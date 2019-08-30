package org.moa.crypto.coreapi;

//import java.security.InvalidKeyException;
import java.util.*;

//---BC & SunJCE
//import javax.crypto.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import javax.crypto.spec.IvParameterSpec;

/**
 * [*] ��� ���� : ���Ű��ȣ ��� ��ȣȭ �� ��ȣȭ�� �����ϴ� �ٽ� API�� �����Ѵ�. <br>
 * [*] ���ϸ� : SymmetricCoreAPI.java <br>
 * [*] ���� �ۼ��� : 2018.10.15 <br>
 * [*] �ֱ� ������ : 2018.11.08 <br>
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
	 * ���Ű ��ȣȭ(AES, SEED, ..., etc.)�� ���� ���� �������̴�.
	 * @param CryptonameModePadtype ���Ű ��ȣȭ�� ���� ���ڿ� ����(ex: AES/CBC/PKCS7Padding, AES/CTR/NoPadding, ..., etc.) �Է�
	 * @param ivBytes ���Ű ��ȣ����� ECB ��带 ������ �ٸ� ��忡 �ʿ��� �ʱ⺤�� ����Ʈ �迭 �����͸� �Է�
	 * @param keyBytes ���Ű ��ȣȭ�� ���� Ű ����Ʈ �迭 �����͸� �Է� 
	 * @param providerStr ��ȣ��� ���� ���ι��̴��� ���ڿ� �Է�
	 * @throws NullPointerException �Ķ���� �Է��� Null �� ��� ���� �߻�
	 * @throws Exception ��Ÿ ����
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
	 * �Է¹��� �����͸� ���Ű��ȣ�˰����� �̿��Ͽ� ��ȣȭ�� �����ϰ�,����� ������ ��ȣȭ�� ����Ʈ �����͸� ��ȯ�Ѵ�.
	 * @param data ��ȣȭ ��� ����Ʈ �迭 �����͸� �Է� 
	 * @return byte[]- ��ȣȭ ��� ����Ʈ �迭 ��ȯ
	 */
	synchronized public static byte[] SymmetricEncryptData(byte[] data)
    {
		byte[] buffer = null;
		
    	try 
    	{
    		/*
			StringTokenizer st = new StringTokenizer(CryptonameModePadtype, "/"); // ���� ���ڿ��� ��ūȭ�ϱ�(������:$)
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
	 * �Է¹��� �����͸� ���Ű��ȣ�˰����� �̿��Ͽ� ��ȣȭ�� �����ϰ�,����� ������ ��ȣȭ�� ����Ʈ �����͸� ��ȯ�Ѵ�.
	 * @param data ��ȣȭ ��� ����Ʈ �迭 �����͸� �Է� 
	 * @return byte[]- ��ȣȭ ��� ����Ʈ �迭 ��ȯ
	 */
	synchronized public byte[] SymmetricDecryptData(byte[] data)
    {
	
		byte[] buffer = null;
		
    	try {
    		
    		/*
			StringTokenizer st = new StringTokenizer(CryptonameModePadtype, "/"); // ���� ���ڿ��� ��ūȭ�ϱ�(������:$)
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