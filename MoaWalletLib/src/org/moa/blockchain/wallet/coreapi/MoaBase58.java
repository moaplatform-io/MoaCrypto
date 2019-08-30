package org.moa.blockchain.wallet.coreapi;

import java.math.BigInteger;

/**
 * [*] 기능 정의 : 바이트 배열 데이터를 Base58로 엔코딩하고 Base58로 엔코딩된 문자열을 MoaBase58 디코딩된 바이트 배열로 변환하는
 *              API를 제공한다. <br>
 * [*] 파일명 : MoaBase58.java <br>
 * [*] 최초 작성일 : 2019.01.28 <br>
 * [*] 최근 수정일 : 2019.02.13 <br>
 * @author Bob <br> 
 **/
public class MoaBase58  
{ 

	private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"; 
	private static final BigInteger BASE = BigInteger.valueOf(58); 

    /**
     * 바이트 배열을 입력 받아 Base58로 엔코딩하여 문자열로 반환한다.
	 * @param input MoaBase58 문자열로 변환할 바이트 배열 입력  
	 * @return String MoaBase58 변환 문자열 반환 
     */
    public static String encode(byte[] input)  
    { 
        // This could be a lot more efficient. 
        BigInteger bi = new BigInteger(1, input); 
        StringBuffer s = new StringBuffer(); 

        while (bi.compareTo(BASE) >= 0)  
        { 
            BigInteger mod = bi.mod(BASE); 
            s.insert(0, ALPHABET.charAt(mod.intValue())); 
            bi = bi.subtract(mod).divide(BASE); 
        } 

        s.insert(0, ALPHABET.charAt(bi.intValue())); 
        
        // Convert leading zeros too. 
        for (byte anInput : input)  
        { 
            if (anInput == 0) 
                s.insert(0, ALPHABET.charAt(0)); 
            else 
                break; 
        } 
        
        return s.toString(); 
    } 

    
    /**
     * Base58로 엔코딩된 문자열을 받아 MoaBase58 디코딩한 바이트 배열을 반환한다.
	 * @param input Base58로 엔코딩된 문자열 입력  
	 * @return byte[] Base58로 디코딩된 바이트 배열 반환 
     */
    public static byte[] decode(String input) 
    { 
        byte[] bytes = decodeToBigInteger(input).toByteArray(); 

        boolean stripSignByte = bytes.length > 1 && bytes[0] == 0 && bytes[1] < 0; 
        
        // Count the leading zeros, if any. 
        int leadingZeros = 0; 
        for (int i = 0; input.charAt(i) == ALPHABET.charAt(0); i++)  
        { 
            leadingZeros++; 
        } 
        // Now cut/pad correctly. Java 6 has a convenience for this, but Android can't use it. 
        byte[] tmp = new byte[bytes.length - (stripSignByte ? 1 : 0) + leadingZeros]; 
        System.arraycopy(bytes, stripSignByte ? 1 : 0, tmp, leadingZeros, tmp.length - leadingZeros); 
        
        return tmp; 
    } 
 
    protected static BigInteger decodeToBigInteger(String input) 
    { 
    	BigInteger bi = BigInteger.valueOf(0); 
         
        // Work backwards through the string. 
        for (int i = input.length() - 1; i >= 0; i--) 
        { 
            int alphaIndex = ALPHABET.indexOf(input.charAt(i)); 
            if (alphaIndex == -1) 
            { 
            	throw new IllegalArgumentException("In MoaBase58.decodeToBigInteger(), Illegal character " + input.charAt(i) + " at index " + i + ". Throwing new IlleglArgumentException."); 
            } 
            bi = bi.add(BigInteger.valueOf(alphaIndex).multiply(BASE.pow(input.length() - 1 - i))); 
        } 
         
        return bi; 
    } 

}