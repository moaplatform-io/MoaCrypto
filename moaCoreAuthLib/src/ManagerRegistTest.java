import java.io.*;
import java.security.*;
//import java.util.Arrays;
import java.util.StringTokenizer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
//import java.io.BufferedReader;
import java.nio.charset.Charset;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.moa.crypto.coreapi.DigestCoreAPI;
import org.moa.crypto.coreapi.SecureRNGCoreAPI;
import org.moa.crypto.coreapi.SymmetricCoreAPI;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;


import org.moa.core.auth.client.api.MoaClientCoreAuthLib;
import org.moa.core.auth.server.api.MoaServerCoreAuthLib;
import org.moa.core.auth.server.api.ManagerAuthDBProcess;  // Delete after Test


public class ManagerRegistTest {
	
	
	static {
		
		java.security.Provider p = new org.bouncycastle.jce.provider.BouncyCastleProvider();
		Security.addProvider(p);
		
	}
	
	// Define about MOA B2C Manager System Properties File
	static String moaB2CCorePropertiesFileName = "moaB2CCore.properties";
		
	// Constructor Create MOA Manager Lib for Registration and LogIn of ID & Password 
	static MoaClientCoreAuthLib 	moaClientManagerAuthLib = new MoaClientCoreAuthLib(moaB2CCorePropertiesFileName);
	static MoaServerCoreAuthLib 	moaServerManagerAuthLib = new MoaServerCoreAuthLib(moaB2CCorePropertiesFileName);
	static ManagerAuthDBProcess		managerAuthDBProcess = new ManagerAuthDBProcess();

	// Define about MOA Manager ID Level Information 
	protected static final String moaAdminManager	= "0x61";
	protected static final String moaTopManager		= "0x62";
	protected static final String moaGeneralManager	= "0x63";
	
	static Charset charset = Charset.forName("UTF-8");
	static String tokenChar = "$";	// Tokenizer String ("$")
	
	

	public static void main(String args[]) 
		   throws NoSuchProviderException, NullPointerException, Exception
	{
		
		BufferedReader registSelect = null;
		BufferedReader brID = null;
		BufferedReader brPSW = null;
		
		try {

			ReStart : 
			while(true) {

				System.out.println("[*] Manager Regist Start------------------------------------> ");

				registSelect = new BufferedReader(new InputStreamReader(System.in));	
				System.out.print("[*] Manager Regist Select - [1] Admin(X) [2] TopManger(O) [3] GeneralManaer(X) : ");
				String selectNumberStr = registSelect.readLine(); 
				
				switch(selectNumberStr) {
					
					case "2" :		// Top Manger Resist [0x62]
						
						// Manager level is determined at login [ AdminID / TopManagerID / GeneralMangerID ]   
						// In case of TopManger Login : ManagerLevel = [0x63]
						String managerLevelInfoStr = moaGeneralManager;
						
						// 1.1 [Only Client PC] Input ID & PSW (for Regist) 
						brID = new BufferedReader(new InputStreamReader(System.in));	
						System.out.print("[**] 1.1 (ManagerID/PSW-R: Client) Regist ID : ");
						String idStr = brID.readLine();
						byte[] idBytes =idStr.getBytes(charset);
						
						// 1.2 [Only Client PC] Input PSW (for Regist) 
						brPSW = new BufferedReader(new InputStreamReader(System.in));	
						System.out.print("[**] 1.2 (ManagerID/PSW-R: Client) Regist Password : ");
						String passwordStr = brPSW.readLine();
									
						// 1.3 [Client PC -> ManagerAuthServer] ID Exist (for Resigt)
						String idExistMsgStr = moaClientManagerAuthLib.coreIdExistRegistRequestMsgGenProcess(idStr);
						System.out.println("[**] 1.3 (ManagerID/PSW-R: Client PC -> ManagerAuthServer) ID Exist Msg Gen[String] : " + idExistMsgStr);
						System.out.println("-------------------------------------------------------------------------------------");
					
						// 1.4.0.1 [Only ManagerAuthServer] ID Exist check - True or False(Not Exist)  
						String idExistMsgParserResultStr = moaServerManagerAuthLib.coreAuthServerMsgPacketParser(idExistMsgStr, null); // IDExist Msg Parser
						System.out.println("[**] 1.4.0.1 (ManagerID/PSW-R: ManagerAuthServer) ID Exist Msg Parser[String] : " + idExistMsgParserResultStr);
						
						//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
						// 1.4.0.2 [Only ManagerAuthServer] Search managerID in Manager AuthDB  <0x6010 or 0x6011>
						String idExistAuthDBSearchResultStr = managerAuthDBProcess.ManagerAuthDBSearchProcess(idExistMsgParserResultStr); // DB Replace Part
						System.out.println("[**] 1.4.0.2 (ManagerID/PSW-R: ManagerAuthServer) Searh Result of ID Exist in Manager AuthDB[String] : " + idExistAuthDBSearchResultStr);
						//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
						
						/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
						 * AuthDB���� ID Exist Check() : ���� DB�� �����Ͽ� ManagerID ���� ���� Ȯ�� �ʿ� Get [������ 1.4.0.2 ��ü�� DB ���� �κ�]
						 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
					 	   AuthVo authDBIdRecordRead = moaServerAuthDBLib.getUserAuth(idExistMsgParserResultStr);	
					 	
					 	   String idExistFlagStr = "0x6010";
					 	   if (authDBIdRecordRead == null) 					// ID Not Found
					 	   {
					 		   System.out.println("DB Object Not Gen");
							   resultStr = "0x6011";						// 	idExistFlag = "0"
					 	   } else {
							   System.out.println("ID(e-mail) exists. ID & PSW Regist Progress Stop!");
							   resultStr = "0x6010";
					 	   }
					 	~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
					 	 

						if (idExistAuthDBSearchResultStr.equals("0x6010")) {
							System.out.println("Error ---> Manager AUthDB is ID Exist. Resigtration Running Stop!");		
							continue;
						}
						
						// 1.4 [Client PC <- ManagerAuthServer] ID Exist Yes or No Result
						String idExistAckMsgStr = moaServerManagerAuthLib.coreIdExistAckMsgGenProcess(idExistAuthDBSearchResultStr); // No ID Exist
						System.out.println("[**] 1.4 (ManagerID/PSW-R: Client PC <- ManagerAuthServer) ID Exist Ack Msg Gen[String] : " + idExistAckMsgStr);
					 	System.out.println("-------------------------------------------------------------------------------------");

					 	// 1.5.0 [Client PC -> ManagerAuthServer] ID/PSW Regist Request (for Regist)
						String idExistAckParserResultStr = moaClientManagerAuthLib.coreAuthClientMsgPacketParser(idExistAckMsgStr);	// IDExistAck Msg Parser
						System.out.println("[**] 1.5.0 (ManagerID/PSW-R: ManagerAuthServer) ID Exist Ack Result Parser[String] : " + idExistAckParserResultStr);
							
						if(idExistAckParserResultStr.equals("0x6010") ) {
							System.out.println("Error ---> Manager ID(e-mail) exists. ID & PSW Regist Progress Stop!");
							continue;
						}
						
						String idPswRegReqMsgGenStr = moaClientManagerAuthLib.coreIdPswRegistRequestMsgGenProcess(managerLevelInfoStr, idStr, passwordStr);		// ID/PSW RegReq Msg Gen
						System.out.println("[**] 1.5 (ManagerID/PSW-R: Client PC -> ManagerAuthServer) ID/PSW Reg Req Msg Gen[String] : " + idPswRegReqMsgGenStr);
						System.out.println("-------------------------------------------------------------------------------------");
									
						// 1.6.0 [Only ManagerAuthServer] User Auth DB(File) Storage -----> MoaAuthDao.jar Replace ���� ��ü
						String idPswRegReqMsgParserResultStr = moaServerManagerAuthLib.coreAuthServerMsgPacketParser(idPswRegReqMsgGenStr, null);	// PIN Regist Request Parser : Ciphered PIN(Id/Psw) 32Byte Compute
						System.out.println("[**] 1.6.0 (ManagerID/PSW-R: ManagerAuthServer) ID/PSW Reg Req Msg Parser[String] : " + idPswRegReqMsgParserResultStr);	
						
						StringTokenizer idPswRegistReqST = new StringTokenizer(idPswRegReqMsgParserResultStr, "$");
						String managerLevelStr = idPswRegistReqST.nextToken();
						String managerIdBase64Str = idPswRegistReqST.nextToken();
						String saltBase64Str = idPswRegistReqST.nextToken();
						String cipheredPswBase64Str = idPswRegistReqST.nextToken();
						
						String managerIDStr = new String(Base64.decode(managerIdBase64Str));
						
						// Manager AuthDB�� ManagerLevel, ManagerID, SaltBase64Str, CipheredPswBase64Str Insert
						//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
						String managerAuthDBRecordDataGenStr = managerAuthDBProcess.ManagerAuthDBAddProcess(managerLevelStr, managerIDStr, saltBase64Str, cipheredPswBase64Str, 0);
						String managerAuthDBAddResultStr = managerAuthDBProcess.ManagerAuthDBFileStorageProcess(managerAuthDBRecordDataGenStr);
						//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
						/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
						 * Manager AuthDB�� ManagerLevel, ManagerID, SaltBase64Str, CipheredPswBase64Str Insert 
						 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
						   AuthVo authDBPinFieldAdd = new AuthVo();					// MoaMangerAuthDao.jar Call(DB)
						   authDBPinFieldAdd.setManagerLevel("0x62");
						   authDBPinFieldAdd.setManagerId(managerIDStr);
						   authDBPinFieldAdd.setSalt(saltBase64Str);
						   authDBPinFieldAdd.setCipheredPsw(cipheredPswBase64Str);
						   authDBPinFieldAdd.setAuthFailCount(0);
						   authDBPinFieldAdd.setLastModifiedDt(authDBPinFieldAdd.getLast_modified_dt()); 
						   managerAuthDBAddResultStr = moaServerAuthDBLib.addUserAuth(authDBPinFieldAdd);
						~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/  
						if (managerAuthDBAddResultStr.equals("0x6021")) {
							System.out.println("Error ---> ID & PSW Registration Falil. ID & PSW Regist Progress Stop!");
							return;
						}

						// 1.6 [Client PC <- ManagerAuthServer] ID/PSW Regist Response
						String idPswRegResMsgGenStr = moaServerManagerAuthLib.coreIdPswRegistResponseMsgGenProcess(managerAuthDBAddResultStr, true); 	// '0x6020' Regist Success
						System.out.println("[**] 1.6 (ManagerID/PSW-R: Client PC <- ManagerAuthServer) ID/PSW RegRes Msg Gen[String] : " + idPswRegResMsgGenStr);
						System.out.println("-------------------------------------------------------------------------------------");
										
						// 1.7 [OnlyClient PC] : Next Job Process after ID/PSW Regist Result Confirm 
						String idPswRegResParserResultStr = moaClientManagerAuthLib.coreAuthClientMsgPacketParser(idPswRegResMsgGenStr);
						System.out.println("[**] 1.7.0 (ManagerID/PSW-R: Client PC ) ID/PSW Reg Res Result Parser[String] : " + idPswRegResParserResultStr);
							
						if (idPswRegResParserResultStr.equals("ox6021")) {	// Client PC Local Member-ID storage : moaManagerID.dat { managerID="managerID" }
							System.out.println("Error ---> ID & PSW Registration Fail. ID & PSW Regist Pe-Progress");
							continue;
						} else {
							
							System.out.println("[**] 1.7 (ManagerID/PSW-R: Client PC ) ID/PSW Regist Success");
							String managerIDInfoStr = "moaManagerID=" + idStr;
								
							System.out.println("moaManagerID.dat [String] : " + managerIDInfoStr);
							System.out.println("-------------------------------------------------------------------------------------");
						}
									
						break;	
						
					default :		
						break;
						
				} 	//----- Switch case area
			}   	//----- While area	
			
		} catch(Exception e) { }
		
	}	
	
}

