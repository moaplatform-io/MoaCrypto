import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.Security;
import java.util.StringTokenizer;

//import org.bouncycastle.util.encoders.Base64;
import org.moa.core.auth.client.api.MoaClientCoreAuthLib;
import org.moa.core.auth.server.api.MoaServerCoreAuthLib;
import org.moa.core.auth.server.api.FranchiseAuthDBProcess;
import org.moa.core.auth.server.api.ManagerAuthDBProcess;

public class FranchiseChangePswTest {
	
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
	
    public static void main(String[] args) {
    	
    	// Process Progress After LogIn Success
    	
    	BufferedReader loginManagerIdBr = null;
    	BufferedReader currentPswBr = null;
    	BufferedReader changePswBr = null;
    	
		try {

			while(true) {

				loginManagerIdBr = new BufferedReader(new InputStreamReader(System.in));	
				System.out.print("[*] LogIn FranchiseID Input(실제는 로그인 상태로 입력 불필요) : ");
				String loginManagerIDStr = loginManagerIdBr.readLine();
				
				System.out.println("[*] Franchise Password Change Start [ Runnable After ID/PSW Login --------------------> ");
				
				// 3.1 [Client PC] Exist Psw & Change(new) Psw Input 
				currentPswBr = new BufferedReader(new InputStreamReader(System.in));	
				System.out.print("[*] 3.1.0 Current PSW Input : ");
				String currentPswStr = currentPswBr.readLine();
				
				changePswBr = new BufferedReader(new InputStreamReader(System.in));	
				System.out.print("[*] 3.1.1 Change PSW Input  : ");
				String changePswStr = changePswBr.readLine();
				
				// 3.2 [Client PC -> FranchiseAuthServer] Auto FranchiseID Load, IdPsw Change Request Msg Gen(Exist Psw -> New Psw Change) 
				String changePswRequestManagerIdMsgGenStr = moaClientFranchiseAuthLib.coreIdPswChangePswRequestMsgGenProcess(loginManagerIDStr, currentPswStr ,changePswStr); // Change PSW Request
				System.out.println("[**] 3.2 (ChangePSW: Client PC -> ManagerAuthServer) managerID Change PSW Request Msg Gen[String] : " + changePswRequestManagerIdMsgGenStr);
			 	System.out.println("-------------------------------------------------------------------------------------");
				
			 	// 3.3 [Client PC <- FranchiseAuthServer] ID/PSW Change PSW Request Parser & Change PSW Response Msg Gen
			 	String changePswRequestMsgParserResultStr = moaServerFranchiseAuthLib.coreAuthServerMsgPacketParser(changePswRequestManagerIdMsgGenStr, null); // Change PSW Msg Parser
			 	System.out.println("[**] 3.3.0 (ChangePSW: FranchiseAuthServer) [franchiseID] Change PSW Request Msg Parser Result[String] : " + changePswRequestMsgParserResultStr);
			 	
			 	StringTokenizer changePswReqParserResultST = new StringTokenizer(changePswRequestMsgParserResultStr, "$");
				String franchiseIdBase64Str = changePswReqParserResultST.nextToken();
				String currentHashPswBase64Str = changePswReqParserResultST.nextToken();
				String currentHmacPswBase64Str = changePswReqParserResultST.nextToken();
				String newHashPswBase64Str = changePswReqParserResultST.nextToken();
				String newHmacPswBase64Str = changePswReqParserResultST.nextToken();
				
				//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				//~~~[ AuthDB Process Part : Get MangerLevle, Salt and CipheredPsw of managerID Record in MangerAuthDB ]~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				String franchiseAuthDBRecordDataGenStr = franchiseAuthDBProcess.franchiseAuthDBGetProcess(franchiseIdBase64Str);
				StringTokenizer serverAutnDBGetST = new StringTokenizer(franchiseAuthDBRecordDataGenStr, "$");
				//String authDBManagerLevelStr = serverAutnDBGetST.nextToken();
				String authDBSaltBase64Str = serverAutnDBGetST.nextToken();
				String authDBCipheredPswBase64Str = serverAutnDBGetST.nextToken();
				//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				String currentPswVerifySFStr = null;
				String currentCipheredPswBase64Str = moaServerFranchiseAuthLib.coreIdPswLogInAuthInfoGen(authDBSaltBase64Str, currentHashPswBase64Str, currentHmacPswBase64Str);
				if (currentCipheredPswBase64Str.equals(authDBCipheredPswBase64Str)) { 
					currentPswVerifySFStr = "0x6060";		// PSW Verify Success of manager ID
				} else { 
					currentPswVerifySFStr = "0x6061";		// PSW Verify Fail of manager ID
					System.out.println("Error ---> franchiseID has not exist the Current PSW. Current Psw Re-Input!");
				}
			 	
				String newCipheredUpdateSFStr = null;
				if (currentPswVerifySFStr.equals("0x6060")) {
					String newCipheredPswBase64Str = moaServerFranchiseAuthLib.coreIdPswLogInAuthInfoGen(authDBSaltBase64Str, newHashPswBase64Str, newHmacPswBase64Str);
					System.out.println("newCipheredPswBase64Str : " +  newCipheredPswBase64Str);
			 		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			 		//~~~[ AuthDB Process Part : Update for Ciphered PSW of managerID Record in MangerAuthDB ]~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
					//AuthVo authDBFPFieldUpdate = moaServerAuthDBLib.getUserAuth(new String(Base64.decode(managerIdBase64Str)));	// MoaAuthDao.jar Call(DB)
					//authDBFPFieldUpdate.setCipheredPsw(newCipheredPswBase64Str);
					//moaServerAuthDBLib.updateUserAuth(authDBFPFieldUpdate); 
			 		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
					newCipheredUpdateSFStr = "0x06070";		
				} else {
					newCipheredUpdateSFStr = "0x06071";	
					System.out.println("Error ---> CipherdPsw Update to Manager AuthDB Failed. Exist Psw Re-Input!");
				}
			 	String chagePswResponseManagerIdMsgGenStr = moaServerFranchiseAuthLib.coreIdPswChangePswResponseMsgGenProcess(currentPswVerifySFStr, newCipheredUpdateSFStr) ;
			 	System.out.println("[**] 3.3 (ChangePSW: Client PC <- FranchiseAuthServer) [managerID] Change Psw Response Msg Gen[String] : " + chagePswResponseManagerIdMsgGenStr);
			 	
			 	// 3.4 [Only Client PC] ID/PSW unRegist Response Msg Parser
			 	String chagePswResponseMsgParserResultStr = moaClientFranchiseAuthLib.coreAuthClientMsgPacketParser(chagePswResponseManagerIdMsgGenStr); // 
			 	System.out.println("[**] 3.4.0 (ChangePSW: Client PC) [franchiseID] Change Psw Response Response Msg Parser Result[String] : " + chagePswResponseMsgParserResultStr);

			 	StringTokenizer chagePswResponseMsgParserResultST = new StringTokenizer(chagePswResponseMsgParserResultStr, "$");
				String existPswVerifyResultStr = chagePswResponseMsgParserResultST.nextToken();
				String cipheredPswUpdateResultStr = chagePswResponseMsgParserResultST.nextToken();
			 	if (existPswVerifyResultStr.equals("0x6060") && cipheredPswUpdateResultStr.equals("0x06070") ) {
			 		System.out.println("[**] 3.4 (ChangePSW: Client PC) [franchiseID] Change Psw Success");			 		
			 	} else {
			 		System.out.println("[**] 3.4 (ChangePSW: Client PC) [franchiseID] Change Psw Fail. Change Psw Re-try!");	
			 	}
			 	
			}   //----- While area	
			
		} catch(Exception e) { }
    	
    }
}
