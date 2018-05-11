/******************************************************************************
File:           Client.java
Name:           Xi Wang
UCID:           30057537
Department:     Computer Science
******************************************************************************/

import java.io.*;
import java.net.*;

/**
 * Client program.  Connects to the server and sends text accross.
 */

public class Client 
{
    private Socket sock;  //Socket to communicate with.
    private String key;   //shared key.
    private String sourceFileName;
    private String destinationFileName;
    static private boolean debugFlg = false;
    
    /**
     * Main method, starts the client.
     * @param args args[0] needs to be a hostname, args[1] a port number.
     * @throws Exception 
     */
    public static void main (String [] args) throws Exception
    {
    	if (args.length !=2 && args.length != 3) {
    		System.out.println ("Usage: java Client hostname port#");
    	    System.out.println ("hostname is a string identifying your server");
    	    System.out.println ("port is a positive integer identifying the port to connect to the server");
    	    return;
    	}
        if(args.length == 3){
            if(args[2].compareTo("debug") == 0){
                debugFlg = true;
                System.out.println("Debug mode...");
            }
        }


	try {
	    Client c = new Client (args[0], Integer.parseInt(args[1]));
	}
	catch (NumberFormatException e) {
	    System.out.println ("Usage: java Client hostname port#");
	    System.out.println ("Second argument was not a port number");
	    return;
	}
    }
	
    /**
     * Constructor, in this case does everything.
     * @param ipaddress The hostname to connect to.
     * @param port The port to connect to.
     * @throws Exception 
     */
    public Client (String ipaddress, int port) throws Exception
    {
	/* Allows us to get input from the keyboard. */
	BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
	String userinput;
	DataOutputStream out;
	DataInputStream in;
		
	/* Try to connect to the specified host on the specified port. */
	try {
	    sock = new Socket (InetAddress.getByName(ipaddress), port);
	}
	catch (UnknownHostException e) {
	    System.out.println ("Usage: java Client hostname port#");
	    System.out.println ("First argument is not a valid hostname");
	    return;
	}
	catch (IOException e) {
	    System.out.println ("Could not connect to " + ipaddress + ".");
	    return;
	}
		
	/* Status info */
	System.out.println ("Connected to " + sock.getInetAddress().getHostAddress() + " on port " + port);
		
	try {
	    out = new DataOutputStream(sock.getOutputStream());
	}
	catch (IOException e) {
	    System.out.println ("Could not create output stream.");
	    return;
	}
	
	try {
	    in = new DataInputStream(sock.getInputStream());
	}
	catch (IOException e) {
	    System.out.println ("Could not create input stream.");
	    return;
	}
	
        
        if(debugFlg) {
        	System.out.println ("Debug Client: Getting key(seed) from user");
        }
        System.out.println("Please enter key:");
        try {
            while ((userinput = stdIn.readLine()) != null) {
                /* Echo it to the screen. */
                key = userinput.toString();
                break;
            }
        } catch (IOException e) {
            System.out.println ("Could not read from input.");
            return;
        }
        
        if(debugFlg) {
        	System.out.println ("Debug Client: Starting file transfer");
        }
        System.out.println("Please enter source file name:");
        try {
            while ((userinput = stdIn.readLine()) != null) {
                /* Echo it to the screen. */
                sourceFileName = userinput.toString();
                break;
            }
        } catch (IOException e) {
            System.out.println ("Could not read from input.");
            return;
        }
        System.out.println("Please enter destination file name:");
        try {
            while ((userinput = stdIn.readLine()) != null) {
                /* Echo it to the screen. */
                destinationFileName = userinput.toString();
                break;
            }
        } catch (IOException e) {
            System.out.println ("Could not read from input.");
            return;
        }
             
        //encrypt file
        secureFile encrypt = new secureFile(key,sourceFileName,destinationFileName);
        encrypt.encryption();
        
      //open files
        FileInputStream in_file = new FileInputStream(destinationFileName);
		byte[] msg = new byte[in_file.available()];
		int read_bytes = in_file.read(msg);
        //transfer the destination file name
        out.writeUTF(destinationFileName);
        //transfer length of the source file in bytes
        out.writeInt(read_bytes);
        //transfer the source file contents (encrypted and integrity-protected)
        out.write(msg);
        if(debugFlg) {
        	System.out.println ("Debug Client: Sending output file name = "+destinationFileName);
        	System.out.println ("Debug Client: Sending file zie = "+read_bytes);
        	System.out.println ("Debug Client: Encrypting and sending file with MAC appended");
        	System.out.println ("Debug Client: Waiting for server acknowledgement");
        }
        
        //wait for server's acknowledgement
	try {
		if (in.readUTF().compareTo("success") == 0) {
		    System.out.println ("File received and verified!");
		    System.out.println ("Client exiting.");
		    stdIn.close ();
		    out.close ();
		    sock.close();
		    return;
		}
		else if(in.readUTF().compareTo("failure") == 0) {
			System.out.println ("Failure!");
			System.out.println ("Client exiting.");
		    stdIn.close ();
		    out.close ();
		    sock.close();
		    return;
		}
	} catch (IOException e) {
	    System.out.println ("Could not read from input.");
	    return;
	}		
    }
    
}
