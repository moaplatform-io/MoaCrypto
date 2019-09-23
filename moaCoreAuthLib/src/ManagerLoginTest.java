import java.io.*;
import java.security.*;
//import java.util.Arrays;
import java.util.StringTokenizer;
import java.nio.charset.Charset;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.moa.core.auth.client.api.MoaClientCoreAuthLib;
import org.moa.core.auth.server.api.ManagerAuthDBProcess;
import org.moa.core.auth.server.api.MoaServerCoreAuthLib;


public class ManagerLoginTest {
	
	
	static {
		
		java.security.Provider p = new org.bouncycastle.jce.provider.BouncyCastleProvider();
		Security.addProvider(p);
		
	}
	
	// Define about MOA B2C Manager System Properties File
	static String moaB2CManagerPropertiesFileName = "moaB2CCore.properties";
		
	// Constructor Create MOA Manager Lib for Registration and LogIn of ID & Password 
	static MoaClientCoreAuthLib 	moaClientManagerAuthLib = new MoaClientCoreAuthLib(moaB2CManagerPropertiesFileName);
	static MoaServerCoreAuthLib 	moaServerManagerAuthLib = new MoaServerCoreAuthLib(moaB2CManagerPropertiesFileName);
	static ManagerAuthDBProcess		managerAuthDBProcess = new ManagerAuthDBProcess();

	// Define about MOA Message Identifier(MID) Parsing for PIN(ID & Password) 
	protected static final String moaAdminManager	= "0x61";
	protected static final String moaTopManager		= "0x62";
	protected static final String moaGeneralManager	= "0x63";

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
				
				System.out.println("[*] Manager LogIn Start------------------------------------> ");

				registSelect = new BufferedReader(new InputStreamReader(System.in));	
				System.out.print("[*] Login Manager Level Select - [1] Admin(X) [2] TopManger(X) [3] GeneralManaer(O) : ");
				String selectNumberStr = registSelect.readLine();
				
				switch(selectNumberStr) {
					
					case "3" :		// General Manger Resist [0x63]
						// 2.1 [Only ClientPC] Input ID & PSW (for Login) 
						brID = new BufferedReader(new InputStreamReader(System.in));	
						System.out.print("[**] 2.1 (ID/PSW-Login: Client) Manager LogIn ID : ");
						String logInIdStr = brID.readLine();
						byte[] idBytes =logInIdStr.getBytes(charset);
						
						// 2.2 [Only ClientPC] Input PSW (for Login) 
						brPSW = new BufferedReader(new InputStreamReader(System.in));	
						System.out.print("[**] 2.2 (ID/PSW-Login: Client) Manager LogIn Password : ");
						String logInPswStr = brPSW.readLine();
							 
						// 2.3 [ClientPC -> ManagerAuthServer] ID/PSW LogIn Start Request : {Header}
						String idPswLogInStartReqMsgGenStr = moaClientManagerAuthLib.coreIdPswLogInStartRequestMsgGenProcess();
						System.out.println("[*] 2.3 (ID/PSW-Login: Client -> ManagerAuthServer) ID/PSW LogIn Satrt Reqest Msg Gen[String] : " + idPswLogInStartReqMsgGenStr);
							 
						// 2.4 [Only ManagerAuthServer] ID/PSW LogIn Start Request Parser[NonceBase64Str] 
						String idPswloginStartReqParserResultNonceStr = moaServerManagerAuthLib.coreAuthServerMsgPacketParser(idPswLogInStartReqMsgGenStr, null);		// moaMANAGER_IdPswLogInStartReq Msg Parser
						System.out.println("[*] 2.4.0 (ID/PSW-Login: ManagerAuthServer) ID/PSW Login Start Req Msg Parser Result[String] : " + idPswloginStartReqParserResultNonceStr);
											
						// 2.5 [ClientPC <- ManagerAuthServer] ID/PSW LogIN Start Response MSG Gen : {Header$NonceBase64Str}
						String idPswLogInStartResMsgStr = moaServerManagerAuthLib.coreIdPswLoginStartResponseMsgGenProcess(idPswloginStartReqParserResultNonceStr);	 
						System.out.println("[*] 2.5 (ID/PSW-Login: Client <- ManagerAuthServer) ID/PSW LogIn Nonce Msg Gen[String] : " + idPswLogInStartResMsgStr);
																	
						// 2.6 [ClientPC -> ManagerAuthServer] ID/PSW LogIN Start Response Msg Parser & ID/PSW LogIn Request MSG Gen 
						String idPswLoginStartResParserResultStr = moaClientManagerAuthLib.coreAuthClientMsgPacketParser(idPswLogInStartResMsgStr);					// moaMID_PIN_LOGINSTARTRES Msg Parser	
						System.out.println("[*] 2.6.0 (ID/PSW-Login: Client) ID/PSW Login Start Request Msg Parser[String] : " + idPswLoginStartResParserResultStr);
						//                                     ID/PSW LogIn Request MSG Gen : Header$ManagerIDBase64Str$HashPSWBase64Str$HmacPSWBase64Str$NonceBase64Str
						String idPswLogInReqMsgGenStr = moaClientManagerAuthLib.coreIdPswLogInRequestMsgGenProcess(logInIdStr, logInPswStr, idPswLoginStartResParserResultStr, true);	// moaMID_PIN_LOGINREQ Gen
						System.out.println("[*] 2.6 (ID/PSW-Login: Client -> ManagerAuthServer) PIN LogIn Reqest Msg Gen[String] : " + idPswLogInReqMsgGenStr);
						
						// 2.7 [Only ManagerAuthServer] ID/PSW LogIn - NonceOTP & Manager Auth : 
						String idPswLoginReqPaserResultStr = moaServerManagerAuthLib.coreAuthServerMsgPacketParser(idPswLogInReqMsgGenStr, idPswloginStartReqParserResultNonceStr);
						
						StringTokenizer idPswRegistReqST = new StringTokenizer(idPswLoginReqPaserResultStr, "$");
						String nonceVefirySFStr = idPswRegistReqST.nextToken();
						String managerIdBase64Str = idPswRegistReqST.nextToken();
						String hashPswBase64Str = idPswRegistReqST.nextToken();
						String hmacPswBase64Str = idPswRegistReqST.nextToken();
						
						System.out.println("[*] 2.7.0 (ID/PSW-Login: ManagerAuthServer) PIN Login Req Msg Parser[String] : " + idPswLoginReqPaserResultStr);
						
						if (nonceVefirySFStr.equals("0x6031")) {
							System.out.println("Error ---> Nonce Verify Failure. ID & PSW LogIn Progress Stop!");
							return ;
						}
						
						//~~~[ AuthDB Replace Part ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
						String serverManagerLevelAndSaltAndCipheredBase64Str = managerAuthDBProcess.ManagerAuthDBGetProcess(managerIdBase64Str);
						//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
						/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
						 * AuthDB���� ManagerID, SaltBase4Str, CipheredPswBase64Str : ���� DB �����Ͽ� ManagerID ���� ���� Ȯ�� �� Get 
						 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
						   AuthVo pinLoginAuthDBrecord = moaServerAuthDBLib.getUserAuth(new String(Base64.decode(managerIdBase64Str)));
						   if (pinLoginAuthDBrecord == null) 	{			// ID Not Found
							   System.out.println("ID(e-mail) not exists. PIN Login Progress Stop!");
						       return "00000000";
						   }
						   String authDBSaltBase64Str = pinLoginAuthDBrecord.getSalt();					// SaltBase64 Read
						   String authDBCipheredPswBase64Str = pinLoginAuthDBrecord.getCipheredPsw();	// CipheredPSWBase64 Read
						//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
						
						String managerLoginAuthVerifySFStr = null;
						String authDBManagerLevelStr = null;
						if (serverManagerLevelAndSaltAndCipheredBase64Str == null) {	// if ManagerID not Exist in Manager AuthDB, Login Fail Process
							managerLoginAuthVerifySFStr = "0x6041";
							System.out.println("Error ---> Manager ID is not found in the Manager AUthDB. ID & PSW Re-LogIn Start!");
							continue;
						} else {
							StringTokenizer serverAutnDBGetST = new StringTokenizer(serverManagerLevelAndSaltAndCipheredBase64Str, "$");
							authDBManagerLevelStr = serverAutnDBGetST.nextToken();
							String authDBSaltBase64Str = serverAutnDBGetST.nextToken();
							String authDBCipheredPswBase64Str = serverAutnDBGetST.nextToken();
						
							String cipheredPswBase64Str = moaServerManagerAuthLib.coreIdPswLogInAuthInfoGen(authDBSaltBase64Str, hashPswBase64Str, hmacPswBase64Str);
							if (cipheredPswBase64Str.equals(authDBCipheredPswBase64Str)) { 
								managerLoginAuthVerifySFStr = "0x6040";		// manager LogIn Success
							} else { 
								managerLoginAuthVerifySFStr = "0x6041";		// Manager LogIN Fail
								System.out.println("Error ---> Manager ID & PSW Login Auth Fail. Re-LogIn Start!");
								continue;
							}			
						}	
						// 2.8 [ClientPC <- ManagerAuthServer] Manager ID/PSW LogIn Result Msg Gen
						String idPswLoginResMsgStr = moaServerManagerAuthLib.coreIdPswLogInResponseMsgGenProcess(nonceVefirySFStr, managerLoginAuthVerifySFStr, authDBManagerLevelStr);
						System.out.println("[*] 2.8 (ClientPC <- ManagerAuthServer) ID & PSW Login Res Msg Gen[String] : " + idPswLoginResMsgStr);
								
						// 2.9 [Only ClientPC] Manager ID/PSW LogIn Success & Fail Result Parser	
						String idPswManagerLoginResultStr = moaClientManagerAuthLib.coreAuthClientMsgPacketParser(idPswLoginResMsgStr); // nonce, (DB)Id Exist & PIN Auth Check
						System.out.println("[*] 2.9 (ClientPC) ID & PSW ID & Psw Login Result Parser[String] : " + idPswManagerLoginResultStr);
						
						StringTokenizer managerAutnSFST = new StringTokenizer(idPswManagerLoginResultStr, "$");
						String nonceVerifySFStr = managerAutnSFST.nextToken();
						String managerAuthSFStr = managerAutnSFST.nextToken();
						String managerLevelStr = managerAutnSFST.nextToken();	// ManagerLevel of AdminID / ManagerLevel of TopManagerID / ManagerLevel of GeneralMangerID Login(Using Regist)  
						
						if ( nonceVerifySFStr.equals("0x6030") && managerAuthSFStr.equals("0x6040") ) {	// User Auth Check
							System.out.println("[*] User Auth Success");
						} else {
							System.out.println("[*] User Auth Failure");
						}
											
					default :		
						break;
						
				} 	//----- Switch case area
			}   	//----- While area	
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}	
	
}

