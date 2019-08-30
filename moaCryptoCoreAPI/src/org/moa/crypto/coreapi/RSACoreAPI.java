package org.moa.crypto.coreapi;

import java.io.File;
//import java.io.FileOutputStream;
import java.io.FileWriter;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.util.encoders.*;


/**
 * [*] 기능 정의 : RSA 공개키쌍 생성, 저장 및 서명/검증을 수행하는 핵심 API를 제공한다. <br>
 * [*] 파일명 : RSACoreAPI.java <br>
 * [*] 최초 작성일 : 2018.11.08 <br>
 * [*] 최근 수정일 : 2018.11.09 <br>
 * @author Bob <br>  */
public class RSACoreAPI {
	
	static Cipher cipher;
	
	static PublicKey publicKey = null;
	static PrivateKey privateKey = null;

	/**
	 * 2018년 현재 안전한 크기(2048/3072/4096)의 공개키 쌍을 생성한다.
	 * @param rsaKeySize 생성할 RSA 공개키쌍의 크기(bit) 숫자 입력
	 * @return KeyPair 생성한 RSA 공개키쌍(publicKey, privateKey)의 생성자 반환
	 * @throws Exception 키 생성 크기 입력 오류
	 */
	synchronized public static KeyPair RSAKeyGen(int rsaKeySize) 
			throws Exception 
	{
		
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
		generator.initialize(rsaKeySize);
		
		return generator.generateKeyPair();
	}
	
	/**
	 * 2018년 현재 안전한 크기(2048/3072/4096)의 공개키 쌍을 생성하여 파일로 저장한다.
	 * @param rsaKeySize 생성할 RSA 공개키쌍의 크기(bit) 숫자 입력
	 * @param publicKeyFileName 저장할 공개키 파일명 문자열 입력
	 * @param privateKeyFileName 저장할 개인키 파일명 문자열 입력
	 * @throws Exception 키 생성 크기 입력 오류
	 */
	synchronized public static void RSAKeyGenFile(int rsaKeySize, String publicKeyFileName, String privateKeyFileName) 
		   throws Exception 
	{
		//File publicKeyFile = new File("publicRSA.key");
		//File privateKeyFile = new File("privateRSA.key");
		File publicKeyFile = new File(publicKeyFileName);
		File privateKeyFile = new File(privateKeyFileName);
				
		//FileOutputStream outputPublic = null;
		//FileOutputStream outputPrivate = null;

		FileWriter publicKeyFileFW = null;
		FileWriter privateKeyFileFW = null;
		
		//System.out.println("RSAKeyGen Test 1 --------------------");
		if (publicKeyFile.exists() && privateKeyFile.exists()) {// 파일에서 키 읽어오기

			/*
			Path publicFile = Paths.get("publicRSA.key", null);
			byte[] publicKeyBytes = Files.readAllBytes(publicFile);
			System.out.println("RSAKeyGen Test 2--------------------");
			
			Path privateFile = Paths.get("privateRSA.key", null);
			byte[] privateKeyBytes = Files.readAllBytes(privateFile);

			KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
			publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
			privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
			
			System.out.println("파일 공개키: " + Hex.toHexString(publicKeyBytes));
			System.out.println("파일 개인키: " + Hex.toHexString(privateKeyBytes));
			*/

		} else {// 공개키쌍 생성

			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
			generator.initialize(rsaKeySize); // 2048 or 3072
			KeyPair pair = generator.generateKeyPair();
			publicKey = pair.getPublic();
			privateKey = pair.getPrivate();
/*
			outputPublic = new FileOutputStream(new File(publicKeyFileName));
			outputPublic.write(publicKey.getEncoded()); 

			outputPrivate = new FileOutputStream(new File(privateKeyFileName));
			outputPrivate.write(privateKey.getEncoded());
*/			
			
			publicKeyFileFW = new FileWriter(publicKeyFile, false);
			publicKeyFileFW.write(Base64.toBase64String(publicKey.getEncoded()));
			publicKeyFileFW.flush();
			
			privateKeyFileFW = new FileWriter(privateKeyFile, false);
			privateKeyFileFW.write(Base64.toBase64String(privateKey.getEncoded()));
			privateKeyFileFW.flush();
			
		}
		
	}
	
	/**
	 * RSA OAEP 공개키 기반 입력 바이트 데이터를 암호화한 바이트를 생성한다.
	 * @param publicKey PublicKey Type의 공개키를 입력
	 * @param plainData 암호화 대상 바이트 데이타를 입력
	 * @return byte[] - 암호화 결과 바이트 반환
	 * @throws NoSuchAlgorithmException 프로바이더에서 제공하지 알고리즘 오류 
	 * @throws GeneralSecurityException RSA 기반 암호화 오류
	 */
	synchronized public static byte[] rsaOAEPEncrypt(PublicKey publicKey, byte[] plainData)
		throws NoSuchAlgorithmException, GeneralSecurityException
	{
				
		//System.out.println("RSAKeyGen Test 5--------------------");
		cipher = Cipher.getInstance("RSA/ECB/OAEPwithSHA512andMGF1Padding", "BC");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] encryptData = cipher.doFinal(plainData);
		
		return encryptData;
	}
	
	/**
	 * RSA OAEP 개인키 기반 입력 바이트 데이터를 암호화한 바이트를 생성한다.
	 * @param privateKey PrivateKey Type의 개인키를 입력
	 * @param encryptData 복호화 대상 바이트 암호화 데이터를 입력
	 * @return byte[] - 복호화 결과 바이트 반환
	 * @throws GeneralSecurityException RSA 복호화  오류
	 */
	synchronized public static byte[] rsaOAEPDecrypt(PrivateKey privateKey, byte[] encryptData)
			throws GeneralSecurityException 
	{
		cipher = Cipher.getInstance("RSA/ECB/OAEPwithSHA512andMGF1Padding", "BC");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] decryptData = cipher.doFinal(encryptData);
			
		return decryptData;
	}

	/**
	 * RSA 개인키 기반 서명된 바이트 데이터를 생성한다.
	 * @param privateKey PrivateKey Type의 서명용 개인키 입력
	 * @param plainData 서명할 대상 바이트 데이터 입력
	 * @return byte[] - RSA 개인키로 전자서명한 바이트 데이터 반환 
	 * @throws NoSuchAlgorithmException 프로바이더에서 제공하지 알고리즘 오류 
	 * @throws GeneralSecurityException 전자서명 오류
	 */
	synchronized public static byte[] rsaSign(PrivateKey privateKey, byte[] plainData) 
		throws NoSuchAlgorithmException, GeneralSecurityException
	{
	
		Signature signature = Signature.getInstance("SHA256withRSA/PSS", "BC");
		signature.initSign(privateKey);
		signature.update(plainData);
		byte[] signatureData = signature.sign();

		return signatureData;
	}

	/**
	 * RSA 개인키로 서명된 데이터를 공개키로 검증한다. 
	 * @param publicKey 서명에 대한 검증할 공개키를 입력
	 * @param signatureData 서명된 바이트 배열 데이터 입력
	 * @param plainData 검증을 위한 서명 대상 바이트 배열 데이터 입력
	 * @return boolean - 전자서명에 대한 검증결과를 반환(성공 : true / 실패: false)
	 * @throws NoSuchAlgorithmException 프로바이더에서 제공하지 알고리즘 오류 
	 * @throws GeneralSecurityException 전자서명 검증 오류 
	 */
	synchronized public static boolean rsaVerify(PublicKey publicKey, byte[] signatureData, byte[] plainData) 
		throws NoSuchAlgorithmException, GeneralSecurityException
	{
		
        Signature signature = Signature.getInstance("SHA256withRSA/PSS", "BC");
        signature.initVerify(publicKey);
        signature.update(plainData);
        
        return signature.verify(signatureData);

	} 
}

/*
private static byte [] encrypt(String plaintext) throws Exception {
	KeyStore keyStore = getKeyStore();
	Certificate[] certs = keyStore.getCertificateChain("oaep");
	Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPwithSHA1andMGF1Padding","BC");
	cipher.init(Cipher.ENCRYPT_MODE, certs[0].getPublicKey());
	return cipher.doFinal(plaintext.getBytes());
}

private static String decrypt(byte [] ciphertext) throws Exception {
	KeyStore keyStore = getKeyStore();
	PrivateKey privateKey = (PrivateKey) keyStore.getKey("oaep",
        "oaep".toCharArray());
	Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPwithSHA1andMGF1Padding","BC");
	cipher.init(Cipher.DECRYPT_MODE, privateKey);
	byte[] cipherbyte=cipher.doFinal(ciphertext);
	return new String(cipherbyte);
}
*/