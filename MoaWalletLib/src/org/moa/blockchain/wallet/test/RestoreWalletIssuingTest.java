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



public class RestoreWalletIssuingTest {
	
	static{

		java.security.Provider p = new org.bouncycastle.jce.provider.BouncyCastleProvider();
		Security.addProvider(p);
	}
	
	static String propertyPathFileName = "E:\\99_JavaWork\\MoaWalletLib\\moaWallet.properties";
	static MoaWalletAPI moaWallet = new MoaWalletAPI(propertyPathFileName);	// ex) eccCurve="secp256r1", hashForSignAlg="SHA256"
	
	static MoaBase58 base58 = new MoaBase58();
	static Charset charset = Charset.forName("UTF-8");
	
	@SuppressWarnings("unused")
	public static void main(String args[]) 
			   throws NoSuchProviderException, NullPointerException, Exception
	{
		
		//MoaWalletAPI moaWallet = new MoaWalletAPI();	// ex) eccCurve="secp256r1", hashForSignAlg="SHA256"

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//~~~~~~~~~~~~~~~~~~~~~~[moaWallet KeyPair & Addr Issuing]~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		
		// 1.0 User Member ID
		// String storageIdStr =  "test1@aaa.com";
		// 1.1 Password Input for Encrypt to the moaWallet KeyPair
		//String pswForWalletIssuingStr = "zxcv%$#@!123456";
		String pswForWalletIssuingStr = "111111";
		String hmacWPswForWalletB64Str = moaWallet.BCWalletHmacWPswGenProcess(pswForWalletIssuingStr);
		String hmacWPswForWalletDataRegistStr = Hex.toHexString(Base64.decode(hmacWPswForWalletB64Str));
		
		System.out.println("Org wPsw : " + pswForWalletIssuingStr);
		System.out.println("Change hmacWPsw28HexStr : " + hmacWPswForWalletDataRegistStr);
		
		// 1.2 ECDSA KeyPair & WalletAddr Gen for BlockChain
		
		// moaWallet Gen for Restore
		//String EwPrkwAndEwPukAndwSaltBase64Str = moaWallet.RestoreBlockchainWalletInfoGenProcess("MoaWalletIssuing", hmacWPswForWalletDataRegistStr); 	// 전송을 위한 월릿 복원 데이터 생성
		//System.out.println("Data For AuthServer DB Storage : " + EwPrkwAndEwPukAndwSaltBase64Str);
		//moaWallet.RestoreBlockchainWalletIssuingProcess(hmacWPswForWalletDataRegistStr, EwPrkwAndEwPukAndwSaltBase64Str);								// 복원형 moaWalletRestore.dat 생성

		// 2019.06.19 Modified
		String hmacWPukB64PerEwPrkB64$EwPukB64$wSaltB64Str = moaWallet.RestoreBlockchainWalletInfoGenProcess("MoaWalletIssuing", hmacWPswForWalletDataRegistStr); // Data to be stored in the AuthDB
		StringTokenizer hmacWPukEwPrkEwPukWSaltST = new StringTokenizer(hmacWPukB64PerEwPrkB64$EwPukB64$wSaltB64Str, "%"); 
		String hmacWPukB64Str = hmacWPukEwPrkEwPukWSaltST.nextToken();
		String eWPrkwAndEwPukAndwSaltBase64Str = hmacWPukEwPrkEwPukWSaltST.nextToken();
		System.out.println("Data For AuthServer DB Storage : " + hmacWPukB64PerEwPrkB64$EwPukB64$wSaltB64Str);
		moaWallet.RestoreBlockchainWalletIssuingProcess(hmacWPswForWalletDataRegistStr, eWPrkwAndEwPukAndwSaltBase64Str);								// 복원형 moaWalletRestore.dat 생성
		
		
		// 1.3 walletAddr Get for BlochChain
		String walletAddrStr = moaWallet.GetBlockchainWalletAddr(true);
		System.out.println("Wallet Addr [MoaBase58] : " + walletAddrStr);

		pswForWalletIssuingStr = ""; 			// Password Delete in Memory
		hmacWPswForWalletB64Str = "";			// hmacWPswForWalletB64Str in Memory
		hmacWPswForWalletDataRegistStr = ""; 	// hmacWPswForWalletDataRegistStr Delete in Memory
		System.out.println("Deleted Password : " + pswForWalletIssuingStr);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//~~~~~~~~~~~~~~~~~~~~~~[moaWallet KeyPair Load, Transaction Sign & Verify]~~~~~~~~~~~~~~~~~~~~~~~~~
		
		// 2.1 Password Input for Decrypt to the WPRK of moaWallet  
		//String pswForSignStr = "zxcv%$#@!123456"; // "zxcv%$#@!12345";
		String pswForSignStr = "111111"; // "zxcv%$#@!12345";
		String hmacWPswBase64Str = moaWallet.BCWalletHmacWPswGenProcess(pswForSignStr);
		String hmacWPswForSignStr = Hex.toHexString(Base64.decode(hmacWPswBase64Str));
		
		// 2.2 Transaction Sign Using WPRK : 거래 데이터 서명
		String transactionForPay = "It is 10000$ payment Alice to Bob";
		byte[] transactionSignBytes = moaWallet.BlockchainTransactionSignProcess("MoaWalletDataBaseSignature", transactionForPay, hmacWPswForSignStr, "RESTORE");
		System.out.println("Transaction Sign [Byte : " + transactionSignBytes.length + " ] = " + Hex.toHexString(transactionSignBytes));
		
		String bcWalletIntegrityCheckStr = new String(transactionSignBytes, 0, transactionSignBytes.length, charset);
		if (bcWalletIntegrityCheckStr.equals("0x5201")) {
			System.out.println("  >>[!] Blockchain Wallet Data Intergity check Fail. Please enter the correct wallet password!");
			System.exit(0); 
		}
	
		pswForSignStr = "";			// Password Delete in Memory
		hmacWPswBase64Str = ""; 	// hmacWPswBase64Str Delete in Memory
		hmacWPswForSignStr = "";	// hmacWPswForSignStr Delete in Memory
		System.out.println("Deleted Password : " + pswForSignStr);
		
		// 2.3 PublicKey Get for BlochChain
		byte[] bcPulicKeyBytes = MoaBase58.decode(moaWallet.GetBlockchainPulicKey(true));
		
		// 2.4 Transaction Verify for Block Chain : 서명된 거래 데이터 검증
		boolean sf = moaWallet.BlockchainTransactionVerifyProcess(transactionForPay.getBytes(charset), transactionSignBytes, bcPulicKeyBytes);
		System.out.println("Transaction Verify Success or Fail : " + sf);
		
	/*	
		byte[] pukBytes = Hex.decode("04A98A23EA15AF24C4F0F432B1C3A3BF7624C3C12DEAD97A2BF7F223D39648B533149C5B365EDCF371D80DD8203CF8208F01EF640BEE0BE89E91BD90E93F9288FC");
		byte[] ethAddr = moaWallet.EthereumWalletAddrGen("SHA256", pukBytes); 
		
		System.out.println("New Gen  eth.addrTest[Hex] : " + Hex.toHexString(ethAddr));
		System.out.println("Expected eth.addrTest[Hex] : 58B0EB7BA944425A9C9A9454256C6FBE7C5C00E8");
	*/	
		
	}
	

}	


	
	