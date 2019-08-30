package org.moa.crypto.coreapi;

import java.io.File;
import java.io.FileOutputStream;
//import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;

/**
 * [*] 기능 정의 : ECDSA 공개키쌍 생성, 저장 및 서명/검증을 수행하는 핵심 API를 제공한다. <br>
 * [*] 파일명 : ECDSACoreAPI.java <br>
 * [*] 최초 작성일 : 2018.10.15 <br>
 * [*] 최근 수정일 : 2018.11.09 <br>
 * @author Bob <br>  */
public class ECDSACoreAPI {
	
	private static PrivateKey privateKey = null;
	private static PublicKey publicKey = null;
	
	private static String  ecdsaCurveInfo = null;
	private static String ecdsaSignAlgothmCuite = null;
	
	/**
	 * ECDSA 서명 및 검증을 위한 키(ECC Curve) 정보와 서명/검증 알고리즘 집합을 입력하여 생성하는 ECDSACoreAPI 생성자이다.
	 * @param ecdsaCurveInfoParam 타원곡선 Curve 문자열(secpxxxr1 = "secp224[/256/384/512]r1") 입력 
	 * @param ecdsaSignAlgothmCuiteParam ECDSA 서명 및 검증을 위한 알고리즘 집합 문자열
	 *        (SHAxxxwithECDSA or SHA3-xxxwithECDSA = "SHA256[/384/512]withECDSA" or "SHA3-256[/384/512]withECDSA") 입력  
	 */
	public ECDSACoreAPI(String ecdsaCurveInfoParam, String ecdsaSignAlgothmCuiteParam) {
		
		ECDSACoreAPI.ecdsaCurveInfo = ecdsaCurveInfoParam;
		ECDSACoreAPI.ecdsaSignAlgothmCuite = ecdsaSignAlgothmCuiteParam;
	}
	
	/**
	 * 2018년 현재 안전한 EC Curve(secp224r1/secp256r1/secp384r1/secp512r1)의 ECDSA 공개키 쌍을 생성한다.
	 * @return KeyPair - 생성한 RSA 공개키쌍(publicKey, privateKey)의 생성하여 반환
	 * @throws Exception 키 생성 크기 입력 오류
	 */
	synchronized public static KeyPair ECDSAKeyGen()
			throws Exception 
	{
		
		KeyPairGenerator generator = KeyPairGenerator.getInstance("ECDSA", "BC");
		ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(ecdsaCurveInfo);
		generator.initialize(ecGenParameterSpec, new SecureRandom());

		return generator.generateKeyPair();
		
	}
	
	/**
	 * 2018년 현재 안전한 EC Curve(secp224[/256/384/512]r1)의 ECDSA 공개키 쌍을 생성하여 파일로 저장한다.
	 * @param publicKeyFileName 저장할 공개키 파일명 문자열 입력
	 * @param privateKeyFileName 저장할 개인키 파일명 문자열 입력
	 * @throws Exception 키 생성 크기 입력 오류
	 */
	public static void ECDSAKeyGenFile(String publicKeyFileName, String privateKeyFileName) 
			   throws Exception 
	{

		KeyPairGenerator generator = KeyPairGenerator.getInstance("ECDSA", "BC");
		ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(ecdsaCurveInfo);
		generator.initialize(ecGenParameterSpec, new SecureRandom());

		KeyPair pair = generator.generateKeyPair();
		publicKey = pair.getPublic();
		privateKey = pair.getPrivate();
		
		FileOutputStream outputPublic = new FileOutputStream(new File(publicKeyFileName));
		outputPublic.write(publicKey.getEncoded()); 

		FileOutputStream outputPrivate = new FileOutputStream(new File(privateKeyFileName));
		outputPrivate.write(privateKey.getEncoded());
		
		outputPublic.close();
		outputPrivate.close();
			
	}

	/**
	 * ECDSA 개인키 기반 서명된 바이트 데이터를 생성한다.
	 * @param privateKey PrivateKey Type의 서명용 개인키 입력
	 * @param plainData 서명할 대상 바이트 배열 데이터 입력
	 * @return byte[] - ECDSA 개인키로 전자서명한 바이트 배열 데이터 반환 
	 * @throws GeneralSecurityException 전자서명 오류
	 */
	public static byte[] ECDSASign(PrivateKey privateKey, byte[] plainData) 
			   throws GeneralSecurityException 
	{

		//Signature signature = Signature.getInstance("SHA3-256withECDSA", "BC");
		Signature signature = Signature.getInstance(ecdsaSignAlgothmCuite, "BC");
		signature.initSign(privateKey);
		signature.update(plainData);
		byte[] signatureData = signature.sign();
			
		return signatureData;

	}

	/**
	 * ECDSA 개인키로 서명된 데이터를 공개키로 검증한다. 
	 * @param publicKey 서명에 대한 검증할 공개키를 입력
	 * @param signatureData 서명된 바이트 데이터 입력
	 * @param plainData 검증을 위한 서명 대상 바이트 데이터 입력
	 * @return boolean - 전자서명에 대한 검증결과를 반환(성공 : true / 실패: false)
	 * @throws GeneralSecurityException 전자서명 검증 오류 
	 */
	public static boolean ECDSAVerify(PublicKey publicKey, byte[] signatureData, byte[] plainData)
				throws GeneralSecurityException 
	{
		//Signature signature = Signature.getInstance("SHA3-256withECDSA", "BC");
		Signature signature = Signature.getInstance(ecdsaSignAlgothmCuite, "BC");
		signature.initVerify(publicKey);
		signature.update(plainData);
		
		return signature.verify(signatureData);
	
	}
	
}
	