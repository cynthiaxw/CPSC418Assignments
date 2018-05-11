/******************************************************************************
File:           decryptFile.java
Name:           Xi Wang
UCID:           30057537
Department:     Computer Science
Purpose:        Java program for decrypting a file
******************************************************************************/
import java.util.Arrays;
import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.interfaces.*;
import java.security.interfaces.DSAKey;
import java.math.*;
import java.security.SecureRandom;

public class decryptFile{
	private static KeyGenerator key_gen = null;
	private static SecretKey sec_key = null;
	private static byte[] raw = null;
	private static SecretKeySpec sec_key_spec = null;
	private static Cipher sec_cipher = null;
	
    private static String outputfile;
    private static String userkey;
    private static byte[] ciphtext;

	public int decryption() throws Exception{
		FileInputStream in_file = null;
		FileOutputStream out_file = null;
		byte[] sha_hash = null;
		byte[] aes_ciphertext = null;
		byte[] decrypted = null;
		boolean verify = false;
        byte[] seed = null;
        SecureRandom sr = null;
        int successflg = 0;

		try{
            seed = userkey.getBytes();
            /*key compute - use seed to compute 128 bit key*/
            key_gen = KeyGenerator.getInstance("AES");
            sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(seed);
            key_gen.init(128,sr);
            sec_key = key_gen.generateKey();
            
            //get key material in raw form
            raw = sec_key.getEncoded();
            sec_key_spec = new SecretKeySpec(raw, "AES");
            
            //create the cipher object that uses AES as the algorithm
            sec_cipher = Cipher.getInstance("AES");
            
            System.out.println("key:"+sec_key_spec);
            /*key compute - use seed to compute 128 bit key*/
            
			//open files
			//in_file = new FileInputStream(inputfile);
			out_file = new FileOutputStream(outputfile);

			//read file into a byte array
			//byte[] ciphtext = new byte[in_file.available()];
			//in_file.read(ciphtext);

			//decrypt file
			decrypted = aes_decrypt(ciphtext);

            //write plaintext to file
            byte[] plaintext = Arrays.copyOfRange(decrypted,0,decrypted.length-20);
            out_file.write(plaintext);
            out_file.close();

            //Seperate the digest from plaintext
            byte[] digest = Arrays.copyOfRange(decrypted,decrypted.length-20,decrypted.length);
            //SHA-1 Hash
            sha_hash = sha1_hash(plaintext);
            //verify if the message has been modified
            if(Arrays.equals(sha_hash,digest)){
                System.out.println("Congratulations!!! File has not been modified!");
                successflg = 1;
            }
            else{
                System.out.println("Oops!!! File has been modified!");
            }
		}
		catch(Exception e){
			System.out.println(e);
		}
		finally{
			if (in_file != null){
				in_file.close();
			}
			if(out_file != null){
				out_file.close();
			}
		}
		if(successflg == 1) {
			return 1;
		}
		else return 0;
	}

	public decryptFile(String key, String inname, byte[] msg, int read_bytes){
        outputfile = inname;
        userkey = key;
        ciphtext = new byte[read_bytes];
        ciphtext = msg;
        
    }
	public static byte[] sha1_hash(byte[] input_data) throws Exception{
		byte[] hashval = null;
		try{
			//create message digest object
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			
			//make message digest
			hashval = sha1.digest(input_data);
		}
		catch(NoSuchAlgorithmException nsae){
			System.out.println(nsae);
		}
		return hashval;
	}


	public static byte[] aes_decrypt(byte[] data_in) throws Exception{
		byte[] decrypted = null;
		String dec_str = null;
		try{
			//set cipher to decrypt mode
			sec_cipher.init(Cipher.DECRYPT_MODE, sec_key_spec);

			//do decryption
			decrypted = sec_cipher.doFinal(data_in);

		}
		catch(Exception e){
			System.out.println(e);
		}
		return decrypted;
	}

}
