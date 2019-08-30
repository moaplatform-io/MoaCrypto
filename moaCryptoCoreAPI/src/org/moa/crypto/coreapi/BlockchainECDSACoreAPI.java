package org.moa.crypto.coreapi;
 
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.util.encoders.Hex;
import org.moa.crypto.coreapi.DigestCoreAPI;

/**
 * [*] 기능 정의 : BlockChain용 ECDSA 공개키쌍 생성, 저장 및 서명/검증을 수행하는 핵심 API를 제공한다. <br>
 * [*] 파일명 : BlockchainECDSACoreAPI.java <br>
 * [*] 최초 작성일 : 2018.11.01 <br>
 * [*] 최근 수정일 : 2019.04.10 <br>
 * @author Bob <br>  */
public class BlockchainECDSACoreAPI {
	
	public static String bcEcdsaCurveInfo = null;
	public static String bcEcdsaHashAlgothm = null;
	
	private static X9ECParameters p = null;
	private static ECDomainParameters ecDomainPrams = null;
	static DigestCoreAPI  dc = null;

	/**
	 * ECDSA 서명 및 검증을 위한 키(ECC Curve) 및 해시 알고리즘 정보를 입력하여 생성하는 BlockchainECDSACoreAPI 생성자이다.
	 * @param bcEcdsaCurveInfoParam 타원곡선 Curve 문자열(secpxxxk1 = "secp224[/256/384/512]k1") 입력 
	 * @param bcEcdsaHashAlgothmParam 해시 알고리즘 문자열(SHAxxx = "SHA256[/384/512]" or SHA3-xxx = "SHA3-256[/384/512]") 입력 
	 */
    public BlockchainECDSACoreAPI(String bcEcdsaCurveInfoParam, String bcEcdsaHashAlgothmParam) {
    	
    	BlockchainECDSACoreAPI.bcEcdsaCurveInfo = bcEcdsaCurveInfoParam;
    	BlockchainECDSACoreAPI.bcEcdsaHashAlgothm = bcEcdsaHashAlgothmParam;
    	p = SECNamedCurves.getByName(bcEcdsaCurveInfo);
    	ecDomainPrams = new ECDomainParameters(p.getCurve(), p.getG(), p.getN(), p.getH());
    	dc = new DigestCoreAPI();
    }

    /**
	 * BlockChain ECDSA 개인키 기반 서명된 바이트 데이터를 생성한다.
	 * @param dataBytes 해시 선 적용할 데이터 바이트 배열 데이터 입력
	 * @param privateKey 바이트 배열 유형의 서명용 개인키 입력
	 * @return byte[] - ECDSA 개인키로 전자서명한 바이트 배열 데이터 반환 
     * @throws NoSuchProviderException 제공하지 않는 프로바이더 설정 오류
	 */
    synchronized public byte[] blockchainECDSASign(byte[] dataBytes, byte[] privateKey) 
    		throws NoSuchProviderException 
    {
    	
    	// Blockchain in this case : Pre Hash Compute
    	byte[] hashedData = DigestCoreAPI.HashDigest(bcEcdsaHashAlgothm, dataBytes, "BC");
		//ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA3Digest(256)));
    	ECDSASigner signer = new ECDSASigner();
    	
		BigInteger privateKeyValue = new BigInteger(1, privateKey);
		ECPrivateKeyParameters ecPrivKeyParams = new ECPrivateKeyParameters(privateKeyValue, ecDomainPrams); // BigInteger PrivateKey
        signer.init(true, ecPrivKeyParams);
        
        BigInteger[] components = signer.generateSignature(hashedData);
        //BigInteger[] components = signer.generateSignature(hash);
        // Test Code Add
        String r = Hex.toHexString(components[0].toByteArray());
        String s = Hex.toHexString(components[1].toByteArray());
        //System.out.println("Byte Sign [0] r [Len / Byte ] : " + r.length()/2 + " / " + Hex.toHexString(components[0].toByteArray()));
        //System.out.println("Byte Sign [1] s [Len / Byte ] : " + s.length()/2 + " / " + Hex.toHexString(components[1].toByteArray()));
        //----------------------
        
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try {
			
			DERSequenceGenerator seq = new DERSequenceGenerator(baos);
			seq.addObject(new ASN1Integer(components[0]));
			seq.addObject(new ASN1Integer(components[1]));
			seq.close();
			
			//System.out.println("blockchainECDSASign = " + Hex.toHexString(baos.toByteArray()) );
			return baos.toByteArray();
			
		} catch (IOException e) {
			return new byte[0];
		}
	}

	/**
	 * BlockChain ECDSA 개인키로 서명된 데이터를 공개키로 검증한다. 
	 * @param dataBytes 해시 선 적용할 데이터 바이트 배열 데이터 입력
	 * @param signature 서명된 바이트 데이터 입력
	 * @param publicKey 검증을 위한 공개키 대상 바이트 데이터 입력
	 * @return boolean - 전자서명에 대한 검증결과를 반환(성공 : true / 실패: false)
	 */
    synchronized public boolean blockchainECDSAVerify(byte[] dataBytes, byte[] signature, byte[] publicKey) {

		ASN1InputStream asn1Decoder = new ASN1InputStream(signature);

		//System.out.println("[] Byte publicKey : " + Hex.toHexString(publicKey) + "/ Len : " + publicKey.length);
	/*	
		// publicKey Type 0x04 = Uncompressed form / 0x02 = Compressed form
		// publicKey 크기가 65Byte 보다 작을 경우와 65Byte인데 첫번째 배열이 0x00 인경우 0x04로 대체하는 루틴
		//byte[] publicKeyQ = new byte[65];
		byte[] publicKeyQ = null; 
		if ( (publicKey.length == 64) ) {
			publicKeyQ = new byte[65];
			publicKeyQ[0] = (byte) 0x04;
			System.arraycopy(publicKey, 0, publicKeyQ, 1, publicKey.length);
			
		} else if ( ((publicKey.length == 65) && (publicKey[0] == (byte) 0x00)) ) {
			publicKeyQ = new byte[65];
			publicKey[0] = (byte) 0x04;
			System.arraycopy(publicKey, 0, publicKeyQ, 0, publicKey.length);
		}
		
		if ((publicKey.length == 32)) { // Compressed form [0x02]
			publicKeyQ = new byte[33];
			publicKeyQ[0] = (byte) 0x02;
			System.arraycopy(publicKey, 0, publicKeyQ, 1, publicKey.length);
		} else if ( ((publicKey.length == 33) && (publicKey[0] == (byte) 0x00)) ) {
			publicKeyQ = new byte[33];
			publicKey[0] = (byte) 0x02;
			System.arraycopy(publicKey, 0, publicKeyQ, 0, publicKey.length);
		} */
		
		try {
			
			// Blockchain in this case : Pre Hash Compute
			byte[] hashedData = DigestCoreAPI.HashDigest(bcEcdsaHashAlgothm, dataBytes, "BC");

			ECDSASigner signer = new ECDSASigner();
			//System.out.println("[] Byte publicKey : " + Hex.toHexString(publicKey) + "/ Len : " + publicKey.length);
			//System.out.println("[] Byte publicKeyQ: " + Hex.toHexString(publicKeyQ) + "/ Len : " + publicKeyQ.length);
			//System.out.println("[] verify test 1");
			ECPublicKeyParameters ecpublicKeyParam = 
					new ECPublicKeyParameters(ecDomainPrams.getCurve().decodePoint(publicKey), ecDomainPrams); // // Byte[] PublicKey (ECC Q Value) 
			//System.out.println("[] verify test 2");

	        signer.init(false, ecpublicKeyParam);
	        
	        DLSequence seq = (DLSequence) asn1Decoder.readObject();
			BigInteger r = ((ASN1Integer) seq.getObjectAt(0)).getPositiveValue();
			BigInteger s = ((ASN1Integer) seq.getObjectAt(1)).getPositiveValue();

			//System.out.println("Bytes verify [0] r = " + Hex.toHexString(r.toByteArray()));
	        //System.out.println("Bytes verify [1] s = " + Hex.toHexString(s.toByteArray()));
			
			// Hash[byte], r[BigInteger], s[BigInteger]
			return signer.verifySignature(hashedData, r, s);
			
		} catch (Exception e) {
			return false;
		} finally {
			try {
				asn1Decoder.close();
			} catch (IOException ignored) {
			}
		}
	}
	
    /**
     * [*] 기능 정의 : BlockchainECDSACoreAPI의 Inner Class로 EC 기반 공개키 및 개인키쌍을 생성하는핵심 API를 제공한다. <br>
     * [*] 파일명 : BlockchainECDSACoreAPI.java [Inner Class] <br>
     * [*] 최초 작성일 : 2018.11.01 <br>
     * [*] 최근 수정일 : 2018.11.13 <br>
     * @author Bob <br> 
     **/
	public static class ECKeyPair
	{
		
	    private final BigInteger privateKey;
	    private final BigInteger publicKey;

	    /**
	     * ECKeyPair 생성자로 BigInteger 유형 공개키쌍을 설정한다. 
	     * @param privateKey BigInteger 유형의 개인키 초기 생성자로 입력
	     * @param publicKey BigInteger 유형의 공개키 초기 생성자로 입력
	     */
	    public ECKeyPair(BigInteger privateKey, BigInteger publicKey) {
	        this.privateKey = privateKey;
	        this.publicKey = publicKey;
	    }

	    /**
	     * ECKeyPair 생정자로 생성된 공개키쌍중에서 개인키를 가져온다.
	     * @return BigInteger - 개인키를 반환
	     */
	    public BigInteger getPrivateKey() {
	        return privateKey;
	    }

	    /**
	     * ECKeyPair 생정자로 생성된 공개키쌍중에서 공개키를 가져온다.
	     * @return BigInteger - 공개키를 반환
	     */
	    public BigInteger getPublicKey() {
	        return publicKey;
	    }
	    
	    /**
	     * BlockChain ECDSA 공개키쌍(Publickey, PrivateKey)을 생성하여 KeyPair 형으로 생성한다. 
	     * @return KeyPair - ECDSA 공개키쌍을 생성하여 KeyPair 생성자형으로 반환 
	     * @throws NoSuchProviderException 제공하지 않는 프로바이더 설정시 오류
	     * @throws NoSuchAlgorithmException 프로바이더에서 지원하는 암호알고리즘 오류
	     * @throws InvalidAlgorithmParameterException 키 생성 초기화 메소드에서 파라미터 입력 오류
	     */
	    synchronized public static KeyPair blockchainECDSAKeyPair() 
				   throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException
		{
				KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
				ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(bcEcdsaCurveInfo);
				keyPairGenerator.initialize(ecGenParameterSpec, new SecureRandom());
				
				return keyPairGenerator.generateKeyPair();
		}
		

	    /**
	     * 2018현재 안전한 BlockChain ECDSA KeyPair형 공개키쌍을 BigInterger형으로 변환 ECKeyPair 생서자를 생성한다. 
	     * @param keyPair - ECDSA KeyPair 생성자로 생성된 공개키쌍을 입력 
	     * @param compressPublicKey - ECDSA Public Key 축약 사용 유무
	     * @return ECKeyPair - 생성한 ECC 공개키쌍(publicKey, privateKey)의 ECKeyPair 생성자 반환
	     */
	    public static ECKeyPair create(KeyPair keyPair, boolean compressPublicKey) {
	    	
	        BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
	        BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();

	        BigInteger privateKeyValue = privateKey.getD();

	        // Ethereum does not use encoded public keys.
	        // Additionally, as the first bit is a constant prefix (0x04). we ignore this value.
	        byte[] publicKeyBytes = publicKey.getQ().getEncoded(false);
	        
	        //-------Test Add
	        //System.out.println("publicKey [Length / (Uncompress) Byte] = " + publicKeyBytes.length 
			//		+ " / " + Hex.toHexString(publicKeyBytes));

	        BigInteger uncompressedPublicKeyValue =
	                new BigInteger(1, Arrays.copyOfRange(publicKeyBytes, 0, publicKeyBytes.length));
	        
	        if (compressPublicKey) { 

		        uncompressedPublicKeyValue = new BigInteger(1, Arrays.copyOfRange(publicKeyBytes, 1, publicKeyBytes.length));

	        	// Add Code Start - compressedPublicKey = 0x02 or 0x03 + 32Byte(X) [Y=(+)0x02/Y=(-)0x03] 
	        	String compressPubKeyStr = compressPubKey(uncompressedPublicKeyValue);
	        	byte[] compressPubKeyBytes = Hex.decode(compressPubKeyStr); 
	        	//System.out.println("publicKey [Length / (Compress) Byte] = " + compressPubKeyBytes.length 
				//				+ " / " + Hex.toHexString(compressPubKeyBytes));
	        	//System.out.println("");
	        	BigInteger compressedPublicKeyValue =
	                new BigInteger(1, Arrays.copyOfRange(compressPubKeyBytes, 0, compressPubKeyBytes.length));
	        	return new ECKeyPair(privateKeyValue, compressedPublicKeyValue);
	        	// Add Code End--------------
	        } else {
	        	//System.out.println("publicKey [Length / (Uncompress) Byte] = " + publicKeyBytes.length 
				//				+ " / " + Hex.toHexString(publicKeyBytes));
	        	//System.out.println("");

	        	return new ECKeyPair(privateKeyValue, uncompressedPublicKeyValue);
	        }
	    }
	    
		public static String compressPubKey(BigInteger pubKey) {
			
			String pubKeyYPrefix = pubKey.testBit(0) ? "03" : "02";
			//System.out.println("pubKeyYPrefix = " + pubKeyYPrefix);
			
			String pubKeyHex = pubKey.toString(16);
			// 2019.04.03 Add Code[Bob] : if PublicKey Length < 128, 128EA(64Byte) Re-Setting
			//System.out.println("pubKeyHex [Length/Bytes] = " + pubKeyHex.length() + " / " + pubKeyHex);
			if (pubKeyHex.length() == 125) {		// 12bit Add
				pubKeyHex = "000" + pubKeyHex;
				//System.out.println("Re-pubKeyHex [Length/Bytes] = " + pubKeyHex.length() + " / " + pubKeyHex);
			} else if(pubKeyHex.length() == 126) {	// 08bit Add	
				pubKeyHex = "00" + pubKeyHex;
				//System.out.println("Re-pubKeyHex [Length/Bytes] = " + pubKeyHex.length() + " / " + pubKeyHex);
			} else if(pubKeyHex.length() == 127) {	// 04bit Add
				pubKeyHex = "0" + pubKeyHex;
				//System.out.println("Re-pubKeyHex [Length/Bytes] = " + pubKeyHex.length() + " / " + pubKeyHex);
			}
			
			String pubKeyX = pubKeyHex.substring(0, 64); 
			//System.out.println("pubKeyX [Length/Bytes] = " + pubKeyX.length() + " / " + pubKeyX);
			
			// 2019.01.25 Add Code[Bob]
			String pubKeY = pubKeyHex.substring(64, 128); 
			//System.out.println("pubKeyY [Length/Bytes] = " + pubKeY.length() + " / " + pubKeY);
			
			return pubKeyYPrefix + pubKeyX; 
	    } 
	    
	    
	} // End of ECKeyPair Class 


} // End of BlockchainECDSACoreAPI Class 