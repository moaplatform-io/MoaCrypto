import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.Security;

import org.moa.core.auth.client.api.MoaClientCoreAuthLib;
import org.moa.core.auth.server.api.MoaServerCoreAuthLib;
import org.moa.core.auth.server.api.ManagerAuthDBProcess;  // Delete after Test

public class ManagerUnRegistTest {
	
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

	// Define about MOA Message Identifier(MID) Parsing for PIN(ID & Password) 
	protected static final String moaAdminManager	= "0x61";
	protected static final String moaTopManager		= "0x62";
	protected static final String moaGeneralManager	= "0x63";

	static Charset charset = Charset.forName("UTF-8");
	static String ManagerAuthFileName = "moaMANAGERAuthDB.dat"; 

	
    public static void main(String[] args) {
    	
    	// Process Progress After LogIn Success
    	
    	BufferedReader unRegistManagerIDStr = null;
    	
		try {

			while(true) {
				
				System.out.println("[*] Manager UnRegist(Remove) Start [ Runnable After ID/PSW Login --------------------> ");

				unRegistManagerIDStr = new BufferedReader(new InputStreamReader(System.in));	
				System.out.print("[*] UnRegist ManagerID Input : ");
				String removeTargerManagerIDStr = unRegistManagerIDStr.readLine();
				
				if (removeTargerManagerIDStr.isEmpty()) {
					System.out.println("UnRegist ManagerID is Null Input. UnRegist Re-Try!");
					continue;
				}
				
				// 4.1 [Client PC -> ManagerAuthServer] Input ID for UnRegist 
				String unRegistRequestManagerIdMsgGenStr = moaClientManagerAuthLib.coreIdPswUnRegistRequestMsgGenProcess(removeTargerManagerIDStr); // No ID Exist
				System.out.println("[**] 4.1 (UnRegist: Client PC -> ManagerAuthServer) managerID UnRegist Request Msg Gen[String] : " + unRegistRequestManagerIdMsgGenStr);
			 	System.out.println("-------------------------------------------------------------------------------------");
				
			 	// 4.2 [Client PC <- ManagerAuthServer] ID/PSW unRegist Request Parser & unRegist Response Msg Gen
			 	String unRegistManagerIdMsgParserResultStr = moaServerManagerAuthLib.coreAuthServerMsgPacketParser(unRegistRequestManagerIdMsgGenStr, null); // UnRegist Msg Parser
			 	System.out.println("[**] 4.2.0 (UnRegist: Client PC <- ManagerAuthServer) managerID UnRegist Request Msg Parser Result[String] : " + unRegistManagerIdMsgParserResultStr);
			 	
			 	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			 	//~~~[ AuthDB Process Part : Remove managerID Record in MangerAuthDB ]~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			 	String managerIdAuthDBRemoveResultStr = managerAuthDBProcess.ManagerAuthDBRemoveRecordProcess(ManagerAuthFileName, unRegistManagerIdMsgParserResultStr);
			 	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			 	
			 	String unRegistResponseManagerIdMsgGenStr = moaServerManagerAuthLib.coreIdPswUnRegistResponseMsgGenProcess(managerIdAuthDBRemoveResultStr) ;
			 	System.out.println("[**] 4.2 (UnRegist: Client PC <- ManagerAuthServer) managerID UnRegist Response Msg Gen[String] : " + unRegistResponseManagerIdMsgGenStr);
			 	
			 	// 4.3 [Only Client PC] ID/PSW unRegist Response Msg Parser
			 	String unRegistResponseMsgParserResultStr = moaClientManagerAuthLib.coreAuthClientMsgPacketParser(unRegistResponseManagerIdMsgGenStr); // 
			 	System.out.println("[**] 4.3 (UnRegist: Client PC) managerID UnRegist Response Msg Parser Result[String] : " + unRegistResponseMsgParserResultStr);
			 	
			 	if (unRegistResponseMsgParserResultStr.equals("0x6051")) {
			 		System.out.println("managerID UnRegist Fail. UnRegist Re-try!");
			 		continue;
			 		
			 	} else if (unRegistResponseMsgParserResultStr.equals("0x6050")) {
			 		System.out.println("managerID UnRegist Success.");
			 	}

			}   //----- While area	
			
		} catch(Exception e) { }
		
    }
}
