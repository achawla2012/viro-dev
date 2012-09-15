
import java.io.*;
import java.net.*;

	public class SocketMonitor implements Runnable {
				
//*********************************************************************
// SocketMonitor Class Variable Declarations
//*********************************************************************
		private String my_host;
		private int my_port;
		private String veil_ID;					
			
		//*********************************************************************
		// Method: SocketMonitor - Constructor
		//
		// Returns: None
		//
		// Arguments:
		//      - my_host - Hostname/IP of Veil Master Server
		//      - my_port - Port to connect with Veil Master
		//      - veil_ID - First four bytes of this veil_switch's Veil ID
		//
		//*********************************************************************
		public SocketMonitor(String my_host, int my_port, String veil_ID) {
			this.my_host = my_host;
			this.my_port = my_port;
			this.veil_ID = veil_ID;
		}		

		//*********************************************************************
		// Method: run
		//
		// Returns: None
		//
		// Arguments:
		//		- None
		//
		//*********************************************************************
		public void run() {
			
			ServerSocket listenSocket = null;
		
			try {
				listenSocket = new ServerSocket(my_port);	
			} catch (IOException e) {
				System.err.println("Unable to listen on Port " + my_port);
				System.exit(1);
			}
		
			Socket clientSocket = null;
		
			// Loop Indefinitely, while Monitoring Socket When connection occurs, spawn new thread for clientHandler, and hand off Socket
			while (true) {
				try {
					clientSocket = listenSocket.accept();
					Thread thread = new Thread(new clientHandler(clientSocket, my_host, my_port, veil_ID)); 
					thread.start();

				} catch (IOException e) {
    				System.out.println("Accept failed.");
    				System.exit(-1);
				}
			} 
		} // End of run

} // End of SocketMonitor class

// End of File
	  
