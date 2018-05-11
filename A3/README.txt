Name:Xi Wang
UCID:30057535

•File list:

	-Client.java
	 Client sends an authenticated and encrypted file to a server. Encrypt both the messages and the means for authentication.

	-Server.java
	 Server decrypt the file, check the integrity of the file content, and write the file content to the destination file specified by the client.

	-ServerThread.java
	 Containing a class for the thread to deal with clients who connect to the Server

	-CryptoUtilities.java
	This class contains various utility functions for working with AES encryption and decryption, and for working with HMAC-SHA1 message digests.

	-README.txt
	 Description.


•How to compile: 
	Use command line tool to open this folder.
	Type “javac CryptoUtilities.java” and enter.
	Type “javac Client.java” and enter.
	Type “javac Server.java” and enter.
	Type “javac ServerThread.java” and enter.

•How to test:
	-java Server portnumber (e.g. java Client 7777) or: java Server portnumber debug
	-java Client hostname portnumber (e.g. java Client localhost 7777) or: java Client hostname portnumber debug
	-In Client window, there will be a request to enter source file name and destination file name
	-There will be a message displayed for success or failure.
	-Length of bits and certainty can be changed in the beginning of “CryptoUtilities.java” file.


•What is implemented:
	-The problem is solved in full.

•known bugs:
	-There are no known bugs

•Description of my key exchange protocol:
	-Description of all protocol messages:
	 I used DataInputStream and DataOutputStream for message sending and receiving.
	 1.Server sends p and g(in byte array) along with their lengths(sending length is for the convenience of receiving byte array); 
	 2.client receives p and g, then transfer them into BigIntegers; 
	 3.server sends yb = g^b (mod p); 
	 4.client receive yb; 
	 5.client sends ya = g^a (mod p); 
	 6.server receives ya
	 The left part of message exchange is the same as assignment2.
	 
	-How I implemented the DH protocol:
	 1.Server generates a random prime BigInteger q, check if p=2*q+1 is prime. If not, generate q again.
	 2.For g=1 to g=p-2, check if g^(p-1) (mod p) congruent to 1. If not, then g is a primitive root.
	 3.Server sends calculated p and g to client.
	 4.Server generates a random number a, and client generates a random number b. (Here, I used BigInteger for both a and b and assigned them the length of 511(DH_BIT_LEN - 1).)
	 5.Server calculates yb = g^b (mod p) and sends it to client. Client calculates ya = g^a (mod p) and sends it to server.
	 6.Server calculates k=ya^b, and client calculates k=yb^a.
	 7.Server and client use k to generate the same 16-bit hash key.

•Reference: Solution of assignment2, problem6 on D2L.	
