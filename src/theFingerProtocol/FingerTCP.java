package theFingerProtocol;

import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * A complete client-side implementation of the finger protocol.
 * 
 * <p>Usage: java theFingerProtocol/FingerTCP &lt;hostname&gt; [&lt;port&gt;] 	
 * [&lt;query&gt;]
 * 
 * @author Joshua Sims
 * @version 03 October 2016
 */
public class FingerTCP 
{	
	// Error codes
	/**
	 * Error code indicating an invalid number of command line arguments were
	 * passed.
	 */
	private static final int INVALID_NUM_OF_ARGS = 1;
	
	/**
	 * Error code indicating an invalid port number was passed at the command
	 * line.
	 */
	private static final int INVALID_PORT_NUMBER = 2;
	
	/**
	 * Error code indicating an IO exception occurred during the 
	 * creation/management of the client to server resources.
	 */
	private static final int IO_EXCEPTION = 3;
	
	/**
	 * Error code indicating that the IP address of the server could not be
	 * determined.
	 */
	private static final int UNKNOWN_HOST_EXCEPTION = 4;
	
	/**
	 * Error code indicating that the security manager has thrown an exception
	 * to indicate that a security violation has arisen from the attempt to 
	 * create the socket.
	 */
	private static final int SECURITY_EXCEPTION = 5;
	
	// Port number information
	/**
	 * The lowest number that a port can assume.
	 */
	private static final int PORT_NUM_LOWER_BOUND = 0;
	
	/**
	 * The highest number that a port can assume.
	 */
	private static final int PORT_NUM_UPPER_BOUND = 65535;
	
	/**
	 * The number assumed by the Finger port.
	 */
	private static final String FINGER_PORT_NUM = "79";

	// Command line argument indices
	/**
	 * The index of the domain name command line argument.
	 */
	private static final int INDEX_OF_HOST_NAME = 0;
	
	/**
	 * The index of the port command line argument.
	 */
	private static final int INDEX_OF_PORT_NUM = 1;
	
	/**
	 * The index of the query command line argument.
	 */
	private static final int INDEX_OF_QUERY = 2;
	
	// Possible number of command line arguments
	/**
	 * Indicates that only hostname was specified.
	 */
	private static final int HOST_ONLY = 1;
	
	/**
	 * Indicates that hostname and port/query were specified.
	 */
	private static final int HOST_AND_PORT_OR_QUERY = 2;
	
	/**
	 * Indicates that hostname, port, and query were specified.
	 */
	private static final int HOST_AND_PORT_AND_QUERY = 3;
	
	// Miscellaneous constants
    /**
     * The length of an empty array.
     */
	private static final int EMPTY = 0;
	
	// Client to server communication resources
	/**
	 * The endpoint between the client and the server
	 */
	private Socket socket;
	
	/**
	 * The stream through which the client sends data to the server.
	 */
	private PrintWriter toHost;
	
	/**
	 * The stream through which the server sends data to the client.
	 */
	private BufferedReader fromHost;
	
	// Methods
	/**
	 * Establishes the connection between the client and the server.
	 * 
	 * @param hostName - the domain name or IP address of the targeted server
	 * @param portNum - the number assumed by the targeted port
	 */
	public FingerTCP(String hostName, String portNum)
	{   
        try 
        {	
        	// Creates the streams and socket necessary to enable the client
        	// to server communication.
        	socket = new Socket(InetAddress.getByName(hostName), 
        		Integer.parseInt(portNum));
            toHost = new PrintWriter(socket.getOutputStream());
            fromHost = new BufferedReader(new InputStreamReader(
            	socket.getInputStream()));
        }
        // The IP address of the server could not be determined.
        catch (UnknownHostException e) 
        {
            System.err.println("The IP address of the specified host, \"" + 
            	hostName + "\", could not be determined.");
            System.exit(UNKNOWN_HOST_EXCEPTION);
        } 
        // An IO exception occurred during the creation of the 
        // client to server communication resources.
        catch (IOException e) 
        {
            System.err.println(e.getMessage());
            System.exit(IO_EXCEPTION);
        } 
        // The security manager indicates that there is a security violation.
        catch (SecurityException e)
        {
        	System.err.println(e.getMessage());
        	System.exit(SECURITY_EXCEPTION);
        }
	}
	
	/**
	 * A complete client-side implementation of the finger protocol.
	 * 
	 * @param args - &lt;hostname&gt; [&lt;port&gt;] [&lt;query&gt;]
	 */
    public static void main(String[] args)
    {	
    	String[] examinedArgs = examineArgs(args);
    	final String hostName = examinedArgs[INDEX_OF_HOST_NAME];
    	final String portNumString = examinedArgs[INDEX_OF_PORT_NUM];
    	final String query = examinedArgs[INDEX_OF_QUERY];
        
    	FingerTCP fingerClient = new FingerTCP(hostName, portNumString);
    	
    	fingerClient.sendQuery(query);
    	
    	fingerClient.receiveAndPrintResponse();

    	fingerClient.closeStreamsAndSocket();
    }
    
    /**
     * Examines the provided command line arguments for validity, assigns 79 
     * (the number assumed by the Finger port) as the port number argument if 
     * no other value was provided, and assigns "" as the query argument if no 
     * other value was provided.
     * 
	 * @param args - &lt;hostname&gt; [&lt;port&gt;] [&lt;query&gt;]
     */
    private static String[] examineArgs(String[] args)
    {	
    	// The default arguments.
    	// (The value of null is assigned as the default host name, 
    	// however, the program will never use it because a server must be 
    	// specified by the user )
    	String[] examinedArgs = 
    		{null, FINGER_PORT_NUM, ""};
    		
    	// Assigns values to the program arguments based on what the user 
    	// specified via the command line.
    	switch (args.length)
    	{
    		// The user has specified a server, port, and query.
    		case HOST_AND_PORT_AND_QUERY:
    			examinedArgs[INDEX_OF_QUERY] = args[INDEX_OF_QUERY];
    			
    		// The user has specified a server and either a query or a port.
    		case HOST_AND_PORT_OR_QUERY:
    			// If the user specified a port number, uses it.
    			if (isValidPortNum(args[INDEX_OF_PORT_NUM].toCharArray()))
    			{
        			examinedArgs[INDEX_OF_PORT_NUM] = args[INDEX_OF_PORT_NUM];
    			}
    			// The user has specified a query only once and has not
    			// specified a port number 
    			else if (examinedArgs[INDEX_OF_QUERY] == "")
    			{
    				examinedArgs[INDEX_OF_QUERY] = args[INDEX_OF_PORT_NUM];
    			}
    			// The user has not specified a valid port and has already
    			// specified a query, so aborts the program.
    			else
    			{
            		System.err.println("The specified port number, \"" +
            				args[INDEX_OF_PORT_NUM] + "\", is invalid." +
                		"\nYou must specify a port number between 0 and " +
                			"65535." +
                		"\nAborting program...");
                	System.exit(INVALID_PORT_NUMBER);
    			}
    			
    		// The user has specified only a server.
    		case HOST_ONLY:
    			examinedArgs[INDEX_OF_HOST_NAME] = args[INDEX_OF_HOST_NAME];
    			break;
    		
    		default:
        		System.err.println("An invalid number of arguments have " +
        				"been passed." + 
            		"\nUsage: java theFingerProtocol/FingerTCP <hostname> " + 
        				"[<port>] [<query>]" +
            		"\nAborting program...");
        		System.exit(INVALID_NUM_OF_ARGS);
    	}
    	
    	return examinedArgs;
    }
    
    /**
     * Determines whether or not the second command line argument is a valid 
     * port number.
     * 
     * @param maybePortNum - the second command line argument specified by
     *     the user
     * 
     * @return true if the second command line argument is a port
     *     number or false if otherwise
     */
    private static boolean isValidPortNum(char[] maybePortNum)
    {
    	// If the second command line argument is an empty string, it cannot
    	// be a port number.
    	if (maybePortNum.length == EMPTY)
    	{
    		return false;
    	}
    	
    	for (char character : maybePortNum)
    	{
    		// If part of the second command line argument is not a digit, 
    		// then the second command line argument cannot be a port number.
    		if (!Character.isDigit(character))
    		{
    			return false;
    		}
    	}
    	
        String portNumString = new String(maybePortNum);
        
        // Determines if specified the port number is valid (between 0 and
        // 65535).
    	int portNum = Integer.parseInt(portNumString);
    	if (portNum < PORT_NUM_LOWER_BOUND 
    		|| portNum > PORT_NUM_UPPER_BOUND)
    	{
    		System.err.println("The specified port number, \"" +
    				portNum + "\", is invalid." +
        		"\nYou must specify a port number between 0 and " +
        			"65535." +
        		"\nAborting program...");
    		System.exit(INVALID_PORT_NUMBER);
    	}
    	
    	// The second command line argument must be a port number.
    	return true;
    }
         
    /**
     * Sends the specified query to the server.
     * 
     * @param query - the query to be sent to the server
     */
    private void sendQuery(String query)
    {
    	// Sends the query followed by the Finger protocol compliant line
    	// endings.
    	toHost.print(query + "\r\n");
    	toHost.flush();
    }
    
    /**
     * Receives the server's response and prints it to the console.
     */
    private void receiveAndPrintResponse()
    {
        String response = null;
        
        try
        {
	        while ((response = fromHost.readLine()) != null)
	        {
	        	System.out.println(response);
	        }
        }
        catch (IOException e)
        {
        	System.err.println(e.getMessage());
        	System.exit(IO_EXCEPTION);
        }
    }
    
    /**
     * Closes the streams and socket which enabled the client to server
     * communication.
     */
    private void closeStreamsAndSocket()
    {
    	try 
    	{
    		toHost.close();
			fromHost.close();
			socket.close();
		}
    	catch (IOException e) 
    	{
    		System.err.println(e.getMessage());
    		System.exit(IO_EXCEPTION);
		}
    }
}