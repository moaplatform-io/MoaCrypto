import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.Security;

import org.moa.core.auth.client.api.MoaClientCoreAuthLib;
import org.moa.core.auth.server.api.MoaServerCoreAuthLib;
import org.moa.core.auth.server.api.FranchiseAuthDBProcess;
import org.moa.core.auth.server.api.ManagerAuthDBProcess;  // Delete after Test

public class FranchiseUnRegistTest {
	
	static {
		
		java.security.Provider p = new org.bouncycastle.jce.provider.BouncyCastleProvider();
		Security.addProvider(p);
		
	}
	
	// Define about MOA B2C Manager System Properties File
	static String moaB2CCorePropertiesFileName = "moaB2CCore.properties";
		
	// Constructor Create MOA Franchise Lib for Registration and LogIn of ID & Password 
	static MoaClientCoreAuthLib 	moaClientFranchiseAuthLib = new MoaClientCoreAuthLib(moaB2CCorePropertiesFileName);
	static MoaServerCoreAuthLib 	moaServerFranchiseAuthLib = new MoaServerCoreAuthLib(moaB2CCorePropertiesFileName);
	static FranchiseAuthDBProcess	franchiseAuthDBProcess 	  = new FranchiseAuthDBProcess();
	
	static String FranchiseAuthFileName = "moaFRANCHISEAuthDB.dat"; 

	static Charset charset = Charset.forName("UTF-8");
	
    public static void main(String[] args) {
    	
    	// Process Progress After LogIn Success
    	
    	BufferedReader unRegistManagerIDStr = null;
    	
		try {

			while(true) {
				
				System.out.println("[*] Franchise UnRegist(Remove) Start [ Runnable After ID/PSW Login --------------------> ");

				unRegistManagerIDStr = new BufferedReader(new InputStreamReader(System.in));	
				System.out.print("[*] UnRegist FranchiseID Input : ");
				String removeTargerManagerIDStr = unRegistManagerIDStr.readLine();
				
				if (removeTargerManagerIDStr.isEmpty()) {
					System.out.println("UnRegist FranchiseID is Null Input. UnRegist Re-Try!");
					continue;
				}
				
				// 4.1 [Client PC -> FranchiseAuthServer] Input ID for UnRegist 
				String unRegistRequestManagerIdMsgGenStr = moaClientFranchiseAuthLib.coreIdPswUnRegistRequestMsgGenProcess(removeTargerManagerIDStr); // No ID Exist
				System.out.println("[**] 4.1 (UnRegist: Client PC -> FranchiseAuthServer) franchiseID UnRegist Request Msg Gen[String] : " + unRegistRequestManagerIdMsgGenStr);
			 	System.out.println("-------------------------------------------------------------------------------------");
				
			 	// 4.2 [Client PC <- FranchiseAuthServer] ID/PSW unRegist Request Parser & unRegist Response Msg Gen
			 	String unRegistManagerIdMsgParserResultStr = moaServerFranchiseAuthLib.coreAuthServerMsgPacketParser(unRegistRequestManagerIdMsgGenStr, null); // UnRegist Msg Parser
			 	System.out.println("[**] 4.2.0 (UnRegist: Client PC <- FranchiseAuthServer) franchiseID UnRegist Request Msg Parser Result[String] : " + unRegistManagerIdMsgParserResultStr);
			 	
			 	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			 	//~~~[ AuthDB Process Part : Remove managerID Record in MangerAuthDB ]~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			 	String managerIdAuthDBRemoveResultStr = franchiseAuthDBProcess.franchiseAuthDBRemoveRecordProcess(FranchiseAuthFileName, unRegistManagerIdMsgParserResultStr);
			 	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			 	
			 	String unRegistResponseManagerIdMsgGenStr = moaServerFranchiseAuthLib.coreIdPswUnRegistResponseMsgGenProcess(managerIdAuthDBRemoveResultStr) ;
			 	System.out.println("[**] 4.2 (UnRegist: Client PC <- FranchiseAuthServer) franchiseID UnRegist Response Msg Gen[String] : " + unRegistResponseManagerIdMsgGenStr);
			 	
			 	// 4.3 [Only Client PC] ID/PSW unRegist Response Msg Parser
			 	String unRegistResponseMsgParserResultStr = moaClientFranchiseAuthLib.coreAuthClientMsgPacketParser(unRegistResponseManagerIdMsgGenStr); // 
			 	System.out.println("[**] 4.3 (UnRegist: Client PC) franchiseID UnRegist Response Msg Parser Result[String] : " + unRegistResponseMsgParserResultStr);
			 	
			 	if (unRegistResponseMsgParserResultStr.equals("0x6051")) {
			 		System.out.println("franchiseID UnRegist Fail. UnRegist Re-try!");
			 		continue;
			 		
			 	} else if (unRegistResponseMsgParserResultStr.equals("0x6050")) {
			 		System.out.println("franchiseID UnRegist Success.");
			 	}

			}   //----- While area	
			
		} catch(Exception e) { }
		
    }
}
