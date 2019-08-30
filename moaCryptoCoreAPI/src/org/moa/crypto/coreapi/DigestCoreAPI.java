package org.moa.crypto.coreapi;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
//import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * [*] 기능 정의 : 데이터 혹은 정보에 대한 무결성 값을 생성 및 검증하는 핵심 API를 제공한다. <br>
 * [*] 파일명 : DigestCoreAPI.java <br>
 * [*] 최초 작성일 : 2018.10.15 <br>
 * [*] 최근 수정일 : 2018.11.09 <br>
 * @author Bob <br> 
 **/
public class DigestCoreAPI 
{ 
	
	/**
	 * 입력데이터를 해시알고리즘 크기(bit)로 축약하여 무결성 정보를 생성하는 메소드이다.
	 * @param algorithmName 해시 알고리즘명(SHA256/SHA384/SHA512 or SHA3-256/SHA3-384/SHA3-512)을 문자열 입력
	 * @param dataBytes 해시 알고리즘 적용 대상 바이트 데이터 입력
	 * @param providerStr 해시알고리즘 제공 프로바이더명 문자열 입력
	 * @return byte[] - 입력데이터에 대한 무결성 생성 바이트 배열 데이터를 반환
	 * @throws NoSuchProviderException 암호모듈 소유하고 있는 프로바이더(JCE)가 없는 경우 에러 발생
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
	 * 입력데이터와 키를 Hmac 해시알고리즘 크기(bit)로 축약하여 무결성 정보를 생성하는 메소드입니다.
	 * @param algorithmName 해시 알고리즘명(HamcSHA256/384/512 or HmacSHA3-256/384/512)을 문자열 입력
	 * @param dataBytes Hamc 해시 알고리즘 적용 대상 바이트 배열 데이터 입력
	 * @param hmacKeyBaytes Hamc 해시 알고리즘에 적용할 바이트 키 입력 
	 * @param providerStr 해시알고리즘 제공 프로바이더명 문자열 입력
	 * @return b[] - 입력 바이트 데이터에 대한 무결성 생성 바이트 배열 데이터를 반환
	 * @throws NoSuchProviderException 암호모듈 소유하고 있는 프로바이더(JCE)가 없는 경우 에러 발생 
	 * @throws InvalidKeyException : 키 사이즈가 특정 사이즈보다 작거나 혹은 큰 경우 에러 발생
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

