package org.moa.crypto.coreapi;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;

import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.prng.SP800SecureRandomBuilder;

/**
 * [*] 기능 정의 : hmacDRBG 기반 난수를 생성하는 핵심 API를 제공한다. <br>
 * [*] 파일명 : SecureRNGCoreAPI.java <br>
 * [*] 최초 작성일 : 2018.10.22 <br>
 * [*] 최근 수정일 : 2018.11.08 <br>
 * @author Bob <br>  */
public class SecureRNGCoreAPI {   

	/**
	 * 입력한 숫자에 해당하는 크기(Bytes)의 안전한 난수를 생성한다.
	 * @param genNumber 생성할 난수의 크기(숫자) 입력
	 * @return byte[] - 생성한 바이트 난수를 반환
	 * @throws NoSuchAlgorithmException 프로바이더에서 지원하는 암호알고리증 오류
	 * @throws NoSuchProviderException 제공하지 않는 프로바이더 오류
	 * @throws NullPointerException 바이트 배열에 Null 입력 오류
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




