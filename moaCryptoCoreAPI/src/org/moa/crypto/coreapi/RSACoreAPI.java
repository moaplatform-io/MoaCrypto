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
 * [*] ��� ���� : RSA ����Ű�� ����, ���� �� ����/������ �����ϴ� �ٽ� API�� �����Ѵ�. <br>
 * [*] ���ϸ� : RSACoreAPI.java <br>
 * [*] ���� �ۼ��� : 2018.11.08 <br>
 * [*] �ֱ� ������ : 2018.11.09 <br>
 * @author Bob <br>  */
public class RSACoreAPI {
	
	static Cipher cipher;
	
	static PublicKey publicKey = null;
	static PrivateKey privateKey = null;

	/**
	 * 2018�� ���� ������ ũ��(2048/3072/4096)�� ����Ű ���� �����Ѵ�.
	 * @param rsaKeySize ������ RSA ����Ű���� ũ��(bit) ���� �Է�
	 * @return KeyPair ������ RSA ����Ű��(publicKey, privateKey)�� ������ ��ȯ
	 * @throws Exception Ű ���� ũ�� �Է� ����
	 */
	synchronized public static KeyPair RSAKeyGen(int rsaKeySize) 
			throws Exception 
	{
		
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
		generator.initialize(rsaKeySize);
		
		return generator.generateKeyPair();
	}
	
	/**
	 * 2018�� ���� ������ ũ��(2048/3072/4096)�� ����Ű ���� �����Ͽ� ���Ϸ� �����Ѵ�.
	 * @param rsaKeySize ������ RSA ����Ű���� ũ��(bit) ���� �Է�
	 * @param publicKeyFileName ������ ����Ű ���ϸ� ���ڿ� �Է�
	 * @param privateKeyFileName ������ ����Ű ���ϸ� ���ڿ� �Է�
	 * @throws Exception Ű ���� ũ�� �Է� ����
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
		if (publicKeyFile.exists() && privateKeyFile.exists()) {// ���Ͽ��� Ű �о����

			/*
			Path publicFile = Paths.get("publicRSA.key", null);
			byte[] publicKeyBytes = Files.readAllBytes(publicFile);
			System.out.println("RSAKeyGen Test 2--------------------");
			
			Path privateFile = Paths.get("privateRSA.key", null);
			byte[] privateKeyBytes = Files.readAllBytes(privateFile);

			KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
			publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
			privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
			
			System.out.println("���� ����Ű: " + Hex.toHexString(publicKeyBytes));
			System.out.println("���� ����Ű: " + Hex.toHexString(privateKeyBytes));
			*/

		} else {// ����Ű�� ����

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
	 * RSA OAEP ����Ű ��� �Է� ����Ʈ �����͸� ��ȣȭ�� ����Ʈ�� �����Ѵ�.
	 * @param publicKey PublicKey Type�� ����Ű�� �Է�
	 * @param plainData ��ȣȭ ��� ����Ʈ ����Ÿ�� �Է�
	 * @return byte[] - ��ȣȭ ��� ����Ʈ ��ȯ
	 * @throws NoSuchAlgorithmException ���ι��̴����� �������� �˰��� ���� 
	 * @throws GeneralSecurityException RSA ��� ��ȣȭ ����
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
	 * RSA OAEP ����Ű ��� �Է� ����Ʈ �����͸� ��ȣȭ�� ����Ʈ�� �����Ѵ�.
	 * @param privateKey PrivateKey Type�� ����Ű�� �Է�
	 * @param encryptData ��ȣȭ ��� ����Ʈ ��ȣȭ �����͸� �Է�
	 * @return byte[] - ��ȣȭ ��� ����Ʈ ��ȯ
	 * @throws GeneralSecurityException RSA ��ȣȭ  ����
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
	 * RSA ����Ű ��� ����� ����Ʈ �����͸� �����Ѵ�.
	 * @param privateKey PrivateKey Type�� ����� ����Ű �Է�
	 * @param plainData ������ ��� ����Ʈ ������ �Է�
	 * @return byte[] - RSA ����Ű�� ���ڼ����� ����Ʈ ������ ��ȯ 
	 * @throws NoSuchAlgorithmException ���ι��̴����� �������� �˰��� ���� 
	 * @throws GeneralSecurityException ���ڼ��� ����
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
	 * RSA ����Ű�� ����� �����͸� ����Ű�� �����Ѵ�. 
	 * @param publicKey ���� ���� ������ ����Ű�� �Է�
	 * @param signatureData ����� ����Ʈ �迭 ������ �Է�
	 * @param plainData ������ ���� ���� ��� ����Ʈ �迭 ������ �Է�
	 * @return boolean - ���ڼ��� ���� ��������� ��ȯ(���� : true / ����: false)
	 * @throws NoSuchAlgorithmException ���ι��̴����� �������� �˰��� ���� 
	 * @throws GeneralSecurityException ���ڼ��� ���� ���� 
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