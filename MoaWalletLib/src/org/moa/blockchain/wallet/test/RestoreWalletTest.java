package org.moa.blockchain.wallet.test;

import java.nio.charset.Charset;
import java.security.NoSuchProviderException;
import java.security.Security;
//import java.util.StringTokenizer;
import java.util.StringTokenizer;

import org.bouncycastle.util.encoders.*;
import org.moa.blockchain.wallet.coreapi.MoaBase58;
import org.moa.blockchain.wallet.coreapi.MoaWalletAPI;

//import com.sun.org.apache.xml.internal.security.utils.Base64;



public class RestoreWalletTest {
	
	static{

		java.security.Provider p = new org.bouncycastle.jce.provider.BouncyCastleProvider();
		Security.addProvider(p);
	}
	
	static String propertyPathFileName = "E:\\99_JavaWork\\MoaWalletLib\\moaWallet.properties";
	static MoaWalletAPI moaWallet = new MoaWalletAPI(propertyPathFileName);	// ex) eccCurve="secp256r1", hashForSignAlg="SHA256"
	
	static MoaBase58 base58 = new MoaBase58();
	static Charset charset = Charset.forName("UTF-8");
	
	public static void main(String args[]) 
			   throws NoSuchProviderException, NullPointerException, Exception
	{
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//~~~~~~~~~~~~~~~~~~~~~~[moaWallet KeyPair & Addr Issuing for Restore]~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		
		// 1.1 Password Input for Encrypt to the moaWallet KeyPair
		String pswForWalletIssuingStr = "zxcv%$#@!123456";
		//String pswForWalletIssuingStr = "111111";
		String hmacWPswForWalletB64Str = moaWallet.BCWalletHmacWPswGenProcess(pswForWalletIssuingStr);
		String hmacWPswForWalletDataRegistStr = Hex.toHexString(Base64.decode(hmacWPswForWalletB64Str));

		System.out.println("hmacWPswForWalletB64Str : " + hmacWPswForWalletB64Str);
		System.out.println("hmacWPswForWalletDataRegistStr : " + hmacWPswForWalletDataRegistStr);
		
		pswForWalletIssuingStr = ""; 	// Password Delete in Memory
		hmacWPswForWalletB64Str = "";	// hmacWPswForWalletB64Str Delete in Memory
		System.out.println("Deleted Password : " + pswForWalletIssuingStr);
		System.out.println("Deleted hmacWPswForWalletB64Str : " + hmacWPswForWalletB64Str);

		
		// 1.2 confirm identifier for wallet restore requestor

		// 1.3 Perform non-face-to-face authentication process based on mobile phone 
		
		// 1.4 moaWallet Gen for Restore
		// String EwPrkwAndEwPukAndwSaltBase64Str = moaWallet.RestoreBlockchainWalletInfoGenProcess("MoaWalletRestore", pswForWalletIssuingStr); 	// 전송을 위한 월릿 복원 데이터 생성
		//String EwPrkwAndEwPukAndwSaltBase64Str = moaWallet.RestoreBlockchainWalletInfoGenProcess("MoaWalletRestore", hmacWPswForWalletDataRegistStr); 	// 전송을 위한 월릿 복원 데이터 생성
		//System.out.println("Data For AuthServer DB Storage : " + EwPrkwAndEwPukAndwSaltBase64Str);
		
	/*	// 2019.06.19 Modifed
		String hmacWPukB64PerEwPrkB64$EwPukB64$wSaltB64Str = moaWallet.RestoreBlockchainWalletInfoGenProcess("MoaWalletIssuing", hmacWPswForWalletDataRegistStr); // Data to be stored in the AuthDB
		StringTokenizer hmacWPukEwPrkEwPukWSaltST = new StringTokenizer(hmacWPukB64PerEwPrkB64$EwPukB64$wSaltB64Str, "%"); 
		String existHmacWPukB64Str = hmacWPukEwPrkEwPukWSaltST.nextToken();
		String eWPrkwAndEwPukAndwSaltBase64Str = hmacWPukEwPrkEwPukWSaltST.nextToken();
		System.out.println("Data For AuthServer DB Storage : " + hmacWPukB64PerEwPrkB64$EwPukB64$wSaltB64Str);
	*/
		// JavaScript 생성 정보 검증 테스트 용도
		//String hmacWPswB64Str = "+JY4mxNvBoXSn4bzm08=";
		//String encryptHmacWPswB64Str = "XXYYUrf+zx7kLHBOKyKMBLXBAb7O7rL7wluBQlIv478=";
		String encryptedWPrkB64Str = "PZs2RW9jYU2egce6tmBWITYCCAQbO8IddUzrYdgI7LE=";
		String encryptedWPukAndHmacEwPukAndWsalt = "qPxuIfwJRWkF8LZDEUODvXOc35bErsF7aT4L+7laZ3ejLSKNoM/is+ltKYGv3tuTwLKQ0sww+NRs7RU75IHD1YGDgVL5UVJQyRBzikGQf+S6XUeQyOzc+jlqgHxqwa3qQi//RgORwviNtMY/RnCln3Fw4FQxpNyUekR3Md8rpMVz";
		
		
		String hmacEWPukB64StrPerEwPrkB664Str$EwPukB64Str$WSaltB64Str = BCWalletDataParsingAndReassembleGen(encryptedWPrkB64Str, encryptedWPukAndHmacEwPukAndWsalt);
		StringTokenizer hmacWPukAndEwPrkEwPukWSaltST = new StringTokenizer(hmacEWPukB64StrPerEwPrkB664Str$EwPukB64Str$WSaltB64Str, "%"); 
		String existHmacWPukB64Str = hmacWPukAndEwPrkEwPukWSaltST.nextToken();
		String eWPrkwAndEwPukAndwSaltBase64Str = hmacWPukAndEwPrkEwPukWSaltST.nextToken();
		
		
		// 1.5 moaWallet Restore : if AuthDB Regist Success, Running
		// moaWallet.RestoreBlockchainWalletProcess(pswForWalletIssuingStr, EwPrkwAndEwPukAndwSaltBase64Str);			// moaWalletRestore.dat 복원
		// moaWallet.RestoreBlockchainWalletProcess(hmacWPswForWalletDataRegistStr, eWPrkwAndEwPukAndwSaltBase64Str);	// moaWalletRestore.dat 복원	
		// 2019.06.19 Modified
		String wPswValidityCheckSFStr = moaWallet.RestoreBlockchainWalletProcess(hmacWPswForWalletDataRegistStr, existHmacWPukB64Str, eWPrkwAndEwPukAndwSaltBase64Str);
		//String wPswValidityCheckSFStr = moaWallet.RestoreBlockchainWalletProcess("hmacWPswForWalletDataRegistStr", existHmacWPukB64Str, eWPrkwAndEwPukAndwSaltBase64Str);
		if ( wPswValidityCheckSFStr.equals("0x509F")) {
			System.out.println(" >>[!] BC-Wallet Restore wPSW Check Fail. Please Re-Enter the Correct Wallet Password!");
			System.exit(0); 
		}
		
		
		hmacWPswForWalletDataRegistStr = ""; 	// hmacWPswForWalletDataRegistStr Delete in Memory
		System.out.println("Deleted hmacWPswForWalletDataRegistStr : " + hmacWPswForWalletDataRegistStr);

		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//~~~~~~~~~~~~~~~~~~~~~~[moaWallet KeyPair Load, Transaction Sign & Verify]~~~~~~~~~~~~~~~~~~~~~~~~~
		
		// 2.1 Password Input for Decrypt to the WPRK of moaWallet  
		String pswForSignStr = "111111"; // "zxcv%$#@!123456";
		String hmacWPswBase64Str = moaWallet.BCWalletHmacWPswGenProcess(pswForSignStr);
		String hmacWPswForSignStr = Hex.toHexString(Base64.decode(hmacWPswBase64Str));

		pswForSignStr = "";				// Password Delete in Memory
		hmacWPswBase64Str = "";			// hmacWPswBase64Str Delete in Memory
		System.out.println("Deleted pswForSignStr : " + pswForSignStr);
		System.out.println("Deleted hmacWPswBase64Str : " + hmacWPswBase64Str);

		
		// 2.2 Transaction Sign Using WPRK : 거래 데이터 서명
		String transactionForPay = "It is 10000$ payment Alice to Bob";
		byte[] transactionSignBytes = moaWallet.BlockchainTransactionSignProcess("MoaWalletDataBaseSignature", transactionForPay, hmacWPswForSignStr, "RESTORE");
		System.out.println("Transaction Sign [Byte : " + transactionSignBytes.length + " ] = " + Hex.toHexString(transactionSignBytes));

		hmacWPswForSignStr = "";		// hmacWPswForSignStr Delete in Memory 
		System.out.println("Deleted hmacWPswForSignStr : " + hmacWPswForSignStr);

		
		String bcWalletIntegrityCheckStr = new String(transactionSignBytes, 0, transactionSignBytes.length, charset);
		if (bcWalletIntegrityCheckStr.equals("0x5201")) {
			System.out.println(" >>[!] BC-Wallet Paswword Input Error. Please enter the correct wallet password!");
			System.exit(0); 
		}

		// 2.3 PublicKey Get for BlochChain
		byte[] bcPulicKeyBytes = MoaBase58.decode(moaWallet.GetBlockchainPulicKey(true));
		
		// 2.4 Transaction Verify for Block Chain : 서명된 거래 데이터 검증
		boolean sf = moaWallet.BlockchainTransactionVerifyProcess(transactionForPay.getBytes(charset), transactionSignBytes, bcPulicKeyBytes);
		System.out.println("Transaction Verify Success or Fail : " + sf);
		
	}
	


	public static String BCWalletDataParsingAndReassembleGen(String encryptedWPrkB64Str, String encryptedWPukAndHmacEwPukAndWsaltB64Str) 
	{
	
		byte[] ewPukHmacEwPukWSaltBytes = Base64.decode(encryptedWPukAndHmacEwPukAndWsaltB64Str);		
		System.out.println("  > ewPukHmacEwPukWSaltBytes [Byte : " + ewPukHmacEwPukWSaltBytes.length + " ] = " + Hex.toHexString(ewPukHmacEwPukWSaltBytes));
	
		byte[] ewPukBytes = new byte[65];
		System.arraycopy(ewPukHmacEwPukWSaltBytes, 0, ewPukBytes, 0, ewPukBytes.length);
		System.out.println("[B] ewPukBytes [Hex] : " + Hex.toHexString(ewPukBytes) + "  /[Len] : " + ewPukBytes.length);

		byte[] hmacEwPukBytes = new byte[32];
		System.arraycopy(ewPukHmacEwPukWSaltBytes, ewPukBytes.length, hmacEwPukBytes, 0, hmacEwPukBytes.length);
		System.out.println("[B] hmacEwPukBytes [Hex] : " + Hex.toHexString(hmacEwPukBytes) + "  /[Len] : " + hmacEwPukBytes.length);

		byte[] wSaltBytes = new byte[32];
		System.arraycopy(ewPukHmacEwPukWSaltBytes, ewPukBytes.length + hmacEwPukBytes.length, wSaltBytes, 0, wSaltBytes.length);
		System.out.println("[B] wSaltBytes [Hex] : " + Hex.toHexString(wSaltBytes) + "  /[Len] : " + wSaltBytes.length);
			
		String hmacEWPukB64StrPerEwPrkB664Str$EwPukB64Str$WSaltB64Str = Base64.toBase64String(hmacEwPukBytes) + "%" +
																	encryptedWPrkB64Str + "$" + Base64.toBase64String(ewPukBytes) + "$" + Base64.toBase64String(wSaltBytes);			
		return hmacEWPukB64StrPerEwPrkB664Str$EwPukB64Str$WSaltB64Str;
	}

}	
	
	