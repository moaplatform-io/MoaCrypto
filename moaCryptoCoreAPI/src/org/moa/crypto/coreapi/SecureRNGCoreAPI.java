package org.moa.crypto.coreapi;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;

import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.prng.SP800SecureRandomBuilder;

/**
 * [*] ��� ���� : hmacDRBG ��� ������ �����ϴ� �ٽ� API�� �����Ѵ�. <br>
 * [*] ���ϸ� : SecureRNGCoreAPI.java <br>
 * [*] ���� �ۼ��� : 2018.10.22 <br>
 * [*] �ֱ� ������ : 2018.11.08 <br>
 * @author Bob <br>  */
public class SecureRNGCoreAPI {   

	/**
	 * �Է��� ���ڿ� �ش��ϴ� ũ��(Bytes)�� ������ ������ �����Ѵ�.
	 * @param genNumber ������ ������ ũ��(����) �Է�
	 * @return byte[] - ������ ����Ʈ ������ ��ȯ
	 * @throws NoSuchAlgorithmException ���ι��̴����� �����ϴ� ��ȣ�˰��� ����
	 * @throws NoSuchProviderException �������� �ʴ� ���ι��̴� ����
	 * @throws NullPointerException ����Ʈ �迭�� Null �Է� ����
	 */
	synchronized public static byte[] SecureRandNumberGen(int genNumber)
		throws NoSuchAlgorithmException, NoSuchProviderException, NullPointerException
	{
		
    	Security.setProperty("securerandom.drbg.config", "Hash_DRBG");
   		byte[] nonce = new byte[10]; 
   		SecureRandom sr = SecureRandom.getInstanceStrong();
   		sr.nextBytes(nonce);
   		
   		SP800SecureRandomBuilder spSecureRandom = new SP800SecureRandomBuilder();
    	//SecureRandom hashDRBG = sp.buildHash(new SHA512Digest(), nonce, true);
    	SecureRandom hmacDRBG = spSecureRandom.buildHMAC(new HMac(new SHA512Digest()), nonce, false); 
   		byte[] buffer = new byte[genNumber];
   		hmacDRBG.nextBytes(buffer);
   
		return buffer;
		
	}
	
}




