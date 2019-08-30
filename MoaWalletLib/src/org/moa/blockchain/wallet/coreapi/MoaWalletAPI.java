package org.moa.blockchain.wallet.coreapi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import java.security.GeneralSecurityException;
import java.security.spec.InvalidKeySpecException;

import org.bouncycastle.util.encoders.*;

import org.moa.crypto.coreapi.BlockchainECDSACoreAPI;
import org.moa.crypto.coreapi.DigestCoreAPI;
import org.moa.crypto.coreapi.SecureRNGCoreAPI;
import org.moa.crypto.coreapi.SymmetricCoreAPI;
import org.moa.crypto.coreapi.BlockchainECDSACoreAPI.ECKeyPair;
import org.moa.security.coreapi.KDFSecurityAPI;
import org.moa.security.coreapi.PBESecurityAPI;

/**
 * [*] 기능 정의 : moaWallet.dat 파일을 생성[발급]하고 거래 데이터를 서명 및 검증하는 핵심 API를 제공한다. <br>
 * [*] 파일명 : MoaWalletAPI.java <br>
 * [*] 최초 작성일 : 2019.01.28 <br>
 * [*] 최근 수정일 : 2019.05.13 <br>
 * @author Bob <br> 
 **/
public class MoaWalletAPI {
	
	// Declaration of moa Crypto Core API
	static BlockchainECDSACoreAPI bcECDSA = null;				//- Enhanced Key operation ECDA Alg API
	static DigestCoreAPI  dcAPI = null;							//- Hash(SHA2, SHA3) and HmacHash(HmacSHA2, HmacSHA3) Alg API
	static SymmetricCoreAPI sc = null;							//- Symmetric Crypto(AES, 3DES, SEED, ...) Alg API
	static SecureRNGCoreAPI srng = new SecureRNGCoreAPI();		//- Hash-base DRBG API
	static MoaBase58 base58 = new MoaBase58();					//- Base58 Encoding and Decoding Alg API

	// Declaration of moa Security Core API
	static PBESecurityAPI pbeAPI = null;						//- Psw-base Encryption Security Alg API
	static KDFSecurityAPI pbkdf2API = null;						//- PBKDF2 or KDF Security Alg API 

	// 
	static String PBKDF2CipherSuit = null;						//- "PBKDF2WithHmacSHA384"
	static String PBECipherSuit	= null;							//- "PBEWithSHA256And256BitAES-CBC-BC" or "PBEWithSHAAnd3-KeyTripleDES-CBC" 
	
	// Declaration Cipher Attribute of Encryt or Decrypt of HmacWPsw or HamcReSetWPsw changed form wPsw
	static int iterationCountforDBK = 10;						//- PBKDF2 Computing Count setting for DBK Gen for DBK Gen using id and date of birth
	static String cipherAlgforWPsw = "AES/CBC/PKCS7Padding";	//- Symmetric Crypto Alg for encrypting and decrypting of
																//  HmacWPsw or HamcReSetWPsw changed from wPsw
	// Declaration of ecdsa keyPaire
	static BigInteger ecdsaPuk = null;
	static BigInteger ecdsaPrk = null;
	
	// Declaration of Moa Crypto Alg Policy 
	static int versionInfo = 0;
	static String symmetricAlg = null;
	static int symmetricKeySize = 256;
	static String bcECDSAHashAlgParam = null;
	static String bcEcdsaCurveParam = null;
	static String signatureAlg = null;
	static String hmacAlg = null;
	static int iterationCount = 1000;
	static String walletFileName = null;
	static String restoreWalletFileName = null;
	
	static String tempRestoreWalletFileName = "tempWalletRestore.dat";

	// Declaration of Constant 
	static int MSGDiestSize = 32;
	static int SaltSize = 32; 
	
	// Declaration of hmacWPsw and Random Size for encrypt HmacWPsw
	static int hmacWPswExtractSize = 14;
	static int randSize = 1;
	
	// Declaration of IV Symmetric Alg 
	static byte[] ivBytes = null;

	// Declaration of Character Set
	static Charset charset = Charset.forName("UTF-8");
	

	/**
	 * 'moaWallet.properties' 정책 파일을 기반으로 복원(비복원형) MoaWallet 키 쌍을 발급하고 거래데이터 서명 및 검증하기 위한 MoaWalletAPI 생성자이다.
	 * @param propertyPathAndFileName - 프로퍼티 파일 경로 밒 파일명 입력
	 */
	public MoaWalletAPI(String propertyPathAndFileName) 
    {

		FileInputStream algPolicyfis = null;

		try {
		
			File algPolicyFile = new File(propertyPathAndFileName);
			Path algPolicyFilePath = Paths.get(algPolicyFile.getAbsolutePath());
			
			algPolicyfis = new FileInputStream(algPolicyFilePath.toString());

			if ( !algPolicyFile.exists() ) {
				System.out.println("'moaWallet.properties' file Not found");
				return;
			}
		
			// Properties Initialization of MoaWalletAPI Constructor 
			Properties propAlg = new Properties();
			propAlg.load(algPolicyfis);
			versionInfo = Integer.parseInt(propAlg.getProperty("Version.Info"));
			symmetricAlg = propAlg.getProperty("Symmetric.Alg").trim();
			symmetricKeySize = Integer.parseInt(propAlg.getProperty("Symmetric.KeySize"));
			bcECDSAHashAlgParam = propAlg.getProperty("Hash.Alg").trim();
			signatureAlg = propAlg.getProperty("Signature.Alg").trim();
			bcEcdsaCurveParam = propAlg.getProperty("ECC.Curve").trim();
			hmacAlg = propAlg.getProperty("MAC.Alg").trim();
			iterationCount = Integer.parseInt(propAlg.getProperty("Iteration.Count"));
			walletFileName = propAlg.getProperty("Wallet.FileName").trim();
			restoreWalletFileName = propAlg.getProperty("RestoreWallet.FileName".trim());

			// 2019.07.11 Add(Release of Properties Object)
			propAlg = null;	
		
		} catch(FileNotFoundException fe) {
		} catch(IOException e) {
		} finally {
			try {
				// 2019.07.11 Add(FileInputStream Close)
				if (algPolicyfis != null) algPolicyfis.close(); 	
			} catch (IOException e) {}	
		}
		
        switch (bcECDSAHashAlgParam) { 
        
        	case "SHA3-256"	: 
        	case "SHA256"	: MSGDiestSize = 32;
        					  SaltSize  = 32; 
                       	  	  break; 
        	case "SHA3-384"	:
        	case "SHA384"	: MSGDiestSize = 32;
        					  SaltSize  = 48; 
                       	  	  break;
        	case "SHA3-512"	:               
        	case "SHA512"	: MSGDiestSize = 32;
        					  SaltSize  = 64; 
                       	  	  break; 
        	default :
        		break;
                       	  	  
        } 

		// Gen of PBESecurityAPI Constructor 
		PBECipherSuit = "PBEWith"+ bcECDSAHashAlgParam+ "And256BitAES-CBC-BC";
    	pbeAPI 	= new PBESecurityAPI(PBECipherSuit, symmetricAlg, "BC"); 
    	
    	// Gen of PBESecurityAPI Constructor 
    	PBKDF2CipherSuit = "PBKDF2WithHmacSHA384";
    	pbkdf2API = new KDFSecurityAPI(PBKDF2CipherSuit, symmetricAlg,  "BC");
		
		// Gen of BlockchainECDSACoreAPI Constructor
    	bcECDSA = new BlockchainECDSACoreAPI(bcEcdsaCurveParam, bcECDSAHashAlgParam);
    	
    }

	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//-[*] ---------------------------------[ START OF PUBLIC METHOD : Moa Wallet API I/F]---------------------------------- [*]

	//--[ Blockchain Wallet wPsw ReSet Process Start for Restore ------------------------------->
	public final String  BCWalletEncryptWPswGenProcess(String idStr, String wPswStr, String dateOfBirthStr) 
		   throws NullPointerException, Exception
	{		
		String hashMoapayPswB64$hmacWPsw14B64$EhmacWPsw16HMAC16B64str = BCWalletEncryptWPswGenCoreEngine(idStr, wPswStr, dateOfBirthStr);
		return hashMoapayPswB64$hmacWPsw14B64$EhmacWPsw16HMAC16B64str;	// hashMoapayPswB64str $ hmacWPsw14B64str $ (EhmacWPsw16Bytes||Hmac16Bytes)B64str
	}
	
	public final String BCWalletHmacWPswGenProcess(String wPswStr)
		   throws GeneralSecurityException
	{		
		byte[] hmacWPsw14Bytes = BCWalletHmacWPswGenCoreEngine(wPswStr); 
		return Base64.toBase64String(hmacWPsw14Bytes);	// hmacWPsw14Bytes Gen using wPswStr
	}
	
	
	public final String  BCWalletWPswVerifyAndDecryptGenProcess(String idStr, String dateOfBirthStr, String encryptHmacWPswAndMACB64str) 
		   throws NullPointerException, Exception
	{		
		String doBVerifySFStr$hmacwPsw14B64Str = BCWalletWPswVerifyAndDecryptCoreEngine(idStr, dateOfBirthStr, encryptHmacWPswAndMACB64str); 
		return doBVerifySFStr$hmacwPsw14B64Str;		// Validity Check SF["0x5015"] about input DoB $ HmacWPsw14B64str 	
	}
	

	public final String BlockchainWalletPswReSetRegistDataGenProcess(String existHmacWPswB64Str, String newHmacReSetWPswB64Str, String existHamcEWPukB64Str, String existEwPrkB64strAndEwPukB64strAndwSaltB64str) 
			   throws NullPointerException, Exception
	{		
		String orgWPukB64strPerConcatNewEwPrkB64str$NewEwPukB64str$NewWSaltB64str 
							= BlockchainWalletPswReSetRegistDataGenCoreEngine(existHmacWPswB64Str, newHmacReSetWPswB64Str, existHamcEWPukB64Str, existEwPrkB64strAndEwPukB64strAndwSaltB64str); 
		return orgWPukB64strPerConcatNewEwPrkB64str$NewEwPukB64str$NewWSaltB64str;	// originalWPukB64str or existHmacWPsw-CheckSF % [NewEwPrkB64str $ NewEwPukB64str $ NewWSaltB64str] 	
	}
	
	public final void BlockchainWalletPswReSetStorageGenProcess(String newHmacReSetWPswB64Str, String orgWPukB64Str, String newEwPrkB64strAndEwPukB64strAndwSaltB64str) 
		   throws NullPointerException, Exception
	{		
		BlockchainWalletPswReSetStorageGenCoreEngine(newHmacReSetWPswB64Str, orgWPukB64Str, newEwPrkB64strAndEwPukB64strAndwSaltB64str); 	// moaWalletRestore.dat Gen
	}


	//--[ NonRestore Blockchain Wallet Issuing Process Start ------------------------------->
	public final void NonRestoreBlockchainWalletIssuingProcess(String alias, String password)
				 throws NullPointerException, Exception 
	{
		NonRestoreBlockchainWalletIssuing(alias, password);
	}

	//--[ Restore Blockchain Wallet Issuing Process Start ------------------------------->
	public final void RestoreBlockchainWalletIssuingProcess(String passwordStr, String encWPrkBase64AndencWPukBase64AndwSaltBase64Str) 
				 throws NullPointerException, Exception
	{
		RestoreBlockchainWalletIssuing(passwordStr, encWPrkBase64AndencWPukBase64AndwSaltBase64Str); 
	}

	public final String RestoreBlockchainWalletInfoGenProcess(String alias, String passwordStr)
				 throws NullPointerException, Exception
    {
    	String bcWalletEncwPrk$EncwPuk$wSalt = RestoreBlockchainWalletInfoGen(alias, passwordStr);
    	return bcWalletEncwPrk$EncwPuk$wSalt;
    }
    
	
	public final String RestoreBlockchainWalletProcess(String passwordStr, String existHmacEwPukBase64Str, String encWPrkBase64AndencWPukBase64AndwSaltBase64Str)
				 throws NullPointerException, Exception	// encWPrkAndencWPukAndwSalt = encWPrkBase64Str$encWPukBase64Str$wSaltBase64Str 
    {
		String inputWPswValidityCheckSF = RestoreBlockchainWallet(passwordStr, existHmacEwPukBase64Str, encWPrkBase64AndencWPukBase64AndwSaltBase64Str);
		return inputWPswValidityCheckSF;			// wPsw Validity Check Success=["0x509E"] or Fail=["0x509F"]
    }
	
	
	public final void BlockchainWalletFileDeleteProcess(String bcWalletFileName, String deleteBCWalletFlag) 
				 throws IOException
	{	   
		
		BlockchainWalletFileDelete(bcWalletFileName, deleteBCWalletFlag);	// Delete an invalid BC-Wallet data file.
	
	}
	
	public final void BlockchainWalletFileUpdateProcess() {
		
		BlockchainWalletFileUpdate();
	}
	
	
    /**  ----[ 2019.04.23 Add Method 
     * Ethereum Wallet ECDSA PublicKey[=WPuk]를 기반으로 Wallet 주소를 생성한다.
	 * @param hashAlg 이더리움용 전자지갑의 주소를 생성하기 위한 해시 알고리즘 입력(32Byte 축약) : SHA256(현재) / SHA3-256(미래)
	 * @param publicKeyBytes 이더리움용 전자지갑의 주소를 생성하기 위하여 공개키 바이트 배열을 입력  
	 * @return ethAddr 이더리움 주소 반환 
     * @throws GeneralSecurityException 일반 보안 오류
     */
    @SuppressWarnings("static-access")
	public static byte[] EthereumWalletAddrGen(String hashAlg, byte[] publicKeyBytes) 
		   throws GeneralSecurityException
	{
		
		byte[] shaNBytes  = dcAPI.HashDigest(hashAlg, publicKeyBytes, "BC");	// SHA256(현재) -> SHA3-256(업그레이드시) 
		//System.out.println("  > eth Public Key Hash [Byte : " + shaNBytes.length + " ] = " + Hex.toHexString(shaNBytes));
		
		// Ethreum Wallet Address(keccak-256[=SHA3-256]:32Byte) = 12Byte[Drop]20Byte[address]
		byte[] ethAddr = new byte[20];
		System.arraycopy(shaNBytes, 12, ethAddr, 0, ethAddr.length);
		//System.out.println("  > eth.Addr [Byte : " + shaNBytes.length + " ] = " + Hex.toHexString(ethAddr));
		
		return ethAddr;
		
	}

    
    /**
     * Bit Coin(BTC) Wallet ECDSA PublicKey[=WPuk]를 기반으로 Wallet 주소를 생성한다.
	 * @param hashAlg1 전자지갑의 주소를 생성하기 위한 1차 해시 알고리즘 입력(32Byte 축약) 
	 * @param hashAlg2 전자지갑의 주소를 생성하기 위한 2차 해시 알고리즘 입력(20Byte 축약)
	 * @param publicKeyBytes 비트코인의 전자지갑 주소를 생성하기 위하여 공개키 배열을 입력
	 * @return BTC 주소를 반환 
     * @throws GeneralSecurityException 일반 보안 오류
     */
    @SuppressWarnings("static-access")
	public final byte[] BTCWalletAddrGen(String hashAlg1, String hashAlg2, byte[] publicKeyBytes) 
 		   throws GeneralSecurityException
 	{
 		
 		byte[] sha256Bytes  = dcAPI.HashDigest(hashAlg1, publicKeyBytes, "BC");
 		byte[] ripe160Bytes = dcAPI.HashDigest(hashAlg2, sha256Bytes, "BC");
 		
 		byte[] checkSumBytes = new byte[4];		// CheckSum 4Bytes Array Setting
 		System.arraycopy(sha256Bytes, 0, checkSumBytes, 0, checkSumBytes.length);
 		
 		int firstInfoLen = 1, checkSumLen = 4;
 		int ethBlockChainAddrLen = firstInfoLen + ripe160Bytes.length + checkSumLen;
 		ByteBuffer byteBuf = ByteBuffer.allocate(ethBlockChainAddrLen);
 		byteBuf.clear();
 		byteBuf.order(ByteOrder.BIG_ENDIAN);
 		
 		// Ethreum Wallet Address = FirstInfoByte||RIPE160Bytes||CheckSumBytes
 		//          Total 25Bytes =      1              20            4   	 
 		byteBuf.put((byte)0x00);		// 1Byte
 		byteBuf.put(ripe160Bytes);		// 20Bytes
 		byteBuf.put(checkSumBytes);		// 4Bytes
 		
 		return byteBuf.array();
 		
 	}

    
    /**
     * moaWallet.dat 파일에서 공개키 가져온다.
     * @param restoreFlag 복원 혹은 비복원 월릿 데이터 선택 정보
	 * @return Base58로 엔코딩된 WPuk 문자열 데이터 반환 
     * @throws FileNotFoundException 파일 존재 유무 오류
     * @throws IOException 입출력 오류
     */
    public final String GetBlockchainPulicKey(boolean restoreFlag) 
    	   throws FileNotFoundException, IOException 
    {
    	
    	File walletFile; 
    	if (restoreFlag == true) {	// Restore Wallet File Setting
    		walletFile = new File(restoreWalletFileName); 
    	} else {					// Non-Restore Wallet File Setting
    		walletFile = new File(walletFileName); 
    	}

    	Path walletFilePath = Paths.get(walletFile.getAbsolutePath());
    	FileInputStream fis = new FileInputStream(walletFilePath.toString());
    			    
    	Properties prop = new Properties();
    	prop.load(fis);
    	String walletPublicKeyBase58Str = prop.getProperty("Wallet.PublicKey").trim();

    	fis.close(); 	// 2019.07.11 Add(FileInputStream Close)
    	prop = null;	// 2019.07.11 Add(Release of Properties Object)

    	return walletPublicKeyBase58Str;
    	
    }
    
    
    /**
     * moaWalletRestore.dat or moaWallet.dat 파일에서 wSalt 가져온다.
     * @param restoreFlag 복원 혹은 비복원 월릿 데이터 선택 정보
	 * @return Base58로 엔코딩된 WSalt 문자열 데이터 반환 
     * @throws FileNotFoundException 파일 존재 유무 오류
     * @throws IOException 입출력 오류
     */
    public final String GetBlockchainWalletSalt(boolean restoreFlag) 
    	   throws FileNotFoundException, IOException 
    {
    	
    	File walletFile; 
    	if (restoreFlag == true) {	// Restore Wallet File Setting
    		walletFile = new File(restoreWalletFileName); 
    	} else {					// Non-Restore Wallet File Setting
    		walletFile = new File(walletFileName); 
    	}

    	Path walletFilePath = Paths.get(walletFile.getAbsolutePath());
    	FileInputStream fis = new FileInputStream(walletFilePath.toString());
    			    
    	Properties prop = new Properties();
    	prop.load(fis);
    	String walletSaltBase58Str = prop.getProperty("Salt.Value").trim();

    	fis.close(); 	// 2019.07.11 Add(FileInputStream Close)
    	prop = null;	// 2019.07.11 Add(Release of Properties Object)

    	return walletSaltBase58Str;
    	
    }

    

    /**
     * moaWallet.dat 파일에서 전자지갑 주소를 가져온다.
     * @param restoreFlag 복원 혹은 비복원 월릿 데이터 선택 정보
	 * @return Base58로 엔코딩된 WAddr 문자열 데이터 반환 
     * @throws FileNotFoundException 파일 존재 유무 오류
     * @throws IOException 입출력 오류
     */
    public final String GetBlockchainWalletAddr(boolean restoreFlag) 
    	   throws FileNotFoundException, IOException 
    {
    	
    	File walletFile; 
    	if (restoreFlag == true) {	// Restore Wallet File Setting
    		walletFile = new File(restoreWalletFileName); 
    	} else {					// Non-Restore Wallet File Setting
    		walletFile = new File(walletFileName); 
    	}

    	Path walletFilePath = Paths.get(walletFile.getAbsolutePath());
    	FileInputStream fis = new FileInputStream(walletFilePath.toString());
    			    
    	Properties prop = new Properties();
    	prop.load(fis);
    	String walletAddrBase58Str = prop.getProperty("Wallet.Addr").trim();
    	
    	fis.close(); 	// 2019.07.11 Add(FileInputStream Close)
    	prop = null;	// 2019.07.11 Add(Release of Properties Object)

    	return walletAddrBase58Str;
    	
    }


    
    /**
     * moaWallet.dat에 이중 암호화되어 있는 ECDSA PrivateKey[=WPrk])을 복호화한 후 입력받은 거래데이터에 서명 바이트 데이터를 생성한다.
     * @param  alias 안드로이드 알리아스 문자열 데이터 입력
	 * @param  trasctionDataStr WPuk로 검증할 거래 데이터 바이트 배열 입력
	 * @param  passwordStr  월릿 복원을 위한 wPsw 문자열 데이터 입력 
	 * @param  restoreFlagStr  복원형/비복원형/임시["RESTORE"/"NON"/"TEMP"] 월릿 식별자 데이터 플래그 문자열 정보 입력
	 * @return  byte[] 서명한 바이트 배열 반환 
	 * @throws  NullPointerException 파라미터 널 입력 오류
     * @throws  Exception 일반적인 예외 오류
     */
	public final byte[] BlockchainTransactionSignProcess(String alias, String trasctionDataStr, String passwordStr, String restoreFlagStr)
			 throws NullPointerException, Exception   
	{
	
		byte[] signatureBytes = BlockchainTransactionSign(alias, trasctionDataStr, passwordStr, restoreFlagStr);
	
		return signatureBytes;
	}


	
    public final boolean BlockchainTransactionVerifyProcess(byte[] trasctionDataBytes, byte[] signatureBytes, byte[] publicKeyBytes) 
    {
    	return BlockchainTransactionVerify(trasctionDataBytes, signatureBytes, publicKeyBytes);
    }
    
    //-[*] ------------------------------------[ END OF PUBLIC ]---------------------------------------------------- [*]


	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //-[*] ------------------------------------[ START OF PRIVATE ]----------------------------------------------> 
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /** [비복원형 Wallet]
     * Ethereum Wallet ECDSA 공개키쌍(PublicKey[=wPuk], PrivateKey[=wPrk])을 생성 한 후 PSW 기반 3개의 키를 생성하여 <br>
     * wPuk, wPrk를  이중암호화 및 wPuk를 기반으로 wAddr을 생성 및 Wallet 데이터에 대한 무결성을 생성한 후 최종 moaWallet.dat <br>
     * 파일을 생성한다. <br>
	 * @param alias TEE 영역의 RSA 연산 수행 시 필요한 별명(현재 코드에는 사용하지 않고 파라미터로 입력만 유지) 
	 * @param password PBKDF2 기반 키 생성 및 암호화 연산 시 입력되는 문자열 데이터, PBE(PC) 암호화 연산시 입력되는 문자열
     * @throws NullPointerException 파라미터 널 입력 오류
     * @throws Exception 일반적인 예외 오류
     */
    @SuppressWarnings("static-access")
	private static void NonRestoreBlockchainWalletIssuing(String alias, String password)
				   throws NullPointerException, Exception
    {
    	
    	// Non-Restore Blockchain Wallet IDentifier
    	String walletTypeStr = "NonR";
    	
    	//--------[ ECDSA KeyPair Gen & transform of Key Type BigInterger to byte
		// ECKeyPair ecKP = ECKeyPair.create(ECKeyPair.blockchainECDSAKeyPair(), false);	// Not Compressed Public Key
		// ECKeyPair ecKP = ECKeyPair.create(ECKeyPair.blockchainECDSAKeyPair(), true);		// Compressed Public Key
    	ECKeyPair ecKP = ECKeyPair.create(ECKeyPair.blockchainECDSAKeyPair(), false);
    	
		ecdsaPrk = ecKP.getPrivateKey();
    	ecdsaPuk = ecKP.getPublicKey();
		
		// System.out.println("  > ecdsaPrk [BigInterger] = " + ecdsaPrk);

		byte[] privateKeyBytes	= ecdsaPrk.toByteArray(); 
		byte[] publicKeyBytes	= ecdsaPuk.toByteArray();

		// System.out.println("  > Original Public Key  [Byte] = " + Hex.toHexString(publicKeyBytes)  + " [Lenth] = " + publicKeyBytes.length);
		// System.out.println("  > Original Private Key [Byte] = " + Hex.toHexString(privateKeyBytes) + " [Lenth] = " + privateKeyBytes.length);
		
		//--------[ Salt Gen
		byte[] salt = srng.SecureRandNumberGen(SaltSize);	// Hash Alg Digest Size

		//--------[ Encrypt Procedure : Double(1st -> 2nd) Encrypted
		byte[] encryptedPrivateKeyBytes = BlockchainEncryptPrk(privateKeyBytes, password, salt, iterationCount);
		byte[] pswBytes = password.getBytes(charset);
		
		//--------[ password & Private Key Delete in Memory 
		password = "";
		ecdsaPrk = BigInteger.ZERO;
		privateKeyBytes = new byte[1];
		// System.out.println("  > Deleted ecdsaPrk [BigInterger] = " + ecdsaPrk);
		// System.out.println("  > Deleted ecdsaPrk [Bytes]       = " + Hex.toHexString(privateKeyBytes));
		
		//--------[ Wallet Addr Gen Procedure
		// System.out.println("------------------------------------------------------------------------------------- ");
		byte[] ethWalletAddrBytes = EthereumWalletAddrGen(bcECDSAHashAlgParam, publicKeyBytes);		 
		String moaWalletAddrBase58Str = base58.encode(ethWalletAddrBytes);
		// System.out.println("  > Eth Wallet Addr [Last 20Bytes] = " + Hex.toHexString(ethWalletAddrBytes));
		// System.out.println("  > Eth Wallet Addr [MoaBase58]    = " + moaWalletAddrBase58Str);
		
		//--------[ Wallet Data Type Conversion
		String saltBase58Str = MoaBase58.encode(salt);
		String cipheredBase58Str = MoaBase58.encode(encryptedPrivateKeyBytes);
		String walletPublicKeyBase58Str = MoaBase58.encode(publicKeyBytes);

		//--------[ moaWallet.dat Hmac Key Gen
		byte[] saltConcatPswBytes = new byte[salt.length + pswBytes.length];
		System.arraycopy(salt, 0, saltConcatPswBytes, 0, salt.length);
		System.arraycopy(pswBytes, 0, saltConcatPswBytes, salt.length, pswBytes.length);
		pswBytes = new byte[1];  // Password Bytes delete in Memory
		
		byte[] hmacKey = dcAPI.HashDigest(bcECDSAHashAlgParam, saltConcatPswBytes, "BC");
		// System.out.println("  > Hmac Key [Byte : " + hmacKey.length + " ] = " + Hex.toHexString(hmacKey));
		
		String osName = System.getProperty("os.name");
		String walletConcat = versionInfo + osName + walletTypeStr + saltBase58Str + iterationCount + cipheredBase58Str + walletPublicKeyBase58Str + moaWalletAddrBase58Str;
		
		byte[] macDataBytes = dcAPI.HmacDigest(hmacAlg, walletConcat.getBytes("UTF-8"), hmacKey, "BC");
		String macDataBase58Str = MoaBase58.encode(macDataBytes);
		
		//--------[ HmacKey delete in Memory 
		hmacKey = new byte[1];
		
		//--------[ moaWallet.dat File Gen
		String walletFileStorage = BlockchainWalletInfoGen(walletTypeStr, saltBase58Str, iterationCount, cipheredBase58Str, walletPublicKeyBase58Str, moaWalletAddrBase58Str, macDataBase58Str); 
		BlockchainWalletFileStorage(walletFileStorage, walletFileName, false);
		
    }
    
    

    /** [복원형 Wallet 구성 데이터 생성]
     * Ethereum Wallet ECDSA 공개키쌍(PublicKey[=wPuk], PrivateKey[=wPrk])을 생성 한 후 PSW 기반 1st 키를 생성하여 <br>
     * wPuk, wPrk 각각을 1st 암호화한 후 EPuk, EwPrk, wSalt를 Base64로 엔코딩한후 연접한 데이터를 생성한다. <br>
     * @param alias 복구형 별명
	 * @param passwordStr PBKDF2 기반 키 생성 및 암호화 연산 시 입력되는 문자열
	 * @return encWPrkAndencWPukAndwSalt Base64EncwPrk, Base64EncwPuk, Base64wSalt 연접한 문자열 
     * @throws NullPointerException 파라미터 널 입력 오류
     * @throws Exception 일반적인 예외 오류
     */
    @SuppressWarnings({ "static-access" }) 	// 2019.06.19 Modified
	private static String RestoreBlockchainWalletInfoGen(String alias, String passwordStr)
    	   throws NullPointerException, Exception
    {
    	
    	//--------[ ECDSA KeyPair Gen & transform of Key Type BigInterger to byte
		//ECKeyPair ecKP = ECKeyPair.create(ECKeyPair.blockchainECDSAKeyPair(), false);		// Not Compressed Public Key
		//ECKeyPair ecKP = ECKeyPair.create(ECKeyPair.blockchainECDSAKeyPair(), true);		// Compressed Public Key
    	ECKeyPair ecKP = ECKeyPair.create(ECKeyPair.blockchainECDSAKeyPair(), false);
    	
		ecdsaPrk = ecKP.getPrivateKey();
    	ecdsaPuk = ecKP.getPublicKey();
		
		//System.out.println("  > ecdsaPrk [BigInterger] = " + ecdsaPrk);

		//byte[] privateKeyBytes = ecdsaPrk.toByteArray(); 
		//byte[] publicKeyBytes = ecdsaPuk.toByteArray();
		byte[] privateKeyBytes = Hex.decode("917ecfad03c5b2c214c3ba2c58c2a4475aa40aece64e500db7db93d972049bcb");
		byte[] publicKeyBytes  = Hex.decode("041997c990af96e68fb2cbd5ffe171db1f3add7e39db536baba973431356100d0dd9123e7e8688946d6a47c0ea5eb95639b32f483f2e73c2bd4a665b97024a13ec");
		System.out.println("  > Original Public  Key [Byte]   = " + Hex.toHexString(publicKeyBytes)  + " [Lenth] = " + publicKeyBytes.length);
		System.out.println("  > Original Private Key [Byte]   = " + Hex.toHexString(privateKeyBytes) + " [Lenth] = " + privateKeyBytes.length);

		
		//--------[ Salt Gen
		// byte[] wSaltBytes = srng.SecureRandNumberGen(SaltSize);	// Hash Alg Digest Size
		byte[] wSaltBytes =  Hex.decode("2fff460391c2f88db4c63f4670a59f7170e05431a4dc947a447731df2ba4c573");
		System.out.println("  > wSaltBytes [Byte]   = " + Hex.toHexString(wSaltBytes) + " [Lenth] = " + wSaltBytes.length);

		//--------[ PswString -> PswBytes 
		byte[] pswBytes = passwordStr.getBytes(charset);
		
		//--------[ moaWallet wPrk, Puk Encrypt & wSalt base Restore Msg Gen
		byte[] dk = pbkdf2API.PBKDF2KeyGen(passwordStr, wSaltBytes, iterationCount, 48*8);
		byte[] bcWalletKeyBytes = Arrays.copyOf(dk, 32);
		byte[] bcWalletIVBytes  = Arrays.copyOfRange(dk, 32, dk.length);
		
		System.out.println("  > dk  [Hex] = " + Hex.toHexString(dk) + " / [Len] = " + dk.length);
		System.out.println("  > key [Hex] = " + Hex.toHexString(bcWalletKeyBytes) + " / [Len] = " + bcWalletKeyBytes.length);
		System.out.println("  > iv  [Hex] = " + Hex.toHexString(bcWalletIVBytes)  + " / [Len] = " + bcWalletIVBytes.length);


		//--------[ Encrypt Procedure : 1st Encrypted
		sc = new SymmetricCoreAPI(symmetricAlg, bcWalletIVBytes, bcWalletKeyBytes, "BC");
		byte[] encwPrkBytes = sc.SymmetricEncryptData(privateKeyBytes);
		byte[] encwPukBytes = sc.SymmetricEncryptData(publicKeyBytes);
		
		byte[] existHmacEwPukBytes = dcAPI.HmacDigest(hmacAlg, encwPukBytes, pswBytes, "BC");
		
		System.out.println("  > encwPrkBytes        [Hex] = " + Hex.toHexString(encwPrkBytes)        + " / [Len] = " + encwPrkBytes.length);
		System.out.println("  > encwPukBytes        [Hex] = " + Hex.toHexString(encwPukBytes)        + " / [Len] = " + encwPukBytes.length);
		System.out.println("  > existHmacEwPukBytes [Hex] = " + Hex.toHexString(existHmacEwPukBytes) + " / [Len] = " + existHmacEwPukBytes.length);

		//System.out.println("encwPrkBytes[Size] = " + encwPrkBytes.length + " / encwPrkBytes[Size] = " +
		//					encwPukBytes.length + " / wSaltBytes[size] = " + wSaltBytes.length + " / existHmacEwPukBytes[size] = " + existHmacEwPukBytes.length );
		
		
		String existHamcEwPukPerConcatEncwPrk$EncwPuk$wSalt = Base64.toBase64String(existHmacEwPukBytes) + "%" +
											   				  Base64.toBase64String(encwPrkBytes)+"$"+Base64.toBase64String(encwPukBytes)+"$"+Base64.toBase64String(wSaltBytes);
		
		//--------[ Private Key Delete in Memory 
		ecdsaPrk = BigInteger.ZERO;
		privateKeyBytes = new byte[1];
		//System.out.println("  > Deleted ecdsaPrk [BigInterger] = " + ecdsaPrk);
		//System.out.println("  > Deleted Private Key [Byte] = " + Hex.toHexString(privateKeyBytes));
		
		return existHamcEwPukPerConcatEncwPrk$EncwPuk$wSalt;
		
    }

    
    /** [복원형 moaWallet.dat 발급]
     * 패스워드와 1st 암호화된 EwPrkBase64$EwPukBase64$wSaltBase64를 입력받아 이중암호화된 wPrk 및 일반 wPuk를 기반으로 <br>
     * wAddr을 생성 및 Wallet 데이터에 대한 무결성을 생성한 후 최종 moaWallet.dat 파일을 생성한다. <br>
	 * @param passwordStr PBKDF2 기반 키 생성 및 암호화 연산 시 입력되는 문자열 데이터, PBE(PC) 암호화 연산시 입력되는 문자열
	 * @param encWPrkBase64AndencWPukBase64AndwSaltBase64 Base64EncwPrk, Base64EncwPuk, Base64wSalt 연접한 문자열 
     * @throws NullPointerException 파라미터 널 입력 오류
     * @throws Exception 일반적인 예외 오류
     */
    @SuppressWarnings("static-access")
	private static void RestoreBlockchainWalletIssuing(String passwordStr, String encWPrkBase64AndencWPukBase64AndwSaltBase64) 
				   throws NullPointerException, Exception
    { 
    	// Restore Blockchain Wallet IDentifier
    	String walletTypeStr = "R";
    	
    	// if Restore Wallet EwPrk, EwPuk||wSalt AuthDB storage Success, Next Step Running
    	byte[] pswBytes = passwordStr.getBytes(charset);
    	
    	if (encWPrkBase64AndencWPukBase64AndwSaltBase64 == null) {
    		return;
    	} 
    	
    	// 1st Encrypt Restore Data EwPrkBase64, EwPukBase64, wSaltBase64 Parsing
		StringTokenizer wPrkwPukwSaltST = new StringTokenizer(encWPrkBase64AndencWPukBase64AndwSaltBase64, "$");
		byte[] firstEncryptWPrkBytes = Base64.decode(wPrkwPukwSaltST.nextToken());	// 1st Encrypted wPrk Tokenizer
		byte[] firstEncryptWPukBytes = Base64.decode(wPrkwPukwSaltST.nextToken());	// 1st Encrypted wPuk Tokenizer
		byte[] saltBytes  	   		 = Base64.decode(wPrkwPukwSaltST.nextToken());	// wSalt Tokenizer
		
		// Decrypt of 1st Encrypt EwPuk
		byte[] dk = pbkdf2API.PBKDF2KeyGen(passwordStr, saltBytes, iterationCount, 48*8);
		byte[] bcWalletKeyBytes = Arrays.copyOf(dk, 32);
		byte[] bcWalletIVBytes  = Arrays.copyOfRange(dk, 32, dk.length);
		
		System.out.println("  > dk  [Hex] = " + Hex.toHexString(dk) + " / [Len] = " + dk.length);
		System.out.println("  > key [Hex] = " + Hex.toHexString(bcWalletKeyBytes) + " / [Len] = " + bcWalletKeyBytes.length);
		System.out.println("  > iv  [Hex] = " + Hex.toHexString(bcWalletIVBytes)  + " / [Len] = " + bcWalletIVBytes.length);
			
		sc = new SymmetricCoreAPI(symmetricAlg, bcWalletIVBytes, bcWalletKeyBytes, "BC");
		byte[] publicKeyBytes = sc.SymmetricDecryptData(firstEncryptWPukBytes);
		System.out.println("  > publicKeyBytes [Hex] = " + Hex.toHexString(publicKeyBytes)  + " / [Len] = " + publicKeyBytes.length);

		// 2nd Encrypt of 1st Encrypt EwPrk
		byte[] secondEncryptPrkBytes = PBESecurityAPI.pbeEncrypt(firstEncryptWPrkBytes, passwordStr.toCharArray(), saltBytes, iterationCount);
		
		//--------[ Wallet Addr Gen Procedure
		byte[] ethWalletAddrBytes = EthereumWalletAddrGen(bcECDSAHashAlgParam, publicKeyBytes);		 
		String moaWalletAddrBase58Str = base58.encode(ethWalletAddrBytes);
		//System.out.println("  > Eth Wallet Addr [Last 20Bytes] = " + Hex.toHexString(ethWalletAddrBytes));
		//System.out.println("  > Eth Wallet Addr [MoaBase58]    = " + moaWalletAddrBase58Str);
		
		//--------[ Wallet Data Type Conversion
		String saltBase58Str = MoaBase58.encode(saltBytes);
		String cipheredBase58Str = MoaBase58.encode(secondEncryptPrkBytes);
		String walletPublicKeyBase58Str = MoaBase58.encode(publicKeyBytes);

		//--------[ moaWallet.dat Hmac Key Gen
		byte[] saltConcatPswBytes = new byte[saltBytes.length + pswBytes.length];
		System.arraycopy(saltBytes, 0, saltConcatPswBytes, 0, saltBytes.length);
		System.arraycopy(pswBytes, 0, saltConcatPswBytes, saltBytes.length, pswBytes.length);
		pswBytes = new byte[1];  // Password Bytes delete in Memory
		
		byte[] hmacKey = dcAPI.HashDigest(bcECDSAHashAlgParam, saltConcatPswBytes, "BC");
		//System.out.println("  > Hmac Key [Byte : " + hmacKey.length + " ] = " + Hex.toHexString(hmacKey));
		
		String osName = System.getProperty("os.name");
		// String walletType = System.getProperty("os.name");
		String walletConcat = versionInfo + osName + walletTypeStr + saltBase58Str + iterationCount + cipheredBase58Str + walletPublicKeyBase58Str + moaWalletAddrBase58Str;
		byte[] macDataBytes = dcAPI.HmacDigest(hmacAlg, walletConcat.getBytes("UTF-8"), hmacKey, "BC");
		String macDataBase58Str = MoaBase58.encode(macDataBytes);
		
		//--------[ HmacKey delete in Memory 
		hmacKey = new byte[1];
		
		//--------[ moaWallet.dat File Gen
		String walletFileStorage = BlockchainWalletInfoGen(walletTypeStr, saltBase58Str, iterationCount, cipheredBase58Str, walletPublicKeyBase58Str, moaWalletAddrBase58Str, macDataBase58Str); 
		BlockchainWalletFileStorage(walletFileStorage, restoreWalletFileName, false);
		
    }

    
    /** [moaWallet.dat 복원]
     * 패스워드와 1st 암호화된 EwPrkBase64$EwPukBase64$wSaltBase64를 입력받아 이중암호화된 wPrk 및 일반 wPuk를 기반으로 <br>
     * wAddr을 생성 및 Wallet 데이터에 대한 무결성을 생성한 후 최종 moaWallet.dat 파일을 생성한다.<br>
	 * @param passwordStr PBKDF2 기반 키 생성 및 암호화 연산 시 입력되는 문자열 데이터, PBE(PC) 암호화 연산시 입력되는 문자열
	 * @param existHmacEwPukBase64Str 복원을 위한 기존 AuthDB에 저장되어 있던 수신받은 'HmacEwPukBase64Str' 문자열 입력
	 * @param encWPrkBase64AndencWPukBase64AndwSaltBase64 wPrk, wPuk, wSalt를 '$' 토큰으로 연접한 문자열 입력
     * @throws NullPointerException 파라미터 널 입력 오류
     * @throws Exception 일반적인 예외 오류
     */ 
    // 2019.06.19 Modified
    @SuppressWarnings({ "static-access" })
	private static String RestoreBlockchainWallet(String passwordStr, String existHmacEwPukBase64Str, String encWPrkBase64AndencWPukBase64AndwSaltBase64)
				   throws NullPointerException, Exception    // encWPrkAndencWPukAndwSalt = encWPrkBase64Str$encWPukBase64Str$wSaltBase64Str 
    {
    	
    	String walletTypeStr = "R";
    	String inputParamNullStringCheck = null;
    	
    	byte[] pswBytes = passwordStr.getBytes(charset);
    	
    	if (encWPrkBase64AndencWPukBase64AndwSaltBase64 == null && existHmacEwPukBase64Str == null) {
    		inputParamNullStringCheck = "0x50A0"; 
    		return inputParamNullStringCheck;
    	} 
    	
		StringTokenizer wPrkwPukwSaltST = new StringTokenizer(encWPrkBase64AndencWPukBase64AndwSaltBase64, "$");
		byte[] firstEncryptWPrkBytes = Base64.decode(wPrkwPukwSaltST.nextToken());	// 1st Encrypted wPrk Tokenizer
		byte[] firstEncryptWPukBytes = Base64.decode(wPrkwPukwSaltST.nextToken());	// 1st Encrypted wPuk Tokenizer
		byte[] saltBytes  	   		 = Base64.decode(wPrkwPukwSaltST.nextToken());	// wSalt Tokenizer
		
		System.out.println("  > firstEncryptWPrkBytes [Hex] = " + Hex.toHexString(firstEncryptWPrkBytes));
		System.out.println("  > firstEncryptWPukBytes [Hex] = " + Hex.toHexString(firstEncryptWPukBytes));
		System.out.println("  > saltBytes             [Hex] = " + Hex.toHexString(saltBytes));

		// 2019.06.19 Add : wPuK(65Bytes)+Hmac(32Bytes)+wSalt(32Bytes) = wPuk_Hamc_wSalt(129Bytes) ---> B64Str(wPuk_Hamc_wSalt) = 172Bytes String 
		String inputWPswValidityCheckSF = null;
		byte[] existHmacEwPukBytes = Base64.decode(existHmacEwPukBase64Str);
		byte[] newHmacEwPukBytes = dcAPI.HmacDigest(hmacAlg, firstEncryptWPukBytes, pswBytes, "BC");
		System.out.println("  > newHmacEwPukBytes     [Hex] = " + Hex.toHexString(newHmacEwPukBytes));
		if (!Arrays.equals(existHmacEwPukBytes, newHmacEwPukBytes)) {		// wPSW Validity Verification Failure for BCWallet Restore
			System.out.println(" >>[!] BC-Wallet Restore wPSW Check Fail. ---> Blockchain Restore Process Stop!");
			inputWPswValidityCheckSF = "0x509F";
			return  inputWPswValidityCheckSF; 
    	} else {															// wPSW Validity Verification Success for BCWallet Restore
    		inputWPswValidityCheckSF = "0x509E";	
		}

		//--------[ 1nd Decrypt Procedure : wPuk 1st Decrypt (=Android)
		byte[] firstDecryptWPukBytes = pbkdf2API.PBKDF2baseEncrptAndDecrypt(firstEncryptWPukBytes, "DECRYPT", passwordStr, saltBytes, iterationCount, 48*8);
		System.out.println("  > firstDecryptWPukBytes [Hex] = " + Hex.toHexString(firstDecryptWPukBytes));
		
		
		//--------[ 2nd Encrypt Procedure : wPrk 2nd Encrypt (Android : wPrk 2nd Encrypt using TEE RSA Public Key)
    	byte[] secondEncryptWPrkBytes = PBESecurityAPI.pbeEncrypt(firstEncryptWPrkBytes, passwordStr.toCharArray(), saltBytes, iterationCount);
		//System.out.println("  > 2nd Encrypted Private Key [Byte] = " + Hex.toHexString(secondEncryptWPrkBytes));
		
		//--------[ Wallet Addr Gen Procedure
		//System.out.println("------------------------------------------------------------------------------------- ");
		byte[] ethWalletAddrBytes = EthereumWalletAddrGen(bcECDSAHashAlgParam, firstDecryptWPukBytes);		 
		String moaWalletAddrBase58Str = base58.encode(ethWalletAddrBytes);
		//System.out.println("  > Eth Wallet Addr [Last 20Bytes] = " + Hex.toHexString(ethWalletAddrBytes));
		//System.out.println("  > Eth Wallet Addr [MoaBase58]    = " + moaWalletAddrBase58Str);
		
		//--------[ Wallet Data Type Conversion
		String saltBase58Str = MoaBase58.encode(saltBytes);
		String cipheredBase58Str = MoaBase58.encode(secondEncryptWPrkBytes);
		String walletPublicKeyBase58Str = MoaBase58.encode(firstDecryptWPukBytes);

		//--------[ moaWallet.dat Hmac Key Gen
		byte[] saltConcatPswBytes = new byte[saltBytes.length + pswBytes.length];
		System.arraycopy(saltBytes, 0, saltConcatPswBytes, 0, saltBytes.length);
		System.arraycopy(pswBytes, 0, saltConcatPswBytes, saltBytes.length, pswBytes.length);
		pswBytes = new byte[1];  // Password Bytes delete in Memory
		
		byte[] hmacKey = dcAPI.HashDigest(bcECDSAHashAlgParam, saltConcatPswBytes, "BC");
		//System.out.println("  > Hmac Key [Byte : " + hmacKey.length + " ] = " + Hex.toHexString(hmacKey));
		
		String osName = System.getProperty("os.name");
		String walletConcatDataStr = versionInfo + osName + walletTypeStr + saltBase58Str + iterationCount + cipheredBase58Str + walletPublicKeyBase58Str + moaWalletAddrBase58Str;
		byte[] macDataBytes = dcAPI.HmacDigest(hmacAlg, walletConcatDataStr.getBytes("UTF-8"), hmacKey, "BC");
		String macDataBase58Str = MoaBase58.encode(macDataBytes);
		
		//--------[ HmacKey delete in Memory 
		hmacKey = new byte[1];
		
		//--------[ moaWallet.dat File Gen
		String walletFileStorage = BlockchainWalletInfoGen(walletTypeStr, saltBase58Str, iterationCount, cipheredBase58Str, walletPublicKeyBase58Str, moaWalletAddrBase58Str, macDataBase58Str); 
		BlockchainWalletFileStorage(walletFileStorage, restoreWalletFileName, false);
		
		// 2019.06.19 Add
		return inputWPswValidityCheckSF;		 
		
    }
    
    
    // 2019.07.16 Add
    @SuppressWarnings("static-access")
	private static byte[] BlockchainTransactionSign(String alias, String trasctionDataStr, String passwordStr, String restoreFlagStr)
    	   throws NullPointerException, Exception 
    {

    	//--------[ moaWallet.dat File Load & Field Get 
    	File walletFile = null; 
    	switch( restoreFlagStr ) {
    		case "RESTORE" : 	// Restore Wallet File Setting
    			walletFile = new File(restoreWalletFileName);		// ex) Restore walletFilePath = "E:\\99_JavaWork\\MoaWalletLib\\"
    			break;
    			
    		case "NON" :	// Non-Restore Wallet File Setting
    			walletFile = new File(walletFileName);				// ex) walletFilePath = "E:\\99_JavaWork\\MoaWalletLib\\"
    			break;
    			
    		case "TEMP" :
    			walletFile = new File(tempRestoreWalletFileName);	// ex) Temp walletFilePath = "E:\\99_JavaWork\\MoaWalletLib\\"
    			break;
    			
    		default :
    			break;
    	}
    	
    	/*if (restoreFlag == true) {		// Restore Wallet File Setting
    		walletFile = new File(restoreWalletFileName);	// ex) Original walletFilePath = "E:\\99_JavaWork\\MoaWalletLib\\"
    	} else {						// Non-Restore Wallet File Setting
    		walletFile = new File(walletFileName);			// ex) Original walletFilePath = "E:\\99_JavaWork\\MoaWalletLib\\"
    	}*/

    	Path walletFilePath = Paths.get(walletFile.getAbsolutePath());
    	FileInputStream fis = new FileInputStream(walletFilePath.toString());

    	Properties prop = new Properties();
    	prop.load(fis);
    	int versionInfo = Integer.parseInt(prop.getProperty("Version.Info"));
    	String osInfo = prop.getProperty("OS.Info").trim();
    	String walletTypeStr = prop.getProperty("Wallet.Type").trim();
    	String saltBase58Str = prop.getProperty("Salt.Value").trim();
    	int iterationCount = Integer.parseInt(prop.getProperty("Iteration.Count"));
    	String cipheredDataBase58Str = prop.getProperty("Ciphered.Data").trim();
    	String walletPublicKeyBase58Str = prop.getProperty("Wallet.PublicKey").trim();
    	String walletAddrBase58Str = prop.getProperty("Wallet.Addr").trim();
    	String macDataBase58Str = prop.getProperty("MAC.Data").trim();
        
    	fis.close();	// 2019.07.11 Add(FileInputStream Close)
    	prop = null;	// 2019.07.11 Add(Release Object) 
    	
    	// System.out.println("  > macDataBase58Str : " + macDataBase58Str);
    	String walletConcatData = versionInfo + osInfo + walletTypeStr + saltBase58Str + iterationCount + 
    							  cipheredDataBase58Str + walletPublicKeyBase58Str + walletAddrBase58Str;

    	//--------[ 32Byte(256bit) Key Gen for Hmac using HashDigest Alg(SHA256 or SHA512)
    	byte[] saltBytes = MoaBase58.decode(saltBase58Str);
    	byte[] pswBytes = passwordStr.getBytes(charset);
		byte[] saltConcatPswBytes = new byte[saltBytes.length + pswBytes.length];
		System.arraycopy(saltBytes, 0, saltConcatPswBytes, 0, saltBytes.length);
		System.arraycopy(pswBytes, 0, saltConcatPswBytes, saltBytes.length, pswBytes.length);
		pswBytes = new byte[1];  // Password Bytes delete in Memory
		
		byte[] hmacKey = dcAPI.HashDigest(bcECDSAHashAlgParam, saltConcatPswBytes, "BC");
		// System.out.println("  > Hmac Key [Byte : " + hmacKey.length + " ] = " + Hex.toHexString(hmacKey));

		//--------[ Hmac Gen for moaWallet data using hmacKey
		byte[] macDataBytes = DigestCoreAPI.HmacDigest(hmacAlg, walletConcatData.getBytes("UTF-8"), hmacKey, "BC");
		String newMacDataBase58Str = MoaBase58.encode(macDataBytes);
		System.out.println("  > newMacDataBase58Str : " + newMacDataBase58Str);
		System.out.println("  > macDataBase58Str    : " + macDataBase58Str);
		//--------[ HmacKey delete in Memory 
		hmacKey = new byte[1];
		
		//--------[ moaWallet.dat Integrity Check
		byte[] successFail = "0x5201".getBytes();
		if ( !macDataBase58Str.equals(newMacDataBase58Str) ) {
			System.out.println("  >>[!] Integrity Verify Fail for Blockchain moaWallet Data. --> Transaction Signature Progress Stop!.");
			return successFail;
		} 
    	
		//--------[ Decrypt Procedure : Double(2nd -> 1st) Decrypted
		byte[] encryptedPrivateKeyBytes = MoaBase58.decode(cipheredDataBase58Str);
		byte[] decryptPrivateKeyBytes = BlockchainDecryptPrk(encryptedPrivateKeyBytes, passwordStr, MoaBase58.decode(saltBase58Str), iterationCount);
		//System.out.println("  > decryptPrivateKeyBytes [Byte : " + decryptPrivateKeyBytes.length + " ] = " + Hex.toHexString(decryptPrivateKeyBytes));
		
		byte[] trasctionDataBytes = trasctionDataStr.getBytes("UTF-8");	
    	byte[] signatureBytes = bcECDSA.blockchainECDSASign(trasctionDataBytes, decryptPrivateKeyBytes); // Signature
    	// System.out.println("  > signature [Byte] = " + signatureBytes.length + " / " +	Hex.toHexString(signatureBytes));
    	// BigInteger r = new BigInteger(1, signatureBytes, 0, 32);
        // BigInteger s = new BigInteger(1, signatureBytes, 32, 32);
    	
    	// password & Private Key Memory Delete
    	passwordStr = "";
		decryptPrivateKeyBytes = new byte[1];
		// System.out.println("  > Deleted Private Key [Byte] = " + Hex.toHexString(decryptPrivateKeyBytes));
		
    	
		// return of Sign Value for transaction data
    	return signatureBytes;
    }

    
    /**
     * ECDSA PrivateKey[=WPrk])로 서명한 거래 데이터를 PublicKey[=WPuk]를 사용하여 검증하고 검증 결과(True or False)로
     * 반환한다.
	 * @param trasctionDataBytes WPuk로 검증할 거래 데이터 바이트 배열 입력
	 * @param signatureBytes WPrk로 서명한 바이트 배열 입력 
	 * @param publicKeyBytes WPrk로 서명한 거래 데이터를 검증하기 위한 WPuk 바이트 배열 입력
	 * @return boolean 서명에 대한 검증 결과 정보(True or False)를 반환 
     */
    private static boolean BlockchainTransactionVerify(byte[] trasctionDataBytes, byte[] signatureBytes, byte[] publicKeyBytes) 
    {
    	boolean verifySuccessAndFail = false;
    	verifySuccessAndFail = bcECDSA.blockchainECDSAVerify(trasctionDataBytes, signatureBytes, publicKeyBytes); // Verify
    	//System.out.println("verify result [True/False] = " + verifySuccessAndFail);
    	
    	return verifySuccessAndFail;
    }

    
    /**
     * WPrk를 패스워드 기반 PBE로 1차 암호화하고 패스워드 기반 HKDF로 2차 암호화 결과 바이트 배열을 반환한다.
	 * @param privateKeyByte 2중 암호화된 WPrk를 바이트 배열로 입력 
	 * @param passwordStr (1st)패스워드 기반 PPBKDF2+AES 암호 연산 및 (2nd)패스워드 PBE(or HKDF) 기반 암호 연산을  위한 패스워드  문자열 입력
	 * @param saltByte 1st의 암호화 및 2nd 키 생성 시 복잡도 증가를 위하여 생성된 난수 바이트 배열을 입력 
	 * @param iterationCount 1st의 암호화 및 2nd 키 생성 시 복잡도 증가를 위하여 반복 계산 횟수를 입력
	 * @return byte[] 이중 암호화된 WPrk 바이트 배열을 반환 
     * @throws NullPointerException 파라미터 널 입력 오류
     * @throws Exception 일반적인 예외 오류
     */
    @SuppressWarnings("static-access")
	private static byte[] BlockchainEncryptPrk(byte[] privateKeyByte, String passwordStr, byte[] saltByte, int iterationCount) 
    		throws NullPointerException, Exception
    {		
    	
    	byte[] firstEncryptPrivateKeyBytes = pbkdf2API.PBKDF2baseEncrptAndDecrypt(privateKeyByte, "ENCRYPT", passwordStr, saltByte, iterationCount, 48*8);
    	//System.out.println("  > 1st Encrypted Private Key [Byte] = " + Hex.toHexString(firstEncryptPrivateKeyBytes));
    	
    	byte[] secondEncryptPrivateKeyBytes = PBESecurityAPI.pbeEncrypt(firstEncryptPrivateKeyBytes, passwordStr.toCharArray(), saltByte, iterationCount);
		//System.out.println("  > 2nd Encrypted Private Key [Byte] = " + Hex.toHexString(secondEncryptPrivateKeyBytes));

    	return secondEncryptPrivateKeyBytes;
    	
    }

    /**
     * 이중 암호화되어 있는 WPrk를 패스워드 기반 HKDF로 2차 복호화하고 패스워드 기반 PBE로 1차 복호화한 WPrk 결과 바이트 배열을 반환한다.
	 * @param cipherTextBytes moaWallet.dat 파일 내의 이중 암호화된 WPrk 바이트 배열로 입력 
	 * @param passwordStr (2nd)패스워드 HKDF 기반 키 생성 및 (1st)패스워드 기반 PBE 복호화 연산을 위한 패스워드  문자열 입력
	 * @param saltByte 1st의 암호화 및 2nd 키 생성 시 복잡도 증가를 위하여 생성된 난수 바이트 배열을 입력 
	 * @param iterationCount 1st의 암호화 및 2nd 키 생성 시 복잡도 증가를 위하여 반복 계산 횟수를 입력
	 * @return byte[] 복호화된 WPrk 바이트 배열을 반환 
     * @throws NullPointerException 파라미터 널 입력 오류
     * @throws Exception 일반적인 예외 오류
     */
    @SuppressWarnings("static-access")
	private static byte[] BlockchainDecryptPrk(byte[] cipherTextBytes, String passwordStr, byte[] saltByte, int iterationCount) 
				   throws NullPointerException, Exception 
    { 
    	
    	byte[] secondDecryptPrivateKeyBytes = PBESecurityAPI.pbeDecrypt(cipherTextBytes, passwordStr.toCharArray(), saltByte, iterationCount);
    	//System.out.println("  > 2nd Decrypted Private Key [Byte] = " + Hex.toHexString(secondDecryptPrivateKeyBytes));
    		
    	byte[] firstDecryptPrivateKeyBytes = pbkdf2API.PBKDF2baseEncrptAndDecrypt(secondDecryptPrivateKeyBytes, "DECRYPT", passwordStr, saltByte, iterationCount, 48*8);
    	//System.out.println("  > 1st Decrypted Private Key [Byte] = " + Hex.toHexString(firstDecryptPrivateKeyBytes));
        	
    	passwordStr = "";			// Password delete in Memory
    	
    	return firstDecryptPrivateKeyBytes;
    	
    }

	
    /**
     * Wallet 정보를 생성한다.
     * @param walletTypeStr  비복원형 및 복원형 Wallet 생성 구분자 문자열("R" or "NonR") 데이터 입력 
	 * @param saltBase58Str  moaWallet.dat or moaWalletRestore.dat에 저장할 MoaBase58 salt 문자열 데이터 입력 
	 * @param InterationCount  moaWallet.dat or moaWalletRestore.dat에 저장할 반복 연산 횟수 숫자 데이터 입력 
	 * @param ivBase58Str  moaWallet.dat or moaWalletRestore.dat에 저장할 2차 암호화에 사용할 MoaBase58 iv 문자열 데이터 입력 
	 * @param cipheredDataBase58Str  moaWallet.dat or moaWalletRestore.dat에 저장할 이중 암호화된 MoaBase58 cipheredData 문자열 데이터 입력 
	 * @param walletPublicKeyBase58Str 	moaWallet.dat or moaWalletRestore.dat에 저장할 MoaBase58 WPuk 문자열 데이터 입력 
	 * @param walletAddrBase58Str  moaWallet.dat or moaWalletRestore.dat에 저장할 MoaBase58 WAddr 문자열 데이터 입력 
	 * @param macDataBase58Str  moaWallet.dat or moaWalletRestore.dat에 저장할 MoaBase58 mac 문자열 데이터 입력 
	 * @return String  moaWallet.dat or moaWalletRestore.dat 파일에 저장할 문자열 데이터 반환 
     */
	private static String BlockchainWalletInfoGen(String walletTypeStr, String saltBase58Str, int InterationCount, String cipheredDataBase58Str, 
												  String walletPublicKeyBase58Str, String walletAddrBase58Str, String macDataBase58Str)
	{
		
		String line = System.getProperty("line.separator");		// Word rap String = '\n' or '\r\n'
		String osName = System.getProperty("os.name");			// OS Name = Windows 10 or Linux xx
		//System.out.println("[**] OS Name(문자열) : " + osName); 
		
		StringBuffer sb = new StringBuffer();
	
		sb.append("Version.Info="); 	sb.append(versionInfo); 				sb.append(line);	// Version Info(String)
		sb.append("OS.Info="); 			sb.append(osName); 						sb.append(line);	// OS Name(String)
		sb.append("Wallet.Type="); 		sb.append(walletTypeStr); 				sb.append(line);	// Wallet Type(String) 2019.04.29 Add
		sb.append("Salt.Value="); 		sb.append(saltBase58Str); 				sb.append(line);	// Salt Value(String)
		sb.append("Iteration.Count="); 	sb.append(InterationCount); 			sb.append(line);	// Interation.Count(String)
		sb.append("Ciphered.Data="); 	sb.append(cipheredDataBase58Str); 		sb.append(line);	// Ciphered Data(String)
		sb.append("Wallet.PublicKey="); sb.append(walletPublicKeyBase58Str); 	sb.append(line);	// Wallet Public Key(String)
		sb.append("Wallet.Addr="); 		sb.append(walletAddrBase58Str); 		sb.append(line);	// Wallet Addr(String)
		sb.append("MAC.Data="); 		sb.append(macDataBase58Str); 								// MAC Data(String)
		
		return sb.toString();

	}

	
	
    /**
     * Wallet 정보를 생성하여 moaWallet.dat에 저장한다.
	 * @param storageWalletDataStr moaWallet.dat에 저장할 문자열 데이터 입력 
	 * @param walletFileName - 저장할 BC-Wallet 파일명을 입력 
	 * @param fileWritePermitFlag - 파일 저장 권한 유무 플래그 정보를 입력
	 * @return 없음 
	 * @throws Exception 일반적인 예외 오류
     */
	private static void BlockchainWalletFileStorage(String storageWalletDataStr, String walletFileName, boolean fileWritePermitFlag) throws IOException
	{
		
		File moaWalletFile = new File(walletFileName);
		Path walletFilePath = Paths.get(moaWalletFile.getAbsolutePath());		// 2019.05.07 Add & Modified
		
		FileWriter moaWalletFileWriter = null;

		try {
			
			// 2019.06.14 Add & Modified
			if (fileWritePermitFlag == false) {			// 	File Write not Permit : False

				//-----[ if 'moaWallet.dat' file already exists, file creation filed! --> Stop ] 
				if (moaWalletFile.exists()) {
					throw new FileAlreadyExistsException( walletFilePath + " File already exists. File creation failed!");
				} else {
					// System.out.println(walletFileName + " Path : " + walletFilePath.toString());
					moaWalletFileWriter = new FileWriter(walletFilePath.toString(), false);			// 2019.05.07 Add & Modified
					moaWalletFileWriter.write(storageWalletDataStr);
					moaWalletFileWriter.flush();
				}
				
			} else if (fileWritePermitFlag == true) {	// File Write Permit : True

				moaWalletFileWriter = new FileWriter(walletFilePath.toString(), false);			
				moaWalletFileWriter.write(storageWalletDataStr);
				moaWalletFileWriter.flush();
				System.out.println(walletFileName + " Path : " + walletFilePath.toString());
				
			}
			
			//moaWalletFileWriter.close();

		} catch(IOException  e) {
			e.printStackTrace();
			System.exit(0);
		} finally {
			try {
				
				if(moaWalletFileWriter != null)		moaWalletFileWriter.close();
			
			} catch (IOException e) {}
		}
		
	} 

	
	// 2019.07.08 Add
    /**
     * BC-Wallet wPsw ReSet 절차에서 올바르지 않게 생성된 "moaWalletRestore.dat" 파일을 삭제하는 기능입니다. 
	 * @param bcWalletFileName "moaWallet.dat" 파일명 문자열 데이터 입력 
	 * @param deleteBCWalletFlag - 파일 제거 식별자 플래그 정보 
	 * @return 없음 
	 * @throws Exception 일반적인 예외 오류
     */
	private static void BlockchainWalletFileDelete(String bcWalletFileName, String deleteBCWalletFlag) 
				   throws IOException
	{
		
		File moaWalletFile = new File(bcWalletFileName);
		Path walletFilePath = Paths.get(moaWalletFile.getAbsolutePath());
		File fullWalletFilePath = walletFilePath.toFile();

		// [BCWallet wPsw Reset Progress] The Correct BCWallet Data Creation Fail in Android
		if ( deleteBCWalletFlag.equals("0x5096") ) {	

			if ( fullWalletFilePath.exists() ) {
				if ( fullWalletFilePath.delete() )	{
					System.out.println("FilePathAndName : " + walletFilePath + " / File Delete SF : " + true); 
					moaWalletFile = null;		// 2019.07.11 Add(Release of File Object) 
				} else {
					System.out.println("FilePathAndName : " + walletFilePath + " / File Delete SF : " + false);
					// System.exit(1);
				}
			} else {
				System.err.println("I cannot find " + moaWalletFile + " [" + moaWalletFile.getAbsolutePath() + "]"); 
			}
		
		}
		
	} 
	
	@SuppressWarnings("unused")
	private static void BlockchainWalletFileUpdate()
	{
	
		FileInputStream fis = null;
		//FileOutputStream fos = null;

		try {
	
			File tempBCWalletFile = new File(tempRestoreWalletFileName);
			Path tempBCWalletFilePath = Paths.get(tempBCWalletFile.getAbsolutePath());
			
	    	fis = new FileInputStream(tempBCWalletFilePath.toString());
	    			    
	    	Properties tempProp = new Properties();
	    	tempProp.load(fis);
	    	int versionInfoStr 	 = Integer.parseInt(tempProp.getProperty("Version.Info"));
	    	String osInfoStr 	 = tempProp.getProperty("OS.Info");
	    	String walletTypeStr = tempProp.getProperty("Wallet.Type");
	    	String saltBase58Str = tempProp.getProperty("Salt.Value");
	    	int iterationCount 	 = Integer.parseInt(tempProp.getProperty("Iteration.Count"));
	    	String cipheredDataBase58Str = tempProp.getProperty("Ciphered.Data");
	    	String wPukBase58Str = tempProp.getProperty("Wallet.PublicKey");
	    	String wAddr58Str 	 = tempProp.getProperty("Wallet.Addr");
	    	String wMACData58Str = tempProp.getProperty("MAC.Data");

	    	// fis.close(); 		// FileInputStream Close
	    	tempProp = null;	// Release of Properties Object
	    	
	    /*	//---------------------------------------------------------------------------
			File restoreBCWalletFile = new File(restoreWalletFileName);
			Path restoreBCWalletFilePath = Paths.get(restoreBCWalletFile.getAbsolutePath());
			
	    	fos = new FileOutputStream(restoreBCWalletFilePath.toString());
	    			    
	    	Properties restoreProp = new Properties();
	    	// restoreProp.load(fos);
	    	restoreProp.setProperty("Version.Info", Integer.toString(versionInfoStr));
	    	restoreProp.setProperty("OS.Info", osInfoStr);
	    	restoreProp.setProperty("Wallet.Type", walletTypeStr);
	    	restoreProp.setProperty("Salt.Value", saltBase58Str);
	    	restoreProp.setProperty("Iteration.Count", Integer.toString(iterationCount));
	    	restoreProp.setProperty("Ciphered.Data", cipheredDataBase58Str);
	    	restoreProp.setProperty("Wallet.PublicKey", wPukBase58Str);
	    	restoreProp.setProperty("Wallet.Addr", wAddr58Str);
	    	restoreProp.setProperty("MAC.Data", wMACData58Str);
	    	restoreProp.store(fos, null);

	    	//fos.close(); 		// FileOutputStream Close
	    	restoreProp = null;	// Release of Properties Object
	    */	
	    	
	    	String bcWalletDataGenStr = BlockchainWalletInfoGen(walletTypeStr, saltBase58Str, iterationCount, 
	    														cipheredDataBase58Str, wPukBase58Str, wAddr58Str, wMACData58Str);
	    		
	    	BlockchainWalletFileStorage(bcWalletDataGenStr, restoreWalletFileName, true);
	
		} catch(FileNotFoundException fe) {
		} catch(IOException e) {
		} finally {
			try {
				// 2019.07.11 Add(FileInputStream Close)
				if (fis != null) fis.close(); 
				//if (fos != null) fos.close(); 	
			} catch (IOException e) {}	
		}

	}

	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 2019.06.12 Add : BC-Wallet wPSW ReSet --------------------------------------------------------------------->
	/** [복원형 Wallet wPSW ReSet]
     * Ethereum Wallet Psw[=wPsw]을 입력하여 HmacWPsw14Bytes로 변형한 후, 이 변형한 값에 난수 1Bytes를 연접하여 DBK로 암호화하고 리 결과 값에 <br>
     * 대한  Hmac 값을 생성하여 연접한 후  Base64Str(HmacWPsw14Bytes)와 Base64Str(EncryptHamcPsw16Bytes||Hmac16Bytes) 데이터를 <br>
     * StringTokenizer 문자('$')로 연접한 결과 값을 생성한다. <br>
	 * @param idStr 고객(사용자)의 Member-ID 문자열을 입력  
	 * @param wPswStr 고객(사용자)의 변형된 패스워드[HmacWPsw14Bytes]를 생성하기 위한 월릿용 패스워드 문자열 입력 
	 * @param dateOfBirthStr PBKDF2 기반 키 유도와 유도된 키를 사용하여 Rand1||HmacWPsw14Bytes을 암호화 하기 위한 생년월일[yyyymmdd] 8자리 문자열 입력
	 * @return hmacWPsw14B64$Ehmac16ConcatMacB64str hmacWPsw14B64str 및 Ehmac16ConcatMacB64str 연접한 문자열 반환 
     * @throws NullPointerException 파라미터 널 입력 오류
     * @throws Exception 일반적인 예외 오류
     */
	@SuppressWarnings("static-access")
	private static String BCWalletEncryptWPswGenCoreEngine(String idStr, String wPswStr, String dateOfBirthStr) 
				   throws NullPointerException, Exception 
	{
		
		//--------[ Setting of Random Number Size for encrypt of HmacWPsw --> int randSize = 1;
		byte[] idSaltBytes = idStr.getBytes(charset);
		byte[] dateOfBirthBytes = dateOfBirthStr.getBytes(charset);
		
		//--------[ moaWallet wPsw Encrypt Key[=DBK] Gen using dateOfBirthStr and idStr
		byte[] dk = pbkdf2API.PBKDF2KeyGen(dateOfBirthStr, idSaltBytes, iterationCountforDBK, 48*8);
		byte[] dbkBytes = Arrays.copyOf(dk, 32);
		byte[] dbkIVBytes  = Arrays.copyOfRange(dk, 32, dk.length);
		System.out.println("[0x] dk Gen using wPsw anf DoB[YYYYMMDD] (Hex) : " + Hex.toHexString(dk));
		//System.out.println("[0x] Derivated PSW Encrypt Key form YYYYMMDD(Hex) : " + Hex.toHexString(dbkBytes));
		
		sc = new SymmetricCoreAPI(cipherAlgforWPsw, dbkIVBytes, dbkBytes, "BC");
		
		dbkBytes   = new byte[1]; 	// dbkBytes clear in memory
		dbkIVBytes = new byte[1];	// dbkIVBytes clear in memory
		
		// 2019.07.16 Add
		byte[] hashMoapayPswBytes = dcAPI.HashDigest("SHA256", wPswStr.getBytes(charset), "BC");
		
		byte[] hmacBCWPsw14Bytes = BCWalletHmacWPswGenCoreEngine(wPswStr);
		byte[] randBytes = srng.SecureRandNumberGen(randSize);
		// System.out.println("[0x] Randomnember (Hex): " + Hex.toHexString(randBytes));
		
		//---------[ Edbk(RAND1||HmacWPsw14)||MAC16
		byte[] randConcatHMACwPsw14Bytes = new byte[randBytes.length + hmacBCWPsw14Bytes.length];
		System.arraycopy(randBytes, 0, randConcatHMACwPsw14Bytes, 0, randBytes.length);
		System.arraycopy(hmacBCWPsw14Bytes, 0, randConcatHMACwPsw14Bytes, randBytes.length, hmacBCWPsw14Bytes.length);
		// System.out.println("[0x] randConcatHashPsw14Bytes(16진수): " + Hex.toHexString(randConcatHMACwPsw14Bytes) + " [길이] : " + randConcatHMACwPsw14Bytes.length);
		
		byte[] encryptRandAndHMACwPsw14Bytes = sc.SymmetricEncryptData(randConcatHMACwPsw14Bytes); // 암호화 테스트 
		// System.out.println("[0x] 암호문(16진수): " + Hex.toHexString(encryptRandAndHMACwPsw14Bytes) + " [길이] : " + encryptRandAndHMACwPsw14Bytes.length);

		byte[] hmac32forEwPsw16Bytes = dcAPI.HmacDigest("HmacSHA256", encryptRandAndHMACwPsw14Bytes, dateOfBirthBytes, "BC");
		byte[] hmac16Bytes = new byte[hmac32forEwPsw16Bytes.length/2];
		for(int i=0; i < hmac32forEwPsw16Bytes.length/2; i++) {
			hmac16Bytes[i] = (byte) (hmac32forEwPsw16Bytes[i] ^ hmac32forEwPsw16Bytes[i+16]);
		}
		// System.out.println("[0x] hmacforEwPsw16Bytes(Hex): " + Hex.toHexString(hmacforEwPsw16Bytes) + " [길이] : " + hmacforEwPsw16Bytes.length);
		// System.out.println("[0x] macBytes(Hex): " + Hex.toHexString(macBytes) + " [길이] : " + macBytes.length);
		
		byte[] encryptHmacBCWPsw16ConcatHmac16Bytes = new byte[encryptRandAndHMACwPsw14Bytes.length + hmac16Bytes.length];	// byte[] = Edbk(RAND1||HmacWPsw14)||MAC16
		System.arraycopy(encryptRandAndHMACwPsw14Bytes, 0, encryptHmacBCWPsw16ConcatHmac16Bytes, 0, encryptRandAndHMACwPsw14Bytes.length);
		System.arraycopy(hmac16Bytes, 0, encryptHmacBCWPsw16ConcatHmac16Bytes, encryptRandAndHMACwPsw14Bytes.length, hmac16Bytes.length);
		
		// 2019.07.16 Modified
		String hashMoapayPswB64$hmacWPsw14B64$Ehmac16Concathmac16B64str = Base64.toBase64String(hashMoapayPswBytes) + "$" + 
																		 Base64.toBase64String(hmacBCWPsw14Bytes) + "$" + Base64.toBase64String(encryptHmacBCWPsw16ConcatHmac16Bytes) ;
		
		return hashMoapayPswB64$hmacWPsw14B64$Ehmac16Concathmac16B64str;		
		
	}	
	
	
	
	@SuppressWarnings({ "static-access" })
	private static byte[] BCWalletHmacWPswGenCoreEngine(String wPswStr)
			throws GeneralSecurityException
	{
		//---------[ // Input wPsw to wPswBytes change
		byte[] wPswBytes   = wPswStr.getBytes(charset); 
		
		//---------[ // Hmac(wPsw, H(wPsw))
		byte[] hashWPswBytes = dcAPI.HashDigest("SHA256", wPswBytes, "BC");		
		byte[] hmacWPswBytes = dcAPI.HmacDigest("HmacSHA256", wPswBytes, hashWPswBytes, "BC");	
		//System.out.println("[0x] hmacWPswBytes(Hex): " + Hex.toHexString(hmacWPswBytes));

		//---------[ // 14Bytes Extract form Hmac(wPsw, H(wPsw)) : //--[ Setting of hmacWPsw Size for encrypt HmacWPsw
		byte[] hmacWPsw14Bytes = new byte[hmacWPswExtractSize];	   //--[ --> int hmacWPswExtractSize = 14	
		if ( (hashWPswBytes[0] % 2) == 0 ) {						
			System.arraycopy(hmacWPswBytes, hmacWPswExtractSize, hmacWPsw14Bytes, 0, hmacWPswExtractSize);
			// System.out.println("[0x] hmacWPsw14Bytes(Even): " + Hex.toHexString(hmacWPsw14Bytes));
		} else {
			System.arraycopy(hmacWPswBytes,  0, hmacWPsw14Bytes, 0, hmacWPswExtractSize);
			// System.out.println("[0x] hmacWPsw14Bytes(Odd) : " + Hex.toHexString(hmacWPsw14Bytes));
		}
		// System.out.println("[0x] hmacWPsw14Bytes(B64Str): " + Base64.toBase64String(hmacWPsw14Bytes));
		
		return hmacWPsw14Bytes;
	}
	
	
	// 2019.06.05 Add : Validity Check about Date of Birth and Decrypt HmacWPsw for wPsw ReSet of the BC-Wallet
	@SuppressWarnings({ "static-access" })
	private static String BCWalletWPswVerifyAndDecryptCoreEngine(String idStr, String dateOfBirthStr, String encryptHmacWPswAndMACB64str)
				   throws NullPointerException, Exception 
	{
		
		byte[] idSaltBytes = idStr.getBytes(charset);
		//byte[] resetWPswBytes   = reSetwPswStr.getBytes(charset); 
		byte[] userDateOfBirthBytes = dateOfBirthStr.getBytes(charset);
		
		//--------[ Exist Encrypted bcwPSW Parsing
		byte[] existEncryptedHmacWPswAndMACBytes = Base64.decode(encryptHmacWPswAndMACB64str);
		
		byte[] existEncHmacWPswBytes = new byte[existEncryptedHmacWPswAndMACBytes.length/2];
		System.arraycopy(existEncryptedHmacWPswAndMACBytes, 0, existEncHmacWPswBytes, 0, existEncHmacWPswBytes.length);
		byte[] existHmac16Bytes = new byte[existEncryptedHmacWPswAndMACBytes.length/2];
		System.arraycopy(existEncryptedHmacWPswAndMACBytes, existEncHmacWPswBytes.length, existHmac16Bytes, 0, existHmac16Bytes.length);
	
		//--------[ New MAC16Bytes Gen 
		byte[] newHmacBytes = dcAPI.HmacDigest("HmacSHA256", existEncHmacWPswBytes, userDateOfBirthBytes, "BC");
		byte[] newHmac16Bytes = new byte[newHmacBytes.length/2];
		for(int i=0; i < newHmacBytes.length/2; i++) {
			newHmac16Bytes[i] = (byte) (newHmacBytes[i] ^ newHmacBytes[i+16]);
		}
		// System.out.println("[0x] newMacBytes(Hex) : " + Hex.toHexString(newHmac16Bytes) + " [길이] : " + newHmac16Bytes.length);
		
		//---------[  Verification of Date of Birth : Compare existMAC and newMAC  
		String hmacwPsw14B64Str = null;
		String dateOfBirthVerifySFStr = null;  
		if (!Arrays.equals(existHmac16Bytes, newHmac16Bytes)) {	// Incorrect date of birth Input (= Verify Fail)
			
			// System.out.println("Entered an invalid date of birth. Please re-enter the correct date of birth!");
			dateOfBirthVerifySFStr = "0x5114";		

		} else {											// Correct date of birth Input (= Verify Success)

			dateOfBirthVerifySFStr = "0x5113";		

			//--------[ moaWallet wPsw Encrypt Key[=DBK] Gen using dateOfBirthStr and idStr
			byte[] dk = pbkdf2API.PBKDF2KeyGen(dateOfBirthStr, idSaltBytes, iterationCountforDBK, 48*8);
			byte[] dbkBytes = Arrays.copyOf(dk, 32);
			byte[] dbkIVBytes  = Arrays.copyOfRange(dk, 32, dk.length);
			System.out.println("[0x] Derivated PSW Encrypt Key form YYYYMMDD(Hex) : " + Hex.toHexString(dbkBytes));
			
			sc = new SymmetricCoreAPI(cipherAlgforWPsw, dbkIVBytes, dbkBytes, "BC");
			
			dbkBytes = new byte[1]; 	// dbkBytes clear in memory
			dbkIVBytes = new byte[1];	// dbkIVBytes clear in memory

			byte[] decRandAndHashPswBytes = sc.SymmetricDecryptData(existEncHmacWPswBytes); 	// Decrypt EncHmacWPsw using DBK 
			// System.out.println("[0x] decRandAndHashPswBytes(Hex): " + Hex.toHexString(decRandAndHashPswBytes) + " [길이] : " + decRandAndHashPswBytes.length);
	
			//--------[ Setting of hmacWPsw and rand Size for encrypt Hmac-wPSW  --> int hmacWPswExtractSize = 14, int randSize = 1
			byte[] hmacwPsw14Bytes = new byte[hmacWPswExtractSize];		// hmacwPsw14Bytes Extract in decRandAndHashPswBytes
			System.arraycopy(decRandAndHashPswBytes, randSize, hmacwPsw14Bytes, 0, hmacwPsw14Bytes.length);
			// System.out.println("[0x] Final HmacwPsw14Bytes(Hex): " + Hex.toHexString(hmacwPsw14Bytes) + " [길이] : " + hmacwPsw14Bytes.length);
			
			hmacwPsw14B64Str = Base64.toBase64String(hmacwPsw14Bytes);
			
		}
		
		String doBVerifySFStr$hmacwPsw14B64Str = dateOfBirthVerifySFStr.concat("$") + hmacwPsw14B64Str;		// 0x5015$hmacwPsw14B64Str
		
		return doBVerifySFStr$hmacwPsw14B64Str; 

	}
	
	
	@SuppressWarnings("static-access") //  2019.06.19 Modified
	private static String BlockchainWalletPswReSetRegistDataGenCoreEngine(String existHmacWPswB64Str, String newHmacReSetWPswB64Str, 
																		  String existHmacWPukB64Str, String existEwPrkB64strAndEwPukB64strAndwSaltB64str) 
				   throws NullPointerException, Exception
	{
		// Input Parameters 'Null' Check    	
    	if (existHmacWPswB64Str == null || newHmacReSetWPswB64Str == null || existEwPrkB64strAndEwPukB64strAndwSaltB64str == null ) {
    		return "  >>[!] Input Parameter Null String. ---> Input Parameter Check!";
    	}
    	
		String existhmacWPsw28Str = Hex.toHexString(Base64.decode(existHmacWPswB64Str));	 	// 
    	
    	// StringTokenizing for existEwPrkB64strAndEwPukB64strAndwSaltB64str 
		StringTokenizer existEwPrkEwPukwSaltST = new StringTokenizer(existEwPrkB64strAndEwPukB64strAndwSaltB64str, "$");
		byte[] firstExistEncryptWPrkBytes = Base64.decode(existEwPrkEwPukwSaltST.nextToken());	// 1st Encrypted wPrk Tokenizer
		byte[] firstExistEncryptWPukBytes = Base64.decode(existEwPrkEwPukwSaltST.nextToken());	// 1st Encrypted wPuk Tokenizer
		byte[] existWSaltBytes  	   	  = Base64.decode(existEwPrkEwPukwSaltST.nextToken());	// wSalt Tokenizer
		
		// 2019.06.19 Add : wPuK(65Bytes)+Hmac(32Bytes)+wSalt(32Bytes) = wPuk_Hamc_wSalt(129Bytes) ---> B64Str(wPuk_Hamc_wSalt) = 172Bytes String 
		String receivedHamcWPswValidityCheckSF = null;
		byte[] existHmacEwPukBytes = Base64.decode(existHmacWPukB64Str);
		byte[] verifyHmacEwPukBytes = dcAPI.HmacDigest(hmacAlg, firstExistEncryptWPukBytes, existhmacWPsw28Str.getBytes(charset), "BC");
		
		if (!Arrays.equals(existHmacEwPukBytes, verifyHmacEwPukBytes)) {	// Received Exist HamcWPsw Validity Verification Failure for BCWallet Restore
			System.out.println(" >>[!] BC-Wallet Exsit wPSW Validity Verify Failure. ---> Blockchain wPSW ReSet Progress Stop!");
			receivedHamcWPswValidityCheckSF = "0x5120";
			return  receivedHamcWPswValidityCheckSF + "%" + "null" + " %" + "null" + "$" + "null" + "$" + "null"; 
    	} else {															// Received Exist HamcWPsw Validity Verification Success for BCWallet Restore
    		receivedHamcWPswValidityCheckSF = "0x5119";	
		}
		
		
		//--------[ (1) Exist-1st Decrypt Procedure : wPrk and wPuk 1st Decrypt (=Android) using EPK[=Exist wPsw Key]
		byte[] firstExistDecryptWPukBytes = pbkdf2API.PBKDF2baseEncrptAndDecrypt(firstExistEncryptWPukBytes, "DECRYPT", existhmacWPsw28Str, existWSaltBytes, iterationCount, 48*8);
		byte[] firstExistDecryptWPrkBytes = pbkdf2API.PBKDF2baseEncrptAndDecrypt(firstExistEncryptWPrkBytes, "DECRYPT", existhmacWPsw28Str, existWSaltBytes, iterationCount, 48*8);
	
		System.out.println("  > firstExistDecryptWPukBytes [Byte] = " + Hex.toHexString(firstExistDecryptWPukBytes)  + " [Lenth] = " + firstExistDecryptWPukBytes.length);
		System.out.println("  > firstExistDecryptWPrkBytes [Byte] = " + Hex.toHexString(firstExistDecryptWPrkBytes)  + " [Lenth] = " + firstExistDecryptWPrkBytes.length);

		//--------[ (2) New Salt Gen
		byte[] newWSaltBytes = srng.SecureRandNumberGen(SaltSize);	// Hash Alg Digest Size

		//--------[ (3) New-1st Encrypt Procedure : wPrk and wPuk 1st Encrypt (=Android) using PRK[=wPsw Reset Key]
		String newHmacReSetWPsw28Str = Hex.toHexString(Base64.decode(newHmacReSetWPswB64Str));	// Binary(14Byte) -> HexString(28Bytes) 
		byte[] firstNewEncryptWPrkBytes = pbkdf2API.PBKDF2baseEncrptAndDecrypt(firstExistDecryptWPrkBytes, "ENCRYPT", newHmacReSetWPsw28Str, newWSaltBytes, iterationCount, 48*8);
		byte[] firstNewEncryptWPukBytes = pbkdf2API.PBKDF2baseEncrptAndDecrypt(firstExistDecryptWPukBytes, "ENCRYPT", newHmacReSetWPsw28Str, newWSaltBytes, iterationCount, 48*8);

		System.out.println("  > New firstNewEncryptWPrkBytes [Byte] = " + Hex.toHexString(firstNewEncryptWPrkBytes)  + " [Lenth] = " + firstNewEncryptWPrkBytes.length);
		System.out.println("  > New firstNewEncryptWPukBytes [Byte] = " + Hex.toHexString(firstNewEncryptWPukBytes)  + " [Lenth] = " + firstNewEncryptWPukBytes.length);

		
		byte[] newHmacEwPukBytes = dcAPI.HmacDigest(hmacAlg, firstNewEncryptWPukBytes, newHmacReSetWPsw28Str.getBytes(charset), "BC");	// newHmacEwPukBytes = Hmac(newEwPukBytes, newReSetHmacWPswBytes)
		
		String decryptWPukB64str  = Base64.toBase64String(firstExistDecryptWPukBytes);
		String newHmacEwPukB64str = Base64.toBase64String(newHmacEwPukBytes);
		String newEwPrkB64str     = Base64.toBase64String(firstNewEncryptWPrkBytes);
		String newEwPukB64str     = Base64.toBase64String(firstNewEncryptWPukBytes);
		String newWSaltB64str     = Base64.toBase64String(newWSaltBytes);
		
		String existHmacWPswValiditySFOrgWPukB64NewHmacEPukB64PerConcatNewEwPrkB64$NewEwPukB64$NewWSaltB64 = receivedHamcWPswValidityCheckSF + "%" + decryptWPukB64str + "%" + newHmacEwPukB64str + "%" +
															  									  			 newEwPrkB64str + "$" + newEwPukB64str + "$" + newWSaltB64str;
		
		return existHmacWPswValiditySFOrgWPukB64NewHmacEPukB64PerConcatNewEwPrkB64$NewEwPukB64$NewWSaltB64;
	}	
	
	
	@SuppressWarnings({ "static-access", "unused" })
	private static void BlockchainWalletPswReSetStorageGenCoreEngine(String hmacReSetWPswB64Str, String orgWPukB64Str, String newEwPrkB64strAndEwPukB64strAndwSaltB64str) 
				   throws GeneralSecurityException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, IOException 
	{ 
		
		String walletTypeStr = "R";
		
		// StringTokenizing for newEwPrkB64strAndEwPukB64strAndwSaltB64str 
		StringTokenizer newEwPrkEwPukwSaltST  = new StringTokenizer(newEwPrkB64strAndEwPukB64strAndwSaltB64str, "$");
		byte[] new1stEncryptWPrkUsingPRKBytes = Base64.decode(newEwPrkEwPukwSaltST.nextToken());	// PRK(New-1st) Encrypted wPrk Tokenizer
		byte[] new1stEncryptWPukUsingPRKBytes = Base64.decode(newEwPrkEwPukwSaltST.nextToken());	// PRK(New-1st) Encrypted wPuk Tokenizer
		byte[] newWSaltBytes  	   			  = Base64.decode(newEwPrkEwPukwSaltST.nextToken());	// New-wSalt Tokenizer

		//--------[ (1) hmacReSetWPswB64Str Type exchange : Bytes -> HexString ->  
		String hmacReSetWPsw28Str = Hex.toHexString(Base64.decode(hmacReSetWPswB64Str));
	   	byte[] hmacReSetWPsw28Bytes = hmacReSetWPsw28Str.getBytes(charset);

		//--------[ (2) New 2nd Encrypt Procedure : wPrk 2nd Encrypt (Android : wPrk 2nd Encrypt using TEE RSA Public Key)
    	byte[] new2ndEncryptWPrkBytes = PBESecurityAPI.pbeEncrypt(new1stEncryptWPrkUsingPRKBytes, hmacReSetWPsw28Str.toCharArray(), newWSaltBytes, iterationCount);
		// System.out.println("  > New-2nd Encrypted for Private Key [Byte] = " + Hex.toHexString(new2ndEncryptWPrkBytes));
		
		//--------[ (3) Wallet Addr Gen Procedure
		// System.out.println("------------------------------------------------------------------------------------- ");
    	byte[] orgWPukBytes = Base64.decode(orgWPukB64Str);
		byte[] ethWalletAddrBytes = EthereumWalletAddrGen(bcECDSAHashAlgParam, orgWPukBytes);		 
		String moaWalletAddrBase58Str = base58.encode(ethWalletAddrBytes);
		// System.out.println("  > Eth Wallet Addr [Last 20Bytes] = " + Hex.toHexString(ethWalletAddrBytes));
		// System.out.println("  > Eth Wallet Addr [MoaBase58]    = " + moaWalletAddrBase58Str);
		
		//--------[ (4) Wallet Data Type Conversion
		String saltBase58Str = MoaBase58.encode(newWSaltBytes);
		String cipheredBase58Str = MoaBase58.encode(new2ndEncryptWPrkBytes);
		String walletPublicKeyBase58Str = MoaBase58.encode(orgWPukBytes);

		//--------[ (5) moaWallet.dat Hmac Key Gen
		// byte[] hmacReSetWPsw14Bytes = Base64.decode(hmacReSetWPswB64Str);	 
		byte[] saltConcatHmacReSetWPswBytes = new byte[newWSaltBytes.length + hmacReSetWPsw28Bytes.length];
		System.arraycopy(newWSaltBytes, 0, saltConcatHmacReSetWPswBytes, 0, newWSaltBytes.length);
		System.arraycopy(hmacReSetWPsw28Bytes, 0, saltConcatHmacReSetWPswBytes, newWSaltBytes.length, hmacReSetWPsw28Bytes.length);
		hmacReSetWPsw28Bytes = new byte[1];		// hmacReSetWPsw28Bytes(Password) Bytes delete in Memory
		
		byte[] hmacKey = dcAPI.HashDigest(bcECDSAHashAlgParam, saltConcatHmacReSetWPswBytes, "BC");
		// System.out.println("  > Hmac Key [Byte : " + hmacKey.length + " ] = " + Hex.toHexString(hmacKey));
		
		String osName = System.getProperty("os.name");
		String walletConcatDataStr = versionInfo + osName + walletTypeStr + saltBase58Str + iterationCount + cipheredBase58Str + walletPublicKeyBase58Str + moaWalletAddrBase58Str;
		byte[] macDataBytes = dcAPI.HmacDigest(hmacAlg, walletConcatDataStr.getBytes("UTF-8"), hmacKey, "BC");
		String macDataBase58Str = MoaBase58.encode(macDataBytes);
		// System.out.println("  > macDataBase58Str [B58] = " + macDataBase58Str);
		
		hmacKey = new byte[1];	//--------[ HmacKey delete in Memory 
		
		//--------[ moaWallet.dat File Gen
		String walletFileStorage = BlockchainWalletInfoGen(walletTypeStr, saltBase58Str, iterationCount, cipheredBase58Str, walletPublicKeyBase58Str, moaWalletAddrBase58Str, macDataBase58Str); 
		
		// BlockchainWalletFileStorage(walletFileStorage, restoreWalletFileName, true);
		
		// 2019.07.16 Add
		BlockchainWalletFileStorage(walletFileStorage, tempRestoreWalletFileName, true);
		System.out.println("  > wPsw Reset wData File Gen Success");

	}
	//-[*] ------------------------------------[ END OF PRIVATE ]---------------------------------------------------- [*]


}	//---[ END of Class    

