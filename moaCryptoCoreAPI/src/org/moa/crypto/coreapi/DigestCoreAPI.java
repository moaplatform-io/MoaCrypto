package org.moa.crypto.coreapi;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
//import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * [*] ��� ���� : ������ Ȥ�� ������ ���� ���Ἲ ���� ���� �� �����ϴ� �ٽ� API�� �����Ѵ�. <br>
 * [*] ���ϸ� : DigestCoreAPI.java <br>
 * [*] ���� �ۼ��� : 2018.10.15 <br>
 * [*] �ֱ� ������ : 2018.11.09 <br>
 * @author Bob <br> 
 **/
public class DigestCoreAPI 
{ 
	
	/**
	 * �Էµ����͸� �ؽþ˰��� ũ��(bit)�� ����Ͽ� ���Ἲ ������ �����ϴ� �޼ҵ��̴�.
	 * @param algorithmName �ؽ� �˰����(SHA256/SHA384/SHA512 or SHA3-256/SHA3-384/SHA3-512)�� ���ڿ� �Է�
	 * @param dataBytes �ؽ� �˰��� ���� ��� ����Ʈ ������ �Է�
	 * @param providerStr �ؽþ˰��� ���� ���ι��̴��� ���ڿ� �Է�
	 * @return byte[] - �Էµ����Ϳ� ���� ���Ἲ ���� ����Ʈ �迭 �����͸� ��ȯ
	 * @throws NoSuchProviderException ��ȣ��� �����ϰ� �ִ� ���ι��̴�(JCE)�� ���� ��� ���� �߻�
	 **/
	synchronized public static byte[] HashDigest(String algorithmName, byte[] dataBytes,  String providerStr) 
			throws NoSuchProviderException
	{
		
		byte[] theHashDigest = new byte[1];
		
		try{
			
			MessageDigest hash = MessageDigest.getInstance(algorithmName, providerStr);
			hash.update(dataBytes);
			theHashDigest = hash.digest();
		
		} catch(NoSuchAlgorithmException e){
			throw new RuntimeException(algorithmName + " Algorithm Not Found", e);
		}
	
		return theHashDigest; 
	}

	/**
	 * �Էµ����Ϳ� Ű�� Hmac �ؽþ˰��� ũ��(bit)�� ����Ͽ� ���Ἲ ������ �����ϴ� �޼ҵ��Դϴ�.
	 * @param algorithmName �ؽ� �˰����(HamcSHA256/384/512 or HmacSHA3-256/384/512)�� ���ڿ� �Է�
	 * @param dataBytes Hamc �ؽ� �˰��� ���� ��� ����Ʈ �迭 ������ �Է�
	 * @param hmacKeyBaytes Hamc �ؽ� �˰��� ������ ����Ʈ Ű �Է� 
	 * @param providerStr �ؽþ˰��� ���� ���ι��̴��� ���ڿ� �Է�
	 * @return b[] - �Է� ����Ʈ �����Ϳ� ���� ���Ἲ ���� ����Ʈ �迭 �����͸� ��ȯ
	 * @throws NoSuchProviderException ��ȣ��� �����ϰ� �ִ� ���ι��̴�(JCE)�� ���� ��� ���� �߻� 
	 * @throws InvalidKeyException : Ű ����� Ư�� ������� �۰ų� Ȥ�� ū ��� ���� �߻�
	 */
	synchronized public static byte[] HmacDigest(String algorithmName, byte[] dataBytes, byte[] hmacKeyBaytes, String providerStr)
			throws NoSuchProviderException, InvalidKeyException
	{
		byte[] theHmacDigest=new byte[1];
		
		try{
			
			// Minimize Key Size - 32 or 64byte
			SecretKeySpec hmacKey = new SecretKeySpec(hmacKeyBaytes, algorithmName); 
			  
			Mac hmac = Mac.getInstance(algorithmName, providerStr);
			hmac.init(hmacKey);
			hmac.update(dataBytes);
			theHmacDigest = hmac.doFinal();
			
		} catch(NoSuchAlgorithmException e){
			throw new RuntimeException(algorithmName + " Algorithm Not Found", e);
		}
		
		return theHmacDigest;
	}
	
}

