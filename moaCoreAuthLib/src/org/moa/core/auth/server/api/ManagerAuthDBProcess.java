package org.moa.core.auth.server.api;

import java.io.*;
//import java.security.*;

import java.util.StringTokenizer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
//import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.SimpleDateFormat;

//import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.encoders.*;

import org.moa.crypto.coreapi.DigestCoreAPI;
import org.moa.crypto.coreapi.SecureRNGCoreAPI;
import org.moa.crypto.coreapi.SymmetricCoreAPI;


public class ManagerAuthDBProcess {
	
	
	static SecureRNGCoreAPI srng = new SecureRNGCoreAPI();
	static DigestCoreAPI dg = new DigestCoreAPI();
	
	static String ManagerAuthFileName = "moaMANAGERAuthDB.dat"; 
	
	//static MoaServerLogInLib	moaServerLogInLib 	= new MoaServerLogInLib();
	
	public static String ManagerAuthDBAddProcess(String managerLevelStr, String managerIdStr, String saltBase64Str, String cipheredPswBase64Str, int authFailCount) {
		
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
		String LastModifiedDatetime = dateTimeFormat.format (System.currentTimeMillis());
		
		StringBuffer sb = new StringBuffer();
		sb.append(managerLevelStr); 			// Manager Level Info(String)
		sb.append("$");
		sb.append(managerIdStr);				// Member ID(String)
		sb.append("$");
		sb.append(saltBase64Str);				// Salt(Base64:String)
		sb.append("$");
		sb.append(cipheredPswBase64Str);		// Ciphered Pin(Base64:String) 
		sb.append("$");
		sb.append(authFailCount);				// Auth Fail Count(String)
		sb.append("$");
		sb.append(LastModifiedDatetime); 		// Last Modified Date time(String)
		
		return sb.toString();
		
	}
	
	// ID/PSW Common Process
	public static String ManagerAuthDBFileStorageProcess(String storageManagerAuthDataStr) 
	{

		String osName = System.getProperty("os.name").toLowerCase();
		
		File managerAuthFile = new File(ManagerAuthFileName);
		FileWriter managerAuthFileWriter = null;
		
		String recordAddSuccess = "0x6020";
		String recordAddFail 	= "0x6021";
		String result = recordAddFail;
		

		try {

			if ( !(managerAuthFile.exists()) ) {
				
				managerAuthFileWriter = new FileWriter(managerAuthFile, true);
				managerAuthFileWriter.write(storageManagerAuthDataStr);
				managerAuthFileWriter.flush();
			
			} else {
			
				if (osName.startsWith("windows"))	storageManagerAuthDataStr = "\n" 	+ storageManagerAuthDataStr;
				if (osName.startsWith("linux"))		storageManagerAuthDataStr = "\n\r"	+ storageManagerAuthDataStr;

				managerAuthFileWriter = new FileWriter(managerAuthFile, true);
				managerAuthFileWriter.write(storageManagerAuthDataStr);
				managerAuthFileWriter.flush();
				
				result =  recordAddSuccess;

			}
		} catch(IOException e) {
			
		} finally {
			try {
				if(managerAuthFileWriter != null) managerAuthFileWriter.close();  ;
			} catch (IOException e) {}
		}
		
		return result;
		
	}	
	
	
	public static String ManagerAuthDBSearchProcess(String idStr) throws Exception 
	{
		
		File mamagerAuthDBFileName = new File(ManagerAuthFileName);
	    
	    StringTokenizer mamagerAuthDBST = null;
		String atManagerLevelStr = null;
		String atManagerIdStr = null;
		String atSaltBase64Str = null;
		String atCipheredPswBase64Str = null;
		String atAuthFailCountStr = null;
		String atLastModifiedDateTimeStr = null;
		
	    String searchResult = null;
		BufferedReader br = null;

		int searchFlag = 0;
		
		//String idStr = new String(Base64.decode(idBase64Str));
		
	    try {
	    	
	    	br = new BufferedReader(new FileReader(mamagerAuthDBFileName));
	    	String line = null;
	            
	    	while ((line = br.readLine()) != null) {
	    		
	    		mamagerAuthDBST = new StringTokenizer(line, "$"); // String Tokenizer("$")
	    		atManagerLevelStr 		= mamagerAuthDBST.nextToken();
	    		atManagerIdStr 			= mamagerAuthDBST.nextToken();
	    		atSaltBase64Str 		= mamagerAuthDBST.nextToken();
	    		atCipheredPswBase64Str 	= mamagerAuthDBST.nextToken();
	    		atAuthFailCountStr 		= mamagerAuthDBST.nextToken();
	    		atLastModifiedDateTimeStr = mamagerAuthDBST.nextToken();
	    		
	    		if (idStr.equals(atManagerIdStr) ) {
	    			searchResult = "0x6010";	// managerID Exist in Manager AuthDB
	    			searchFlag = 1;
	    			break;
	    		}	
	    	}
	    	
	    	if (searchFlag == 0) {
	    		searchResult = "0x6011";		// managerID not Exist in Manager AuthDB
	    		//throw new Exception("Error --> ManagerID Not Found in Manager AUthDB!");
	    	}
	    	
	    } catch (IOException ioe) {
	    	System.out.println(ioe.getMessage());
	    } finally {
	    	try { 
	    		if (br!=null) br.close(); 
	        } catch (Exception e) {}
	    }
	
	    return searchResult;
	
	}

	public static String ManagerAuthDBGetProcess(String receivedDdBase64Str) throws Exception 
	{
		
		File mamagerAuthDBFileName = new File(ManagerAuthFileName);
	    
	    StringTokenizer mamagerAuthDBST = null;
		String atManagerLevelStr = null;
		String atManagerIdStr = null;
		String atSaltBase64Str = null;
		String atCipheredPswBase64Str = null;
		String atAuthFailCountStr = null;
		String atLastModifiedDateTimeStr = null;
		
	    String managerLevelAndSaltAndCipheredBase64Str = null;
		BufferedReader br = null;

		int searchFlag = 0;
		
		String receivedIdStr = new String(Base64.decode(receivedDdBase64Str));
		//System.out.println("Received Manager ID [String] : " + receivedIdStr);
		
	    try {
	    	
	    	br = new BufferedReader(new FileReader(mamagerAuthDBFileName));
	    	String line = null;
	            
	    	while ((line = br.readLine()) != null) {
	    		
	    		mamagerAuthDBST = new StringTokenizer(line, "$"); // String Tokenizer("$")
	    		atManagerLevelStr 		= mamagerAuthDBST.nextToken();
	    		atManagerIdStr 			= mamagerAuthDBST.nextToken();
	    		atSaltBase64Str 		= mamagerAuthDBST.nextToken();
	    		atCipheredPswBase64Str 	= mamagerAuthDBST.nextToken();
	    		atAuthFailCountStr 		= mamagerAuthDBST.nextToken();
	    		atLastModifiedDateTimeStr = mamagerAuthDBST.nextToken();
	    		
	    		//System.out.println("Manager AuthDB Manager ID [String] : " + atManagerIdStr);
	    		
	    		if (receivedIdStr.equals(atManagerIdStr) ) {
	    			managerLevelAndSaltAndCipheredBase64Str = atManagerLevelStr + "$" + atSaltBase64Str + "$" + atCipheredPswBase64Str;
	    			searchFlag = 1;
	    			break;
	    		}	
	    	}
	    	
	    	//System.out.println("searchFlag : " + searchFlag);
	    	if (searchFlag == 0) {
	    		System.out.println("Error ---> Received ManagerID is not found in the Manager AUthDB!");
	    		managerLevelAndSaltAndCipheredBase64Str = null;
	    		//throw new Exception("Error --> Received ManagerID Not Found in Manager AUthDB!");
	    	}
	    	
	    } catch (IOException ioe) {
	    	System.out.println(ioe.getMessage());
	    } finally {
	    	try { 
	    		if (br!=null) br.close(); 
	        } catch (Exception e) {}
	    }
	    
	    //System.out.println("managerLevelAndSaltAndCipheredBase64Str [String] : " + managerLevelAndSaltAndCipheredBase64Str);
	
	    return managerLevelAndSaltAndCipheredBase64Str;
	
	}
	
	
	public static String ManagerAuthDBRemoveRecordProcess(String ManagerAuthFileName, String removeManagerIdrecordStr) {

		String authDBDeleteSF = "0x6051";
		
		StringTokenizer mamagerAuthDBST = null;
		String atManagerLevelStr = null;
		String atManagerIdStr = null;
		String atSaltBase64Str = null;
		String atCipheredPswBase64Str = null;
		String atAuthFailCountStr = null;
		String atLastModifiedDateTimeStr = null;
		
        try {

        	File mamagerAuthDBFileName = new File(ManagerAuthFileName);
            //File inFile = new File(file);

            if (!mamagerAuthDBFileName.isFile()) {
                System.out.println("Parameter is not an existing file");
                authDBDeleteSF = "0x6053";
                return authDBDeleteSF;
            }

            // Construct the new file that will later be renamed to the original
            // filename.
            File tempFile = new File(mamagerAuthDBFileName.getAbsolutePath() + ".tmp");

            BufferedReader br = new BufferedReader(new FileReader(ManagerAuthFileName));
            PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

            String line = null;

            // Read from the original file and write to the new
            // unless content matches data to be removed.
            while ((line = br.readLine()) != null) {
            	
            	mamagerAuthDBST = new StringTokenizer(line, "$"); // String Tokenizer("$")
	    		atManagerLevelStr 		= mamagerAuthDBST.nextToken();
	    		atManagerIdStr 			= mamagerAuthDBST.nextToken();
	    		atSaltBase64Str 		= mamagerAuthDBST.nextToken();
	    		atCipheredPswBase64Str 	= mamagerAuthDBST.nextToken();
	    		atAuthFailCountStr 		= mamagerAuthDBST.nextToken();
	    		atLastModifiedDateTimeStr = mamagerAuthDBST.nextToken();

                if (!atManagerIdStr.equals(removeManagerIdrecordStr)) {

                    pw.println(line);
                    pw.flush();
                } else {
                	authDBDeleteSF = "0x6050";
                }
            }
            pw.close();
            br.close();

            // Delete the original file
            if (!mamagerAuthDBFileName.delete()) {
                System.out.println("Could not delete file");
                authDBDeleteSF = "0x6054";
                return authDBDeleteSF;
            }

            // Rename the new file to the filename the original file had.
            if (!tempFile.renameTo(mamagerAuthDBFileName))
                System.out.println("Could not rename file");
            
            if (!authDBDeleteSF.equals("0x6050")) {
            	System.out.println(removeManagerIdrecordStr + " UnRegist ManagerID not Exist");
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return authDBDeleteSF;
    }

	
}