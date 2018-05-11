Name:Xi Wang
UCID:30057535

•File list:

	-RSATool.java
	 Implemented the RSA-OAEP encryption and decryption.

	-README.txt
	 Description.


•How to compile: 
	Use command line tool to open this folder.
	Type “javac RSATool.java” and enter.

•How to test:
	-java Server portnumber (e.g. java Client 7777) or: java Server portnumber debug
	-java Client hostname portnumber (e.g. java Client localhost 7777) or: java Client hostname portnumber debug
	-In Client window, there will be a request to enter source file name and destination file name
	-There will be a message displayed for success or failure.


•What is implemented:
	-Chinese Remainder decryption have been implemented.

•known bugs:
	-There are no known bugs

•Description of how RSA system parameters are generated:
	-p and q: first generate big prime p' and q' which are 511 bits using BigInteger probable prime constructor; p = 2p'+1 and q = 2q'+1 are safe prime;
	-e: for e = 3 to e = phi(n)-2, check if gcd(e, phi(n))==1, if so, break loop;
	-d: d = e.modInverse(phi);
	 

•Reference: Solution of assignment3 problem6 on D2L.	
