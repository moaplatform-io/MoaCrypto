package org.moa.blockchain.wallet.test;

import java.nio.charset.Charset;
import java.security.NoSuchProviderException;
import java.security.Security;
//import java.util.StringTokenizer;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.moa.blockchain.wallet.coreapi.MoaBase58;
import org.moa.crypto.coreapi.DigestCoreAPI;



@SuppressWarnings("unused")
public class Base58Test {
	
	static{

		java.security.Provider p = new org.bouncycastle.jce.provider.BouncyCastleProvider();
		Security.addProvider(p);
	}
	
	static DigestCoreAPI dg = new DigestCoreAPI();
	static MoaBase58 base58 = new MoaBase58();
	static Charset charset = Charset.forName("UTF-8");
	
	public static void main(String args[]) 
			   throws NoSuchProviderException, NullPointerException, Exception
	{
		
		
		byte[] BCWPukBytes = MoaBase58.decode("CpmUMjjAixnWzcUiRcNe3EMiR9DDKpQmjPoTyBSGQYkfeyd9fSyMnhETLJ9e8pBZYT1bpqDV1fejXcepCxjuXLkXL");
		System.out.println("[0x]BCWPukBytes(16진수): " + Hex.toHexString(BCWPukBytes) + " [길이] : " + BCWPukBytes.length);
		
		
		
		
		String test = "abc";
		byte[] testBytes =test.getBytes("UTF-8");
		
		@SuppressWarnings("static-access")
		byte[] sha256TestBytes = dg.HashDigest("SHA256", testBytes, "BC"); 
		System.out.println("[0x]SHA256TestBytes(16진수) : " + Hex.toHexString(sha256TestBytes));
		
		String sha256Base58Str = MoaBase58.encode(sha256TestBytes);
		System.out.println("[0x]sha256Base58Str(Str) : " + sha256Base58Str);
		
		byte[] decSha256TestBytes = MoaBase58.decode(sha256Base58Str); 
		System.out.println("[0x]Base58 Decode Sha256TestBytes(16진수) : " + Hex.toHexString(decSha256TestBytes));
		
		
		String aaOrg = "2CJs4x1iycKy7D3ckZo1Yt8xm4EY";
		byte[] aa = MoaBase58.decode("2CJs4x1iycKy7D3ckZo1Yt8xm4EY");
		String aaHexStr = Hex.toHexString(MoaBase58.decode("2CJs4x1iycKy7D3ckZo1Yt8xm4EY"));
		System.out.println("[0x]aaOrg(B58Str): " + aaOrg + " / Len : " + aaOrg.length());
		System.out.println("[0x]aa(Hex)      : " + Hex.toHexString(aa) + " / Len : " + aa.length);
		System.out.println("[0x]aa(HexStr)   : " + aaHexStr + " / Len : "  + aaHexStr.length());
		
	}
	

}	


	
	