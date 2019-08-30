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
 * [*] ��� ���� : ECDSA ����Ű�� ����, ���� �� ����/������ �����ϴ� �ٽ� API�� �����Ѵ�. <br>
 * [*] ���ϸ� : ECDSACoreAPI.java <br>
 * [*] ���� �ۼ��� : 2018.10.15 <br>
 * [*] �ֱ� ������ : 2018.11.09 <br>
 * @author Bob <br>  */
public class ECDSACoreAPI {
	
	private static PrivateKey privateKey = null;
	private static PublicKey publicKey = null;
	
	private static String  ecdsaCurveInfo = null;
	private static String ecdsaSignAlgothmCuite = null;
	
	/**
	 * ECDSA ���� �� ������ ���� Ű(ECC Curve) ������ ����/���� �˰��� ������ �Է��Ͽ� �����ϴ� ECDSACoreAPI �������̴�.
	 * @param ecdsaCurveInfoParam Ÿ��� Curve ���ڿ�(secpxxxr1 = "secp224[/256/384/512]r1") �Է� 
	 * @param ecdsaSignAlgothmCuiteParam ECDSA ���� �� ������ ���� �˰��� ���� ���ڿ�
	 *        (SHAxxxwithECDSA or SHA3-xxxwithECDSA = "SHA256[/384/512]withECDSA" or "SHA3-256[/384/512]withECDSA") �Է�  
	 */
	public ECDSACoreAPI(String ecdsaCurveInfoParam, String ecdsaSignAlgothmCuiteParam) {
		
		ECDSACoreAPI.ecdsaCurveInfo = ecdsaCurveInfoParam;
		ECDSACoreAPI.ecdsaSignAlgothmCuite = ecdsaSignAlgothmCuiteParam;
	}
	
	/**
	 * 2018�� ���� ������ EC Curve(secp224r1/secp256r1/secp384r1/secp512r1)�� ECDSA ����Ű ���� �����Ѵ�.
	 * @return KeyPair - ������ RSA ����Ű��(publicKey, privateKey)�� �����Ͽ� ��ȯ
	 * @throws Exception Ű ���� ũ�� �Է� ����
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
	 * 2018�� ���� ������ EC Curve(secp224[/256/384/512]r1)�� ECDSA ����Ű ���� �����Ͽ� ���Ϸ� �����Ѵ�.
	 * @param publicKeyFileName ������ ����Ű ���ϸ� ���ڿ� �Է�
	 * @param privateKeyFileName ������ ����Ű ���ϸ� ���ڿ� �Է�
	 * @throws Exception Ű ���� ũ�� �Է� ����
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
	 * ECDSA ����Ű ��� ����� ����Ʈ �����͸� �����Ѵ�.
	 * @param privateKey PrivateKey Type�� ����� ����Ű �Է�
	 * @param plainData ������ ��� ����Ʈ �迭 ������ �Է�
	 * @return byte[] - ECDSA ����Ű�� ���ڼ����� ����Ʈ �迭 ������ ��ȯ 
	 * @throws GeneralSecurityException ���ڼ��� ����
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
	 * ECDSA ����Ű�� ����� �����͸� ����Ű�� �����Ѵ�. 
	 * @param publicKey ���� ���� ������ ����Ű�� �Է�
	 * @param signatureData ����� ����Ʈ ������ �Է�
	 * @param plainData ������ ���� ���� ��� ����Ʈ ������ �Է�
	 * @return boolean - ���ڼ��� ���� ��������� ��ȯ(���� : true / ����: false)
	 * @throws GeneralSecurityException ���ڼ��� ���� ���� 
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
	