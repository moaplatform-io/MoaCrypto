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


public class FranchiseAuthDBProcess {
	
	
	static SecureRNGCoreAPI srng = new SecureRNGCoreAPI();
	static DigestCoreAPI dg = new DigestCoreAPI();
	
	static String FranchiseAuthFileName = "moaFRANCHISEAuthDB.dat"; 
	
	public static String franchiseAuthDBAddProcess(String franchiseIdStr, String saltBase64Str, String cipheredPswBase64Str, int authFailCount) {
		
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
		String LastModifiedDatetime = dateTimeFormat.format (System.currentTimeMillis());
		
		StringBuffer sb = new StringBuffer();
		sb.append(franchiseIdStr);				// Franchise ID(String)
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
	public static String franchiseAuthDBFileStorageProcess(String storageFranchiseAuthDataStr) 
	{

		String osName = System.getProperty("os.name").toLowerCase();
		
		File franchiseAuthFile = new File(FranchiseAuthFileName);
		FileWriter franchiseAuthFileWriter = null;
		
		String recordAddSuccess = "0x6020";
		String recordAddFail 	= "0x6021";
		String result = recordAddFail;
		

		try {

			if ( !(franchiseAuthFile.exists()) ) {
				
				franchiseAuthFileWriter = new FileWriter(franchiseAuthFile, true);
				franchiseAuthFileWriter.write(storageFranchiseAuthDataStr);
				franchiseAuthFileWriter.flush();
			
			} else {
			
				if (osName.startsWith("windows"))	storageFranchiseAuthDataStr = "\n" 	    + storageFranchiseAuthDataStr;
				if (osName.startsWith("linux"))		storageFranchiseAuthDataStr = "\n\r"	+ storageFranchiseAuthDataStr;

				franchiseAuthFileWriter = new FileWriter(franchiseAuthFile, true);
				franchiseAuthFileWriter.write(storageFranchiseAuthDataStr);
				franchiseAuthFileWriter.flush();
				
				result =  recordAddSuccess;

			}
		} catch(IOException e) {
			
		} finally {
			try {
				if(franchiseAuthFileWriter != null) franchiseAuthFileWriter.close();  ;
			} catch (IOException e) {}
		}
		
		return result;
		
	}	
	
	
	public static String franchiseAuthDBSearchProcess(String idStr) throws Exception 
	{
		
		File franchiseAuthDBFileName = new File(FranchiseAuthFileName);
	    
	    StringTokenizer franchiseAuthDBST = null;
		String atFranchiseIdStr = null;
		String atSaltBase64Str = null;
		String atCipheredPswBase64Str = null;
		String atAuthFailCountStr = null;
		String atLastModifiedDateTimeStr = null;
		
	    String searchResult = null;
		BufferedReader br = null;

		int searchFlag = 0;
		
		//String idStr = new String(Base64.decode(idBase64Str));
		
	    try {
	    	
	    	br = new BufferedReader(new FileReader(franchiseAuthDBFileName));
	    	String line = null;
	            
	    	while ((line = br.readLine()) != null) {
	    		
	    		franchiseAuthDBST = new StringTokenizer(line, "$"); // String Tokenizer("$")
	    		atFranchiseIdStr 			= franchiseAuthDBST.nextToken();
	    		atSaltBase64Str 		= franchiseAuthDBST.nextToken();
	    		atCipheredPswBase64Str 	= franchiseAuthDBST.nextToken();
	    		atAuthFailCountStr 		= franchiseAuthDBST.nextToken();
	    		atLastModifiedDateTimeStr = franchiseAuthDBST.nextToken();
	    		
	    		if (idStr.equals(atFranchiseIdStr) ) {
	    			searchResult = "0x6010";	// FranchiseID Exist in Franchise AuthDB
	    			searchFlag = 1;
	    			break;
	    		}	
	    	}
	    	
	    	if (searchFlag == 0) {
	    		searchResult = "0x6011";		// FranchiseID not Exist in Franchise AuthDB
	    		//throw new Exception("Error --> FranchiseID Not Found in Franchise AUthDB!");
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

	public static String franchiseAuthDBGetProcess(String receivedDdBase64Str) throws Exception 
	{
		
		File franchiseAuthDBFileName = new File(FranchiseAuthFileName);
	    
	    StringTokenizer franchiseAuthDBST = null;
		String atFranchiseIdStr = null;
		String atSaltBase64Str = null;
		String atCipheredPswBase64Str = null;
		String atAuthFailCountStr = null;
		String atLastModifiedDateTimeStr = null;
		
	    String saltAndCipheredBase64Str = null;
		BufferedReader br = null;

		int searchFlag = 0;
		
		String receivedIdStr = new String(Base64.decode(receivedDdBase64Str));
		//System.out.println("Received Franchise ID [String] : " + receivedIdStr);
		
	    try {
	    	
	    	br = new BufferedReader(new FileReader(franchiseAuthDBFileName));
	    	String line = null;
	            
	    	while ((line = br.readLine()) != null) {
	    		
	    		franchiseAuthDBST = new StringTokenizer(line, "$"); // String Tokenizer("$")
	    		atFranchiseIdStr 			= franchiseAuthDBST.nextToken();
	    		atSaltBase64Str 			= franchiseAuthDBST.nextToken();
	    		atCipheredPswBase64Str 		= franchiseAuthDBST.nextToken();
	    		atAuthFailCountStr 			= franchiseAuthDBST.nextToken();
	    		atLastModifiedDateTimeStr 	= franchiseAuthDBST.nextToken();
	    		
	    		//System.out.println("Franchise AuthDB Franchise ID [String] : " + atFranchiseIdStr);
	    		
	    		if (receivedIdStr.equals(atFranchiseIdStr) ) {
	    			saltAndCipheredBase64Str = atSaltBase64Str + "$" + atCipheredPswBase64Str;
	    			searchFlag = 1;
	    			break;
	    		}	
	    	}
	    	
	    	//System.out.println("searchFlag : " + searchFlag);
	    	if (searchFlag == 0) {
	    		System.out.println("Error ---> Received FranchiseID is not found in the Franchise AUthDB!");
	    		saltAndCipheredBase64Str = null;
	    		//throw new Exception("Error --> Received FranchiseID Not Found in Franchise AUthDB!");
	    	}
	    	
	    } catch (IOException ioe) {
	    	System.out.println(ioe.getMessage());
	    } finally {
	    	try { 
	    		if (br!=null) br.close(); 
	        } catch (Exception e) {}
	    }
	    
	    return saltAndCipheredBase64Str;
	
	}
	
	
	public static String franchiseAuthDBRemoveRecordProcess(String franchiseAuthFileName, String removeFranchiseIdrecordStr) {

		String authDBDeleteSF = "0x6051";
		
		StringTokenizer franchiseAuthDBST = null;
		String atFranchiseIdStr = null;
		String atSaltBase64Str = null;
		String atCipheredPswBase64Str = null;
		String atAuthFailCountStr = null;
		String atLastModifiedDateTimeStr = null;
		
        try {

        	File franchiseAuthDBFileName = new File(FranchiseAuthFileName);
            //File inFile = new File(file);

            if (!franchiseAuthDBFileName.isFile()) {
                System.out.println("Parameter is not an existing file");
                authDBDeleteSF = "0x6053";
                return authDBDeleteSF;
            }

            // Construct the new file that will later be renamed to the original
            // filename.
            File tempFile = new File(franchiseAuthDBFileName.getAbsolutePath() + ".tmp");

            BufferedReader br = new BufferedReader(new FileReader(franchiseAuthFileName));
            PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

            String line = null;

            // Read from the original file and write to the new
            // unless content matches data to be removed.
            while ((line = br.readLine()) != null) {
            	
            	franchiseAuthDBST = new StringTokenizer(line, "$"); // String Tokenizer("$")
	    		atFranchiseIdStr 		= franchiseAuthDBST.nextToken();
	    		atSaltBase64Str 		= franchiseAuthDBST.nextToken();
	    		atCipheredPswBase64Str 	= franchiseAuthDBST.nextToken();
	    		atAuthFailCountStr 		= franchiseAuthDBST.nextToken();
	    		atLastModifiedDateTimeStr = franchiseAuthDBST.nextToken();

                if (!atFranchiseIdStr.equals(removeFranchiseIdrecordStr)) {

                    pw.println(line);
                    pw.flush();
                } else {
                	authDBDeleteSF = "0x6050";
                }
            }
            pw.close();
            br.close();

            // Delete the original file
            if (!franchiseAuthDBFileName.delete()) {
                System.out.println("Could not delete file");
                authDBDeleteSF = "0x6054";
                return authDBDeleteSF;
            }

            // Rename the new file to the filename the original file had.
            if (!tempFile.renameTo(franchiseAuthDBFileName))
                System.out.println("Could not rename file");
            
            if (!authDBDeleteSF.equals("0x6050")) {
            	System.out.println(removeFranchiseIdrecordStr + " UnRegist ManagerID or FranchiseID not Exist");
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return authDBDeleteSF;
    }

	
}