import java.io.*;
import java.security.*;
//import java.util.Arrays;
import java.util.StringTokenizer;
import java.nio.charset.Charset;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.moa.core.auth.client.api.MoaClientCoreAuthLib;
import org.moa.core.auth.server.api.FranchiseAuthDBProcess;
import org.moa.core.auth.server.api.ManagerAuthDBProcess;
import org.moa.core.auth.server.api.MoaServerCoreAuthLib;


public class FranchiseLoginTest {
	
	
	static {
		
		java.security.Provider p = new org.bouncycastle.jce.provider.BouncyCastleProvider();
		Security.addProvider(p);
		
	}
	
	// Define about MOA B2C Manager System Properties File
	static String moaB2CCorePropertiesFileName = "moaB2CCore.properties";
		
	// Constructor Create MOA Franchise Lib for Registration and LogIn of ID & Password 
	static MoaClientCoreAuthLib 	moaClientFranchiseAuthLib = new MoaClientCoreAuthLib(moaB2CCorePropertiesFileName);
	static MoaServerCoreAuthLib 	moaServerFranchiseAuthLib = new MoaServerCoreAuthLib(moaB2CCorePropertiesFileName);
	static FranchiseAuthDBProcess	franchiseAuthDBProcess 		= new FranchiseAuthDBProcess();

	static Charset charset = Charset.forName("UTF-8");
	static String tokenChar = "$";		// Tokenizer String ("$")
	
	public static void main(String args[]) 
		   throws NoSuchProviderException, NullPointerException, Exception
	{
		
		BufferedReader registSelect = null;
		
		BufferedReader brID = null;
		BufferedReader brPSW = null;
		
		try {

			while(true) {
				
				System.out.println("[*] Franchise LogIn Start------------------------------------> ");

				registSelect = new BufferedReader(new InputStreamReader(System.in));	
				System.out.print("[*] Franchise Login Select - [1] Regist(X) [2] Login(O) : ");
				String selectNumberStr = registSelect.readLine();
				
				switch(selectNumberStr) {
					
					case "2" :		
						// 2.1 [Only ClientPC] Input ID & PSW (for Login) 
						brID = new BufferedReader(new InputStreamReader(System.in));	
						System.out.print("[**] 2.1 (ID/PSW-Login: Client) Franchise LogIn ID : ");
						String logInIdStr = brID.readLine();
						byte[] idBytes =logInIdStr.getBytes(charset);
						
						// 2.2 [Only ClientPC] Input PSW (for Login) 
						brPSW = new BufferedReader(new InputStreamReader(System.in));	
						System.out.print("[**] 2.2 (ID/PSW-Login: Client) Franchise LogIn Password : ");
						String logInPswStr = brPSW.readLine();
							 
						// 2.3 [ClientPC -> FranchiseAuthServer] ID/PSW LogIn Start Request : {Header}
						String idPswLogInStartReqMsgGenStr = moaClientFranchiseAuthLib.coreIdPswLogInStartRequestMsgGenProcess();
						System.out.println("[*] 2.3 (ID/PSW-Login: Client -> FranchiseAuthServer) ID/PSW LogIn Satrt Reqest Msg Gen[String] : " + idPswLogInStartReqMsgGenStr);
							 
						// 2.4 [Only FranchiseAuthServer] ID/PSW LogIn Start Request Parser[NonceBase64Str] 
						String idPswloginStartReqParserResultNonceStr = moaServerFranchiseAuthLib.coreAuthServerMsgPacketParser(idPswLogInStartReqMsgGenStr, null);		// moaMANAGER_IdPswLogInStartReq Msg Parser
						System.out.println("[*] 2.4.0 (ID/PSW-Login: FranchiseAuthServer) ID/PSW Login Start Req Msg Parser Result[String] : " + idPswloginStartReqParserResultNonceStr);
											
						// 2.5 [ClientPC <- FranchiseAuthServer] ID/PSW LogIN Start Response MSG Gen : {Header$NonceBase64Str}
						String idPswLogInStartResMsgStr = moaServerFranchiseAuthLib.coreIdPswLoginStartResponseMsgGenProcess(idPswloginStartReqParserResultNonceStr);	 
						System.out.println("[*] 2.5 (ID/PSW-Login: Client <- FranchiseAuthServer) ID/PSW LogIn Nonce Msg Gen[String] : " + idPswLogInStartResMsgStr);
																	
						// 2.6 [ClientPC -> FranchiseAuthServer] ID/PSW LogIN Start Response Msg Parser & ID/PSW LogIn Request MSG Gen 
						String idPswLoginStartResParserResultStr = moaClientFranchiseAuthLib.coreAuthClientMsgPacketParser(idPswLogInStartResMsgStr);					// moaMID_PIN_LOGINSTARTRES Msg Parser	
						System.out.println("[*] 2.6.0 (ID/PSW-Login: Client) ID/PSW Login Start Request Msg Parser[String] : " + idPswLoginStartResParserResultStr);
						//                                     ID/PSW LogIn Request MSG Gen : Header$ManagerIDBase64Str$HashPSWBase64Str$HmacPSWBase64Str$NonceBase64Str
						String idPswLogInReqMsgGenStr = moaClientFranchiseAuthLib.coreIdPswLogInRequestMsgGenProcess(logInIdStr, logInPswStr, idPswLoginStartResParserResultStr, false);	// moaMID_PIN_LOGINREQ Gen
						System.out.println("[*] 2.6 (ID/PSW-Login: Client -> FranchiseAuthServer) PIN LogIn Reqest Msg Gen[String] : " + idPswLogInReqMsgGenStr);
						
						// 2.7 [Only FranchiseAuthServer] ID/PSW LogIn - NonceOTP & Manager Auth : 
						String idPswLoginReqPaserResultStr = moaServerFranchiseAuthLib.coreAuthServerMsgPacketParser(idPswLogInReqMsgGenStr, idPswloginStartReqParserResultNonceStr);
						
						StringTokenizer idPswRegistReqST = new StringTokenizer(idPswLoginReqPaserResultStr, "$");
						String nonceVefirySFStr = idPswRegistReqST.nextToken();
						String franchiseIdBase64Str = idPswRegistReqST.nextToken();
						String hashPswBase64Str = idPswRegistReqST.nextToken();
						String hmacPswBase64Str = idPswRegistReqST.nextToken();
						
						System.out.println("[*] 2.7.0 (ID/PSW-Login: FranchiseAuthServer) PIN Login Req Msg Parser[String] : " + idPswLoginReqPaserResultStr);
						
						if (nonceVefirySFStr.equals("0x6031")) {
							System.out.println("Error ---> Nonce Verify Failure. ID & PSW LogIn Progress Stop!");
							return ;
						}
						
						//~~~[ AuthDB Replace Part ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
						String serverSaltAndCipheredBase64Str = franchiseAuthDBProcess.franchiseAuthDBGetProcess(franchiseIdBase64Str);
						//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
						/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
						 * AuthDB에서 FranchiseID, SaltBase4Str, CipheredPswBase64Str : 실제 DB 연동하여 ManagerID 존재 유무 확인 후 Get 
						 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
						   AuthVo pinLoginAuthDBrecord = moaServerAuthDBLib.getUserAuth(new String(Base64.decode(managerIdBase64Str)));
						   if (pinLoginAuthDBrecord == null) 	{			// ID Not Found
							   System.out.println("ID(e-mail) not exists. PIN Login Progress Stop!");
						       return "0x6041";
						   }
						   String authDBSaltBase64Str = pinLoginAuthDBrecord.getSalt();					// SaltBase64 Read
						   String authDBCipheredPswBase64Str = pinLoginAuthDBrecord.getCipheredPsw();	// CipheredPSWBase64 Read
						//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
						
						String franchiseLoginAuthVerifySFStr = null;
						if (serverSaltAndCipheredBase64Str == null) {	// if FranchiseID not Exist in Franchise AuthDB, Login Fail Process
							franchiseLoginAuthVerifySFStr = "0x6041";
							System.out.println("Error ---> Franchise ID is not found in the Franchise AUthDB. ID & PSW Re-LogIn Start!");
							continue;
						} else {
							StringTokenizer serverAutnDBGetST = new StringTokenizer(serverSaltAndCipheredBase64Str, "$"); 	// AuthDB 사용시 불필요
							String authDBSaltBase64Str = serverAutnDBGetST.nextToken();
							String authDBCipheredPswBase64Str = serverAutnDBGetST.nextToken();
						
							String cipheredPswBase64Str = moaServerFranchiseAuthLib.coreIdPswLogInAuthInfoGen(authDBSaltBase64Str, hashPswBase64Str, hmacPswBase64Str);
							if (cipheredPswBase64Str.equals(authDBCipheredPswBase64Str)) { 
								franchiseLoginAuthVerifySFStr = "0x6040";		// Franchise LogIn Success
							} else { 
								franchiseLoginAuthVerifySFStr = "0x6041";		// Franchise LogIN Fail
								System.out.println("Error ---> Franchise ID & PSW Login Auth Fail. Re-LogIn Start!");
								continue;
							}			
						}	
						
						// 2.8 [ClientPC <- FranchiseAuthServer] Manager ID/PSW LogIn Result Msg Gen
						String idPswLoginResMsgStr = moaServerFranchiseAuthLib.coreIdPswLogInResponseMsgGenProcess(nonceVefirySFStr, franchiseLoginAuthVerifySFStr, null);
						System.out.println("[*] 2.8 (ClientPC <- FranchiseAuthServer) ID & PSW Login Res Msg Gen[String] : " + idPswLoginResMsgStr);
								
						// 2.9 [Only ClientPC] Manager ID/PSW LogIn Success & Fail Result Parser	
						String idPswFranchiseLoginResultStr = moaClientFranchiseAuthLib.coreAuthClientMsgPacketParser(idPswLoginResMsgStr); // nonce, (DB)Id Exist & PIN Auth Check
						System.out.println("[*] 2.9 (ClientPC) ID & PSW ID & Psw Login Result Parser[String] : " + idPswFranchiseLoginResultStr);
						
						StringTokenizer franchiseAutnSFST = new StringTokenizer(idPswFranchiseLoginResultStr, "$");
						String nonceVerifySFStr = franchiseAutnSFST.nextToken();
						String franchiseAuthSFStr = franchiseAutnSFST.nextToken();
						//String managerLevelStr = managerAutnSFST.nextToken();	// ManagerLevel of AdminID / ManagerLevel of TopManagerID / ManagerLevel of GeneralMangerID Login(Using Regist)  
						
						if ( nonceVerifySFStr.equals("0x6030") && franchiseAuthSFStr.equals("0x6040") ) {	// User Auth Check
							System.out.println("[*] Franchise User Auth Success");
						} else {
							System.out.println("[*] Franchise User Auth Failure");
						}
											
					default :		
						break;
						
				} 	//----- Switch case area
			}   	//----- While area	
			
		} catch(Exception e) { }
		
	}	
	
}

