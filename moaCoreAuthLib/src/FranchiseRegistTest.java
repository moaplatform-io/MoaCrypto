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
import org.moa.core.auth.server.api.FranchiseAuthDBProcess;  // Delete after Test


public class FranchiseRegistTest {
	
	
	
	
	public static void main(String args[]) 
		   throws NoSuchProviderException, NullPointerException, Exception
	{
		// Define about MOA B2C Manager System Properties File
		String moaB2CCorePropertiesFileName = "moaB2CCore.properties";
			
		// Constructor Create MOA Franchise Lib for Registration and LogIn of ID & Password 
		MoaClientCoreAuthLib 	moaClientFranchiseAuthLib = new MoaClientCoreAuthLib(moaB2CCorePropertiesFileName);
		MoaServerCoreAuthLib 	moaServerFranchiseAuthLib = new MoaServerCoreAuthLib(moaB2CCorePropertiesFileName);
		FranchiseAuthDBProcess	franchiseAuthDBProcess 		= new FranchiseAuthDBProcess();

		Charset charset = Charset.forName("UTF-8");
		String tokenChar = "$";	// Tokenizer String ("$")

	
		java.security.Provider p = new org.bouncycastle.jce.provider.BouncyCastleProvider();
			Security.addProvider(p);
			
		

		
		BufferedReader registSelect = null;
		
		BufferedReader brID = null;
		BufferedReader brPSW = null;
		
		try {

			ReStart : 
			while(true) {

				System.out.println("[*] Franchise Regist Start------------------------------------> ");

				registSelect = new BufferedReader(new InputStreamReader(System.in));	
				System.out.print("[*] Franchise Regist Select - [1] Regist(O) : ");
				String selectNumberStr = registSelect.readLine(); 
				
				switch(selectNumberStr) {
					
					case "1" :
					
						// 1.1 [Only Client PC] Input ID & PSW (for Regist) 
						brID = new BufferedReader(new InputStreamReader(System.in));	
						System.out.print("[**] 1.1 (FranchiseID/PSW-R: Client) Regist ID : ");
						String idStr = brID.readLine();
						byte[] idBytes =idStr.getBytes(charset);
						
						// 1.2 [Only Client PC] Input PSW (for Regist) 
						brPSW = new BufferedReader(new InputStreamReader(System.in));	
						System.out.print("[**] 1.2 (FranchiseID/PSW-R: Client) Regist Password : ");
						String passwordStr = brPSW.readLine();
									
						// 1.3 [Client PC -> FranchiseAuthServer] ID Exist (for Resigt)
						String idExistMsgStr = moaClientFranchiseAuthLib.coreIdExistRegistRequestMsgGenProcess(idStr);
						System.out.println("[**] 1.3 (FranchiseID/PSW-R: Client PC -> FranchseAuthServer) ID Exist Msg Gen[String] : " + idExistMsgStr);
						System.out.println("-------------------------------------------------------------------------------------");
					
						// 1.4.0.1 [Only FranchiseAuthServer] ID Exist check - True or False(Not Exist)  
						String idExistMsgParserResultStr = moaServerFranchiseAuthLib.coreAuthServerMsgPacketParser(idExistMsgStr, null); // IDExist Msg Parser
						System.out.println("[**] 1.4.0.1 (FranchiseID/PSW-R: FranchseAuthServer) ID Exist Msg Parser[String] : " + idExistMsgParserResultStr);
						
						//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
						// 1.4.0.2 [Only FranchiseAuthServer] Search managerID in Manager AuthDB  <0x6010 or 0x6011>
						String idExistAuthDBSearchResultStr = franchiseAuthDBProcess.franchiseAuthDBSearchProcess(idExistMsgParserResultStr); // DB Replace Part
						System.out.println("[**] 1.4.0.2 (FranchiseID/PSW-R: FranchseAuthServer) Searh Result of ID Exist in Manager AuthDB[String] : " + idExistAuthDBSearchResultStr);
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
							System.out.println("Error ---> Franchise AUthDB is ID Exist. Resigtration Running Stop!");		
							continue;
						}
						
						// 1.4 [Client PC <- FranchiseAuthServer] ID Exist Yes or No Result
						String idExistAckMsgStr = moaServerFranchiseAuthLib.coreIdExistAckMsgGenProcess(idExistAuthDBSearchResultStr); // No ID Exist
						System.out.println("[**] 1.4 (FranchiseID/PSW-R: Client PC <- FranchiseAuthServer) ID Exist Ack Msg Gen[String] : " + idExistAckMsgStr);
					 	System.out.println("-------------------------------------------------------------------------------------");

					 	// 1.5.0 [Client PC -> FranchiseAuthServer] ID/PSW Regist Request (for Regist)
						String idExistAckParserResultStr = moaClientFranchiseAuthLib.coreAuthClientMsgPacketParser(idExistAckMsgStr);	// IDExistAck Msg Parser
						System.out.println("[**] 1.5.0 (FranchiseID/PSW-R: FranchiseAuthServer) ID Exist Ack Result Parser[String] : " + idExistAckParserResultStr);
							
						if(idExistAckParserResultStr.equals("0x6010") ) {
							System.out.println("Error ---> Franchise ID(e-mail) exists. ID & PSW Regist Progress Stop!");
							continue;
						}
						
						String idPswRegReqMsgGenStr = moaClientFranchiseAuthLib.coreIdPswRegistRequestMsgGenProcess(null, idStr, passwordStr);		// ID/PSW RegReq Msg Gen
						System.out.println("[**] 1.5 (FranchiseID/PSW-R: Client PC -> FranchiseAuthServer) ID/PSW Reg Req Msg Gen[String] : " + idPswRegReqMsgGenStr);
						System.out.println("-------------------------------------------------------------------------------------");
									
						// 1.6.0 [Only FranchiseAuthServer] User Auth DB(File) Storage -----> MoaAuthDao.jar Replace ���� ��ü
						String idPswRegReqMsgParserResultStr = moaServerFranchiseAuthLib.coreAuthServerMsgPacketParser(idPswRegReqMsgGenStr, null);	// PIN Regist Request Parser : Ciphered PIN(Id/Psw) 32Byte Compute
						System.out.println("[**] 1.6.0 (FranchiseID/PSW-R: FranchiseAuthServer) ID/PSW Reg Req Msg Parser[String] : " + idPswRegReqMsgParserResultStr);	
						
						StringTokenizer idPswRegistReqST = new StringTokenizer(idPswRegReqMsgParserResultStr, "$");
						//String managerLevelStr = idPswRegistReqST.nextToken();
						String franchiseIdBase64Str = idPswRegistReqST.nextToken();
						String saltBase64Str = idPswRegistReqST.nextToken();
						String cipheredPswBase64Str = idPswRegistReqST.nextToken();
						
						String franchiseIDStr = new String(Base64.decode(franchiseIdBase64Str));
						
						// Franchise AuthDB�� FranchiseID, SaltBase64Str, CipheredPswBase64Str Insert
						//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
						String franchiseAuthDBRecordDataGenStr = franchiseAuthDBProcess.franchiseAuthDBAddProcess(franchiseIDStr, saltBase64Str, cipheredPswBase64Str, 0);
						String franchiseAuthDBAddResultStr = franchiseAuthDBProcess.franchiseAuthDBFileStorageProcess(franchiseAuthDBRecordDataGenStr);
						//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
						/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
						 * Franchise AuthDB�� FranchiseID, SaltBase64Str, CipheredPswBase64Str Insert 
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
						if (franchiseAuthDBAddResultStr.equals("0x6021")) {
							System.out.println("Error ---> ID & PSW Registration Falil. ID & PSW Regist Progress Stop!");
							return;
						}

						// 1.6 [Client PC <- FranchiseAuthServer] ID/PSW Regist Response
						String idPswRegResMsgGenStr = moaServerFranchiseAuthLib.coreIdPswRegistResponseMsgGenProcess(franchiseAuthDBAddResultStr, false); 	// '0x6020' Regist Success
						System.out.println("[**] 1.6 (FranchiseID/PSW-R: Client PC <- FranchiseAuthServer) ID/PSW RegRes Msg Gen[String] : " + idPswRegResMsgGenStr);
						System.out.println("-------------------------------------------------------------------------------------");
										
						// 1.7 [OnlyClient PC] : Next Job Process after ID/PSW Regist Result Confirm 
						String idPswRegResParserResultStr = moaClientFranchiseAuthLib.coreAuthClientMsgPacketParser(idPswRegResMsgGenStr);
						System.out.println("[**] 1.7.0 (FranchiseID/PSW-R: Client PC ) ID/PSW Reg Res Result Parser[String] : " + idPswRegResParserResultStr);
							
						if (idPswRegResParserResultStr.equals("ox6021")) {	// Client PC Local Member-ID storage : moaManagerID.dat { managerID="managerID" }
							System.out.println("Error ---> ID & PSW Registration Fail. ID & PSW Regist Pe-Progress");
							continue;
						} else {
							
							System.out.println("[**] 1.7 (FranchiseID/PSW-R: Client PC ) ID/PSW Regist Success");
							String franchiseIDInfoStr = "moaFranchiseID=" + idStr;
								
							System.out.println("moaFranchiseID.dat [String] : " + franchiseIDInfoStr);
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

