/******************************************************************************
 File:           secureFile.java
 Name:           Xi Wang
 UCID:           30057537
 Department:     Computer Science
 Purpose:        Java program for encrypting a file
 ******************************************************************************/
import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.interfaces.*;
import java.security.interfaces.DSAKey;
import java.math.*;
import java.security.SecureRandom;

public class secureFile{
	private static KeyGenerator key_gen = null;
	private static SecretKey sec_key = null;
	private static byte[] raw = null;
	private static SecretKeySpec sec_key_spec = null;
	private static Cipher sec_cipher = null;
    private static String inputfile;
    private static String outputfile;
    private static String userkey;
    
    

	public void encryption() throws Exception{
		FileInputStream in_file = null;
		FileOutputStream out_file = null;
		byte[] sha_hash = null;
		byte[] aes_ciphertext = null;
		int read_bytes = 0;
        byte[] seed = null;
        SecureRandom sr = null;

		try{
            seed = userkey.getBytes();
           
            //open files
			in_file = new FileInputStream(inputfile);
            out_file = new FileOutputStream(outputfile);
            
			//read file into a byte array
			byte[] msg = new byte[in_file.available()];
			read_bytes = in_file.read(msg);

			//SHA-1 Hash
			sha_hash = sha1_hash(msg);
            
            byte[] finmsg = new byte[msg.length + sha_hash.length];
            System.arraycopy(msg,0,finmsg,0,msg.length);
            System.arraycopy(sha_hash,0,finmsg,msg.length,sha_hash.length);
            
			//encrypt file with AES
			//key setup - use seed to generate 128 bit key
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
            

			//do AES encryption
			aes_ciphertext = aes_encrypt(finmsg);
            System.out.println("File has been encrypted!");
			out_file.write(aes_ciphertext);
			out_file.close();
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
	}
    
    public secureFile(String key, String inname, String outname){
        inputfile = inname;
        outputfile = outname;
        userkey = key;
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

	public static byte[] hmac_sha1(byte[] in_data) throws Exception{
		byte[] result = null;

		try{
			//generate the HMAC key		
			KeyGenerator theKey = KeyGenerator.getInstance("HMACSHA1");
			SecretKey secretKey = theKey.generateKey();

			Mac theMac = Mac.getInstance("HMACSHA1");
			theMac.init(secretKey);

			//create the hash
			result = theMac.doFinal(in_data);
		}
		catch(Exception e){
			System.out.println(e);
		}
		return result;
	}

	public static byte[] aes_encrypt(byte[] data_in) throws Exception{
		byte[] out_bytes = null;
		try{
			//set cipher object to encrypt mode
			sec_cipher.init(Cipher.ENCRYPT_MODE, sec_key_spec);

			//create ciphertext
			out_bytes = sec_cipher.doFinal(data_in);
		}
		catch(Exception e){
			System.out.println(e);
		}
		return out_bytes;
	}

	public static String aes_decrypt(byte[] data_in) throws Exception{
		byte[] decrypted = null;
		String dec_str = null;
		try{
			//set cipher to decrypt mode
			sec_cipher.init(Cipher.DECRYPT_MODE, sec_key_spec);

			//do decryption
			decrypted = sec_cipher.doFinal(data_in);

			//convert to string
			dec_str = new String(decrypted);
		}
		catch(Exception e){
			System.out.println(e);
		}
		return dec_str;
	}

}
