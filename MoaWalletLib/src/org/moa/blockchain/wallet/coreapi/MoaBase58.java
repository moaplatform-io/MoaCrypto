package org.moa.blockchain.wallet.coreapi;

import java.math.BigInteger;

/**
 * [*] ��� ���� : ����Ʈ �迭 �����͸� Base58�� ���ڵ��ϰ� Base58�� ���ڵ��� ���ڿ��� MoaBase58 ���ڵ��� ����Ʈ �迭�� ��ȯ�ϴ�
 *              API�� �����Ѵ�. <br>
 * [*] ���ϸ� : MoaBase58.java <br>
 * [*] ���� �ۼ��� : 2019.01.28 <br>
 * [*] �ֱ� ������ : 2019.02.13 <br>
 * @author Bob <br> 
 **/
public class MoaBase58  
{ 

	private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"; 
	private static final BigInteger BASE = BigInteger.valueOf(58); 

    /**
     * ����Ʈ �迭�� �Է� �޾� Base58�� ���ڵ��Ͽ� ���ڿ��� ��ȯ�Ѵ�.
	 * @param input MoaBase58 ���ڿ��� ��ȯ�� ����Ʈ �迭 �Է�  
	 * @return String MoaBase58 ��ȯ ���ڿ� ��ȯ 
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
     * Base58�� ���ڵ��� ���ڿ��� �޾� MoaBase58 ���ڵ��� ����Ʈ �迭�� ��ȯ�Ѵ�.
	 * @param input Base58�� ���ڵ��� ���ڿ� �Է�  
	 * @return byte[] Base58�� ���ڵ��� ����Ʈ �迭 ��ȯ 
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