package org.moa.blockchain.wallet.test;

import java.nio.charset.Charset;
import java.security.NoSuchProviderException;
import java.security.Security;
//import java.util.StringTokenizer;

import org.bouncycastle.util.encoders.Hex;
import org.moa.blockchain.wallet.coreapi.MoaBase58;
import org.moa.blockchain.wallet.coreapi.MoaWalletAPI;



public class NonRestoreWalletTest {
	
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
		//~~~~~~~~~~~~~~~~~~~~~~[Non-Restore moaWallet KeyPair & Addr Issuing]~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
		
		// 1.1 Password Input for Encrypt to the moaWallet KeyPair
		String pswForWalletIssuingStr = "zxcv%$#@!123456";
		
		// 1.2 ECDSA KeyPair & WalletAddr Gen for BlockChain
		//---[NonRestore MSG
		moaWallet.NonRestoreBlockchainWalletIssuingProcess("MoaWalletIssuing", pswForWalletIssuingStr);  	// 비복원 moaWallet.dat 생성

		pswForWalletIssuingStr = ""; 	// Password Delete in Memory
		System.out.println("Deleted Password : " + pswForWalletIssuingStr);
		
		// 1.3 walletAddr Get for BlochChain
		String walletAddrStr = moaWallet.GetBlockchainWalletAddr(false);
		System.out.println("Wallet Addr [MoaBase58] : " + walletAddrStr);

		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//~~~~~~~~~~~~~~~~~~~~~~[moaWallet KeyPair Load, Transaction Sign & Verify]~~~~~~~~~~~~~~~~~~~~~~~~~
		
		// 2.1 Password Input for Decrypt to the WPRK of moaWallet  
		String pswForSignStr = "zxcv%$#@!123456"; // "zxcv%$#@!12345";
		
		// 2.2 Transaction Sign Using WPRK : 거래 데이터 서명
		String transactionForPay = "It is 10000$ payment Alice to Bob";
		byte[] transactionSignBytes = moaWallet.BlockchainTransactionSignProcess("MoaWalletDataBaseSignature", transactionForPay, pswForSignStr, "NON");
		System.out.println("Transaction Sign [Byte : " + transactionSignBytes.length + " ] = " + Hex.toHexString(transactionSignBytes));
		
		pswForSignStr = "";		// Password Delete in Memory
		System.out.println("Deleted Password : " + pswForSignStr);
		
		// 2.3 PublicKey Get for BlochChain
		byte[] bcPulicKeyBytes = MoaBase58.decode(moaWallet.GetBlockchainPulicKey(false));
		
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


	
	