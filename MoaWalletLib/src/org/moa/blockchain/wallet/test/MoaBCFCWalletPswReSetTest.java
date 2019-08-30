package org.moa.blockchain.wallet.test;

import java.io.*;
import java.security.*;

import java.util.StringTokenizer;
import java.nio.charset.Charset;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import org.moa.blockchain.wallet.coreapi.MoaBase58;
import org.moa.blockchain.wallet.coreapi.MoaWalletAPI;


public class MoaBCFCWalletPswReSetTest {
	
	
	static {
		
		java.security.Provider p = new org.bouncycastle.jce.provider.BouncyCastleProvider();
		Security.addProvider(p);
		
	}
	
	static String propertyPathFileName = "E:\\99_JavaWork\\MoaWalletLib\\moaWallet.properties";
	static MoaWalletAPI moaBCWallet = new MoaWalletAPI(propertyPathFileName);	// ex) eccCurve="secp256r1", hashForSignAlg="SHA256"
	
	static MoaBase58 base58 = new MoaBase58();
	static Charset charset = Charset.forName("UTF-8");
	
	@SuppressWarnings({ "unused" })
	public static void main(String args[]) 
		   throws NoSuchProviderException, NullPointerException, Exception
	{
		
		BufferedReader restoreSelectBr 	= null; 
		BufferedReader walletPSWBr   	= null;
		BufferedReader dateOfBirthBr 	= null;		// yyyymmdd(19901010)
		BufferedReader resetWalletPswBr	= null;
		//moaWallet.BCWalletHmacWPswGenProcess(pswForWalletIssuingStr);
			
		String tokenChar = "$";		// Tokenizer String ("$")
		
		try {
			
			while(true) {
				
				restoreSelectBr = new BufferedReader(new InputStreamReader(System.in));
				System.out.print("[*] (BC-WPsw ReSet) Select of Regist and wPsw ReSet for Restore (BC&FC)Wallet [OK] : "); 
				String selectStr = restoreSelectBr.readLine();
				
				if( !(selectStr.toUpperCase().equals("OK")) ) {
					System.out.println("[*] Not Select of Registration fpr Restore (BC&FC)Wallet! - Stop"); 
				}

				
				//----------------------------------------------------------------------------->
				moaBCWallet.BlockchainWalletFileDeleteProcess("moaWalletRestore.dat", "0x5096");

				///////////////////////////////////////////////////////////////////////////////////////////
				//---[ BCWallet Regist for Restore Request & Response MSG Process -----------------> 
				System.out.println("[*]***************************************************************************");
				System.out.println("[*] BCWallet Regist for Restore Request & Response MSG Process ------->");
				System.out.println("[*]***************************************************************************");
				
				// 1.0 Member ID
				String storageIdStr =  "test21@aaa.com";
				System.out.println("[*] 1.0 Storage ID(Auto Load) : " + storageIdStr);

				// 1.1 [Only Android : BCW-Issuing] : Password Input for Encrypt to the moaWallet KeyPair [<-- for BC-Wallet & FC-Wallet]
				walletPSWBr = new BufferedReader(new InputStreamReader(System.in));
				System.out.print("[*] 1.1 Enter moaWallet Password [String(123456)] : "); 
				String wPswStr = walletPSWBr.readLine();	
				//String wPswStr = "123456";
				
				
				//String hmacWPswGenB64Str = moaBCWallet.BCWalletHmacWPswGenProcess(wPswStr);
				//System.out.println("[*] 1.3 hmacWPsw and Encrypt hmacWPswConcatHmac Data [Base64] : " + hmacWPswB64str$encHmacWPswConcatHmacB64str); 
				
				// 1.2 [Only Android : BCW-Issuing] : Date of birth(yyyymmdd) Input for BC-Wallet wPSW Encrypt
				dateOfBirthBr = new BufferedReader(new InputStreamReader(System.in));
				System.out.print("[*] 1.2 Enter User's Date of Birth[yyyymmdd(19901010)] : "); 
				String dateOfBirthStr = dateOfBirthBr.readLine();	// "yyyymmdd(19901010)"
				//String dateOfBirthStr = "19901010";	// "yyyymmdd(19901010)"
				
				// 1.3 [Only Android : BCW-Issuing] : Gen hmacWPswB64str and encHmacWPswConcatHmacB64str using idStr, wPswStr, dateOfBirthStr 
				System.out.println("[*]---------------------------------------------------------------------------"); 
				String hmacWPswB64str$encHmacWPswConcatHmacB64str = moaBCWallet.BCWalletEncryptWPswGenProcess(storageIdStr, wPswStr, dateOfBirthStr);
				System.out.println("[*] 1.3 hmacWPsw and Encrypt hmacWPswConcatHmac Data [Base64] : " + hmacWPswB64str$encHmacWPswConcatHmacB64str); 
				
				wPswStr = ""; 			// wPswStr Delete in Memory
				dateOfBirthStr = "";	// dateOfBirthStr Delete in Memory
				System.out.println("Deleted wPswStr : " + wPswStr);
				System.out.println("Deleted dateOfBirthStr : " + dateOfBirthStr);
				
				StringTokenizer hmacWPswEhmacWPswST = new StringTokenizer(hmacWPswB64str$encHmacWPswConcatHmacB64str, "$");
				//-[ Add ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				String hCardPsw64Str  = hmacWPswEhmacWPswST.nextToken();	// 2019.07.17 Add : FC-Wallet HashCardPswB64Str	
				//-[ Add ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				String hmacWPswB64Str = hmacWPswEhmacWPswST.nextToken();
				String encryptHmacWPswB64Str = hmacWPswEhmacWPswST.nextToken();						// Data to be stored in the AuthDB
				String hmacWPswforBCWRegistStr = Hex.toHexString(Base64.decode(hmacWPswB64Str));	// wPsw를 변형된 패스워드인 "hmacWPsw14HexString(28Bytes)"를 사용

				//System.out.println("[*] hmacWPswGenB64Str : " + hmacWPswGenB64Str);
				System.out.println("[*] hmacWPswB64Str : " + hmacWPswB64Str);
				
				
				// 1.4 [Only Android : BCW-Issuing] ECDSA KeyPair & WalletAddr Gen for BlockChain, wSalt and HmacWPsw-base KeyPair Encrypt
				//String EwPrkwAndEwPukAndwSaltBase64Str = moaBCWalletApi.RestoreBlockchainWalletInfoGenProcess("MoaWalletIssuing", hmacWPswforBCWRegistStr); // Data to be stored in the AuthDB
				// 2019.06.19 Modified
				String existHmacEwPukB64PerEwPrkB64$EwPukB64$wSaltB64Str = moaBCWallet.RestoreBlockchainWalletInfoGenProcess("MoaWalletIssuing", hmacWPswforBCWRegistStr); // Data to be stored in the AuthDB
				StringTokenizer hmacWPukEwPrkEwPukWSaltST = new StringTokenizer(existHmacEwPukB64PerEwPrkB64$EwPukB64$wSaltB64Str, "%"); 
				String existHmacEwPukB64Str = hmacWPukEwPrkEwPukWSaltST.nextToken();
				String existEwPrkB64$EwPukB64$wSaltB64Str = hmacWPukEwPrkEwPukWSaltST.nextToken();
				System.out.println("[*] 1.4 Data For AuthServer DB Storage : " + existHmacEwPukB64PerEwPrkB64$EwPukB64$wSaltB64Str);
				
				moaBCWallet.RestoreBlockchainWalletIssuingProcess(hmacWPswforBCWRegistStr, existEwPrkB64$EwPukB64$wSaltB64Str);	// Android내에 복원형 moaWalletRestore.dat 생성
				
				
				//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				//~~~~~~~~~~~~~~~~~~~~~~[moaWallet KeyPair Load, Transaction Sign & Verify]~~~~~~~~~~~~~~~~~~~~~~~~~
				
				// A.1 Password Input for Decrypt to the wPrk of moaWallet  
				//String pswForSignStr = "zxcv%$#@!12345";
				
				/* 복원형 BC-Wallet에서 사용하기 위해 개선한 방식: wPsw -> Hex.toHexString(hmacWPswBytes) 변경하여 사용 */
				String pswForSignStr = Hex.toHexString(Base64.decode(hmacWPswB64Str));
				
				// A.2 Transaction Sign Using WPRK : 거래 데이터 서명
				String transactionForPay = "It is 10000$ payment Alice to Bob";
				byte[] transactionSignBytes = moaBCWallet.BlockchainTransactionSignProcess("MoaWalletEncDecKayPair", transactionForPay, pswForSignStr, "RESTORE");
				System.out.println("[A] Transaction Sign [Byte : " + transactionSignBytes.length + " ] = " + Hex.toHexString(transactionSignBytes));
				
				pswForSignStr = "";		// Password Delete in Memory
				System.out.println("[A] Deleted Password : " + pswForSignStr);
				
				// A.3 PublicKey Get for BlochChain
				byte[] bcPulicKeyBytes = MoaBase58.decode(moaBCWallet.GetBlockchainPulicKey(true));
				
				// A.4 Transaction Verify for Block Chain : 서명된 거래 데이터 검증
				boolean sf = moaBCWallet.BlockchainTransactionVerifyProcess(transactionForPay.getBytes(charset), transactionSignBytes, bcPulicKeyBytes);
				System.out.println("[A] Transaction Verify Success or Fail : " + sf);
				
				
				///////////////////////////////////////////////////////////////////////////////////////////
				//---[ BCWallet wPsw ReSet Request & Response MSG Process --------------------> 
				System.out.println("[*]***************************************************************************");
				System.out.println("[*] BCWallet wPSW ReSet for Restore Request & Response MSG Process ---->");
				System.out.println("[*]***************************************************************************");

				//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				// 2.0 [Android <-> KG모빌리언스] : KG모빌리언스의 비대면 휴대폰 인증 수행[성공시에만 다음 단계] 연동 필요 ~~~~~~~~~~~~~~
				//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				System.out.println("[*] 2.0 If the KG MOBILIONS succeeds in performing non-face-to-face authentication of the mobile phone, The next step Progress."); 
				
				// 2.1 [Only Android : BCW wPsw ReSet] : ReSet wPsw Input for BCWallet [<-- for BC-Wallet & FC-Wallet]
				resetWalletPswBr = new BufferedReader(new InputStreamReader(System.in));
				System.out.print("[*] 2.1 Entered moaWallet ReSet Password [String(654321)] : "); 
				String reSetWPswStr = resetWalletPswBr.readLine();
				//String reSetWPswStr = "654321";

				// 2.2 [Only Android : BCW wPsw ReSet] : Date of birth(yyyymmdd) Input for Exist HmacWPsw Decrypt and New HamcResetWPsw Encrypt
				String reInputDateOfBirthStr = "19901010";
				System.out.println("[*] 2.2 Enter moaWallet Date of Birth [yyyymmdd] : " + reInputDateOfBirthStr);
				
				// 2.3 [Only Android : BCW wPsw ReSet] : Validity verify about DoB and Decrypt hmacWPsw using idStr, reSetWPswStr, reInputdateOfBirthStr 
				String doBVerifySFStr$hmacWPsw14B64Str 
				       = moaBCWallet.BCWalletWPswVerifyAndDecryptGenProcess(storageIdStr, reInputDateOfBirthStr, encryptHmacWPswB64Str);
				System.out.println("[*] 2.3 DoB Vailidy Verify and BC-Wallet wPSW(hmacWPsw) Decrypt [StringTokeniser] : " + doBVerifySFStr$hmacWPsw14B64Str); 
				
				StringTokenizer doBVerifySFAndHmacwPsw14B64ST = new StringTokenizer(doBVerifySFStr$hmacWPsw14B64Str, "$"); 
				String doBVerifySFStr   = doBVerifySFAndHmacwPsw14B64ST.nextToken();
				String existHmacwPsw14B64Str = doBVerifySFAndHmacwPsw14B64ST.nextToken();
				System.out.println("  >> existHmacwPsw14B64Str : " + existHmacwPsw14B64Str);
				if (doBVerifySFStr.equals("0x5114"))  {				// DoB Validity Check Fail  
					System.out.println("  >>[!] Incorrect DoB(yyyymmdd) Input. ---> Re-enter the correct date of birth!");
					continue;
				} else if (doBVerifySFStr.equals("0x5113")) {		// DoB Validity Check Success
					System.out.println("  >>[*] Correct Date of Bith(yyyymmdd) Input. Next Step.");
				}
				
				// 2.4 [Only Android : BCW wPsw ReSet] : Decrypt EwPrk, EwPuk using hmacWPsw14Str(=Exsit wPsw) and wSalt
				String existEwPrkB64strAndEwPukB64strAndwSaltB64str = existEwPrkB64$EwPukB64$wSaltB64Str;    // Get form AuthServr DB
				String newBCWalletResetEncryptWPswGenB64str = moaBCWallet.BCWalletEncryptWPswGenProcess(storageIdStr, reSetWPswStr, reInputDateOfBirthStr); 
				System.out.println("[*] 2.4 New Gen HmacReSetWPsw and Encrypted HmacReSetWPsw using id, ReSet wPSW & DoB [String] : " + newBCWalletResetEncryptWPswGenB64str); 

				 StringTokenizer newHmacReSetWPswStrAndnewHmacReSetWPswStrST = new StringTokenizer(newBCWalletResetEncryptWPswGenB64str, "$");
				 String newHashMoapayResetPswB64Str	 = newHmacReSetWPswStrAndnewHmacReSetWPswStrST.nextToken();	// 2019.07.17 Add(FCWallet : newHashMoapayResetPswB64Str)
				 String newHmacReSetBCWPsw14B64Str	 = newHmacReSetWPswStrAndnewHmacReSetWPswStrST.nextToken();
				 String newEhmac16BCWPswHmac16B64str = newHmacReSetWPswStrAndnewHmacReSetWPswStrST.nextToken();
				
				reSetWPswStr = ""; 			// wPswStr Delete in Memory
				reInputDateOfBirthStr = "";	// reInputDateOfBirthStr Delete in Memory
				System.out.println("Deleted reSetWPswStr : " + wPswStr);
				System.out.println("Deleted reInputDateOfBirthStr : " + reInputDateOfBirthStr);


				// 2.5 [Only Android : BCW wPsw ReSet] : Encrypt of EwPrk, EwPuk using hmacReSetWPsw14Str(=New wPsw) and New wSalt
				// String orgWPukPerConcatNewEwPrkB64str$NewEwPukB64str$NewWSaltB64str 
				//        = moaBCWallet.BlockchainWalletPswReSetRegistDataGenProcess(existHmacwPsw14B64Str, newHmacReSetWPsw14B64Str, existEwPrkB64strAndEwPukB64strAndwSaltB64str);
				// 2019.06.19 Modified
				String bcwPswReSetRegDataPerNewEwPrkB64str$NewEwPukB64str$NewWSaltB64str 
				       = moaBCWallet.BlockchainWalletPswReSetRegistDataGenProcess(existHmacwPsw14B64Str, newHmacReSetBCWPsw14B64Str, existHmacEwPukB64Str, existEwPrkB64strAndEwPukB64strAndwSaltB64str);
				System.out.println("[*] 2.5 Encrypt of EwPrk, EwPuk using hmacReSetWPsw14Str(=New wPsw) and New wSalt [String] : " + bcwPswReSetRegDataPerNewEwPrkB64str$NewEwPukB64str$NewWSaltB64str); 

				// bcwPswReSetRegDataGenResultStr = receivedHamcWPswValidityCheckSF + "%" + decryptWPukB64str + "%" + newHmacEwPukB64str + "%" + newEwPrkB64str + "$" + newEwPukB64str + "$" + newWSaltB64str;
				StringTokenizer wPswVerifyAndOrgWPukPerNewEwPrkNewEwPukNewWSaltST = new StringTokenizer(bcwPswReSetRegDataPerNewEwPrkB64str$NewEwPukB64str$NewWSaltB64str, "%");
				String existSavedWPswVerifySFstr = wPswVerifyAndOrgWPukPerNewEwPrkNewEwPukNewWSaltST.nextToken();
				String orgWPukB64str = wPswVerifyAndOrgWPukPerNewEwPrkNewEwPukNewWSaltST.nextToken();
				String newHmacEwPukB64str = wPswVerifyAndOrgWPukPerNewEwPrkNewEwPukNewWSaltST.nextToken();
				String newEwPrkB64str$NewEwPukB64str$NewWSaltB64str	= wPswVerifyAndOrgWPukPerNewEwPrkNewEwPukNewWSaltST.nextToken();
				
				if (existSavedWPswVerifySFstr.equals("0x5120")) {
					System.out.println("  >>[!] BC-Wallet Exist wPassword Verify Fail. Please contact the operation center in Moa!");
					continue;
				} else {
					System.out.println("  >>[*] BC-Wallet Exist wPassword Verify Success. The Next Step.");
				}

				System.out.println("  >>[*] orgWPukB64str [B64str] : " + orgWPukB64str);
				
				moaBCWallet.BlockchainWalletPswReSetStorageGenProcess(newHmacReSetBCWPsw14B64Str, orgWPukB64str, newEwPrkB64str$NewEwPukB64str$NewWSaltB64str);

				
				//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				//~~~~~~~~~~~~~~~~~~~~~~[moaWallet KeyPair Load, Transaction Sign & Verify]~~~~~~~~~~~~~~~~~~~~~~~~~
				
				// B.1 Password Input for Decrypt to the WPRK of moaWallet  
				//String newPswForSignStr = "zxcv%$#@!12345";	// 기존 사용 방식
				
				/* 복원형 BC-Wallet에서 사용하기 위해 개선한 방식: ReSetwPsw -> Hex.toHexString(hmacReSetWPswBytes) 변경하여 사용 */
				String newPswForSignStr = Hex.toHexString(Base64.decode(newHmacReSetBCWPsw14B64Str));	
				
				// B.2 Transaction Sign Using WPRK : 거래 데이터 서명
				String newTransactionForPay = "It is 10000$ payment Alice to Bob";
				//byte[] newTransactionSignBytes = moaBCWallet.BlockchainTransactionSignProcess("MoaWalletEncDecKayPair", newTransactionForPay, newPswForSignStr, true);
				byte[] newTransactionSignBytes = moaBCWallet.BlockchainTransactionSignProcess("MoaWalletDataBaseSignature", newTransactionForPay, newPswForSignStr, "TEMP");
				System.out.println("[B] Transaction Sign [Byte : " + newTransactionSignBytes.length + " ] = " + Hex.toHexString(newTransactionSignBytes));
				
				String bcWalletIntegrityCheckStr = new String(newTransactionSignBytes, 0, newTransactionSignBytes.length, charset);
				if (bcWalletIntegrityCheckStr.equals("0x5201")) {
					System.out.println("  >>[!] Blockchain Wallet Data Intergity check Fail. Please enter the correct wallet password!");
					System.exit(0); 
				}
				
				newPswForSignStr = "";		// Password Delete in Memory
				System.out.println("[B] Deleted Password : " + pswForSignStr);
				
				// B.3 PublicKey Get for BlochChain
				byte[] newBCPulicKeyBytes = MoaBase58.decode(moaBCWallet.GetBlockchainPulicKey(true));
				System.out.println("[B] newBCPulicKeyBytes [Hex] : " + Hex.toHexString(newBCPulicKeyBytes));
				
				// B.4 Transaction Verify for Block Chain : 서명된 거래 데이터 검증
				boolean newSF = moaBCWallet.BlockchainTransactionVerifyProcess(transactionForPay.getBytes(charset), newTransactionSignBytes, newBCPulicKeyBytes);
				System.out.println("[B] Transaction Verify Success or Fail : " + newSF);

				//newSF = false;
				if ( newSF == true) {	// BCWallet Data Validation Success
					System.out.println("BCWallet Data Validation Success in Androrid.");
					moaBCWallet.BlockchainWalletFileUpdateProcess();			// Copy "tempWalletRestore.dat" file to "moaWalletRestore.dat" file
					moaBCWallet.BlockchainWalletFileDeleteProcess("tempWalletRestore.dat", "0x5096");	// "tempWalletRestore.dat" Delete
					
				} else {
					System.out.println("BCWallet Data Validation Fail in Androrid.");
					moaBCWallet.BlockchainWalletFileDeleteProcess("tempWalletRestore.dat", "0x5096");	// "tempWalletRestore.dat" Delete
				}
				
			} 	//---[End of switch

		
		} catch(Exception e) { }
		
	}	//---[End of main
	
}	//---[End of Class 