import java.net.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.util.Random;

import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * This class represents a thread to deal with clients who connect to Server.  
 * Put what you want the thread to do in it's run() method.
 *
 * @author Mike Jacobson
 * @version 1.0, October 23, 2013
 */
public class ServerThread extends Thread
{
    private Socket sock;  //The socket it communicates with the client on.
    private Server parent;  //Reference to Server object for message passing.
    private int idnum;  //The client's id number.
    private DataOutputStream out;
    private DataInputStream in;
    private SecretKeySpec key;   // AES encryption key


    /**
     * Utility for printing protocol messages
     * @param s protocol message to be printed
     */
    private void debug(String s) {
	if(parent.getDebug()) 
	    System.out.println("Debug Server: " + s);
    }



    /**
     * Constructor, does the usual stuff.
     * @param s Communication Socket.
     * @param p Reference to parent thread.
     * @param id ID Number.
     */
    public ServerThread (Socket s, Server p, int id)
    {
	parent = p;
	sock = s;
	idnum = id;
	in = null;
	out = null;
    }


	
    /**
     * Getter for id number.
     * @return ID Number
     */
    public int getID ()
    {
	return idnum;
    }


	
    /**
     * Getter for the socket, this way the parent thread can
     * access the socket and close it, causing the thread to
     * stop blocking on IO operations and see that the server's
     * shutdown flag is true and terminate.
     * @return The Socket.
     */
    public Socket getSocket ()
    {
	return sock;
    }



    /**
     * Prompts user for a sting to be used as seed for deriving the AES key
     * @throws IOException 
     */
    public void getKey() throws IOException {
    	debug("Generating key...");
    	BigInteger p = BigInteger.ONE;
    	BigInteger q = BigInteger.ZERO;
    	BigInteger g = BigInteger.ONE;
    	BigInteger b = new BigInteger(CryptoUtilities.DH_BIT_LEN-1, new Random());
    	
    do {
    		q = new BigInteger(CryptoUtilities.DH_BIT_LEN, CryptoUtilities.DH_PRIME_CERTAINTY, new Random());
    		p = p.add(q.multiply(new BigInteger("2")));
    }while(p.isProbablePrime(CryptoUtilities.DH_PRIME_CERTAINTY) == false);
    debug("Safe prime p = "+ p);
    
    for(;g.compareTo(p.add(BigInteger.ONE.negate())) == -1;g = g.add(BigInteger.ONE)) {
    		if(g.modPow(q, p).compareTo(BigInteger.ONE) != 0) {
    			break;
    		}
    }
    debug("Primitive root g = " + g);
    
    debug("Secret b = " + b);
    
    //Send p,g to Client
    byte[] p_byte = p.toByteArray();
	byte[] g_byte = g.toByteArray();
    out.writeInt(p_byte.length);
    out.write(p_byte);
    out.writeInt(g_byte.length);
    out.write(g_byte);
    
    
    
    byte[] ya_byte = null;
    byte[] yb_byte = null;
    
    BigInteger yb = g.modPow(b, p);
    //Send yb to Client
    yb_byte = yb.toByteArray();
    out.writeInt(yb_byte.length);
    	out.write(yb_byte);

    debug("Receiving g^a (mod p)...");
    int length = in.readInt();
    ya_byte = new byte[length];
    in.read(ya_byte);
    BigInteger ya = new BigInteger(ya_byte);
    debug("Received g^a (mod p) = "+ ya);
    
    BigInteger k = ya.modPow(b, p);
    debug("Calculate g^ab (mod p) = " + k);
    

	// compute key:  1st 16 bytes of SHA-1 hash of seed
	key = CryptoUtilities.key_from_seed(k.toByteArray());
 	debug("Using key = " + CryptoUtilities.toHexString(key.getEncoded()));
   }



    /**
     * Encrypted file transfer
     * @return true if file transfer was successful
     */
    public boolean receiveFile() {
	debug("Starting File Transfer");

	// get the output file name
	String outfilename;
	try {
	    debug("Receiving output file name");
	    outfilename = new String(CryptoUtilities.receiveAndDecrypt(key,in));
	    debug("Got file name = " + outfilename);
	}
	catch (IOException e) {
	    System.out.println("Error receiving the output file name");
	    close();
	    return false;
	}

	System.out.println("Output file: " + outfilename);



	// get the file size
	int size;
	try {
	    debug("Receiving file size");
	    size = Integer.parseInt(new String(CryptoUtilities.receiveAndDecrypt(key,in)));	
	    debug("Got file size = " + size);
	}
	catch (IOException e) {
	    System.out.println("Error sending the file length");
	    close();
	    return false;

	}

	System.out.println("File size = " + size);



	// get the encrypted, integrity-protected file
	byte[] hashed_plaintext;
	try {
	    debug("Receiving and decrypting file with MAC appended");
	    hashed_plaintext = CryptoUtilities.receiveAndDecrypt(key,in);
	}
	catch (IOException e) {
	    System.out.println("Error receiving encrypted file");
	    close();
	    return false;
	}


	// check validity of MAC.  Write to the file if valid.
	debug("Checking MAC");
	boolean fileOK = false;
	if (CryptoUtilities.verify_hash(hashed_plaintext,key)) {
	    debug("Message digest OK.  Writing file.");
	    System.out.println("Message digest OK. Writing file");

	    // extract plaintext and output to file
	    byte[] plaintext = CryptoUtilities.extract_message(hashed_plaintext);

	    // writing file
	    FileOutputStream outfile = null;
	    try {
		outfile = new FileOutputStream(outfilename);
		outfile.write(plaintext,0,plaintext.length);
		outfile.close();
	    }
	    catch (IOException e) {
		System.out.println("Error writing decrypted file.");
		close();
		return false;
	    }
	    finally {
		try {
		    outfile.close();
		}
		catch (IOException e) {
		    System.out.println("Error closing output file.");
		    return false;
		}
	    }


	    fileOK = true;

	    // send acknowledgement to client
	    try {
		debug("Sending \"passed\" acknowledgement.");
		CryptoUtilities.encryptAndSend("Passed".getBytes(),key,out);
	    }
	    catch (IOException e) {
		System.out.println("Error sending passed acknowledgement.");
		close();
		return true;
	    }

	    System.out.println("File written successfully.");
	}
	else {
	    System.out.println("Integrity check failed.  File not written.");

	    try {
		debug("Sending \"Failed\" acknowledgement.");
		CryptoUtilities.encryptAndSend("Failed".getBytes(),key,out);
	    }
	    catch (IOException e) {
		System.out.println("Error sending failed acknowledgement.");
		close();
		return false;
	    }
	}

	close();
	return fileOK;
    }



    /**
     * Shuts down the socket connection
     */
    public void close() {
	// shutdown socket and input reader
	try {
	    sock.close();
	    if (in != null)
		in.close();
	    if (out != null)
		out.close();
	} 
	catch (IOException e) {
	    return;
	}	
		
    }



	
    /**
     * This is what the thread does as it executes.  Gets the encryption key,
     * receives a file from the client, and shuts down.
     */
    public void run ()
    {
	// open input and output streams for file transfer
	try {
	    in = new DataInputStream(sock.getInputStream());
	    out = new DataOutputStream(sock.getOutputStream());
	}
	catch (UnknownHostException e) {
	    System.out.println ("Unknown host error.");
	    close();
	    return;
	}
	catch (IOException e) {
	    System.out.println ("Could not create input and output streams.");
	    close();
	    return;
	}

	// get the encryption key
	try {
		getKey();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	// do file transfer
	receiveFile();

	// shut down the client and kill the server
	close();
	parent.killall();
    }
}
