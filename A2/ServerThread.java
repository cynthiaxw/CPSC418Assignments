/******************************************************************************
File:           ServerThread.java
Name:           Xi Wang
UCID:           30057537
Department:     Computer Science
******************************************************************************/

import java.net.*;
import java.util.Scanner;
import java.io.*;

/**
 * Thread to deal with clients who connect to Server.  Put what you want the
 * thread to do in it's run() method.
 */

public class ServerThread extends Thread
{
    private Socket sock;  //The socket it communicates with the client on.
    private Server parent;  //Reference to Server object for message passing.
    private int idnum;  //The client's id number.
    private boolean debugFlg = false; //debug flag
	
    /**
     * Constructor, does the usual stuff.
     * @param s Communication Socket.
     * @param p Reference to parent thread.
     * @param id ID Number.
     */
    public ServerThread (Socket s, Server p, int id, boolean debugflg)
    {
	parent = p;
	sock = s;
	idnum = id;
        debugFlg = debugflg;
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
     * This is what the thread does as it executes.  Listens on the socket
     * for incoming data and then echos it to the screen.  A client can also
     * ask to be disconnected with "exit" or to shutdown the server with "die".
     */
    public void run ()
    {
	String incoming = null;
	DataOutputStream out;
	DataInputStream in;
		
	try {
	    in = new DataInputStream (sock.getInputStream());
	}
	catch (UnknownHostException e) {
	    System.out.println ("Unknown host error.");
	    return;
	}
	catch (IOException e) {
	    System.out.println ("Could not establish communication.");
	    return;
	}
	try {
	    out = new DataOutputStream (sock.getOutputStream());
	}
	catch (UnknownHostException e) {
	    System.out.println ("Unknown host error.");
	    return;
	}
	catch (IOException e) {
	    System.out.println ("Could not establish communication.");
	    return;
	}

	

	String key = null;
	String destinationFileName = null;
	int read_bytes;
	//get the destination file name
	try {
		destinationFileName = in.readUTF();
	}
	catch (IOException e) {
	    System.out.println ("Could not read from input.");
	    return;
	}
	//get length of the source file in bytes
	try {
		read_bytes = in.readInt();	
	}
	catch (IOException e) {
	    System.out.println ("Could not read from input.");
	    return;
	}
	
	byte[] msg = new byte[read_bytes];
	
	//get the source file contents (encrypted and integrity-protected)
	try {
		in.read(msg);
	}
	catch (IOException e) {
	    System.out.println ("Could not read from input.");
	    return;
	}
	
	if(debugFlg) {
    	System.out.println ("Debug Server: Getting key(seed) from user");
    }
	System.out.println("enter shared key:");
	Scanner scanner = new Scanner(System.in);
	key = scanner.nextLine();
	//decrypt file
	if(debugFlg) {
    	System.out.println ("Debug Server: Starting file transfer");
    	System.out.println ("Debug Server: Receiving output file name");
    	System.out.println ("Debug Server: Got output file name = "+destinationFileName);
    	System.out.println ("Debug Server: Got receiving file zie = "+read_bytes);
    	System.out.println ("Debug Server: Receiving and decrypting file with MAC appended");
    	System.out.println ("Debug Server: Checking MAC");
    }
    decryptFile decrypt = new decryptFile(key,destinationFileName,msg,read_bytes);
    try {
		if(decrypt.decryption() == 1) {
			out.writeUTF("success");
		}
		else{
			out.writeUTF("failure");
		}
	} catch (Exception e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
    if(debugFlg) {
    	System.out.println("Debug Server: Sending \"passed\" acknowledgement");
    }
    
    
    }
}
