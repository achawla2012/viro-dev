
//***********************************************
// INCLUDES
//***********************************************
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.Timer;
import java.util.TimerTask;

public class veil_switch {

	// ***********************************************
	// CONSTANTS
	// ***********************************************

	public static boolean multipleRoute = true;

	public static boolean printDebug = true;


	public static long numDataPckSent = 0;
	public static long numDataPckRecv = 0;
	public static long numDataPckTTL = 0;
	public static long numDataPckDst = 0;
	public static long numDataPckHop = 0;

	public static long numCtrlPckSent = 0;
	public static long numCtrlPckRecv = 0;
	public static long numCtrlPckTTL = 0;
	public static long numCtrlPckDst = 0;
	public static long numCtrlPckHop = 0;
	public static long numControlPckDrop = 0; // to record the # of control pck

	public static long startCtructRT;
	public static long endCtructRT;

	public static final String RDV_PUBLISH = "1000";
	public static final String RDV_QUERY = "2000";
	public static final String RDV_REPLY = "3000";
	public static final String RDV_UPDATE = "4000";
	public static final int K = 32; // Max Number of Bits Allowed for Veil IDs.
	public static final int WAIT_TIME = 5000; // Time to wait between rounds
	public static final int MAX_VID_LEN = 8; // MAX number of Bits in VID
	public static final int RTEntryTimeout = 90000; // Time before routing table entry expire;
	public static final int RdvEntryTimeout = 60000; // Time before rdv table entry expire;
	public static final int RTRefreshTime = 60000;   // repeat every 60 seconds
	public static final int RTinitialDelay = 0;

	// ***********************************************
	// CLASS VARIABLES
	// ***********************************************
	public static String failSendPacketVid = new String();

	private static String veil_ID = null;
	public static String[][] adj_hosts = new String[20][2];
	public static String[][] vid_hosts = new String[90][2];
	private static String phys_ID;
	public static ArrayList<String[]> routingTable = new ArrayList<String[]>();
	public static RTable rTable = new RTable();
	public static int topology_size = 0;
	public static int adjacent_count = 0;
	private static int vid_size = 0;
	private static String my_host = "";
	public static int my_port = 0;

	public static void main(String[] args) {
		long startCreatRT = System.currentTimeMillis();
		String delim;
		String tokens[];

		// Check for the Proper Number of Command Line Arguments
		if (args.length != 3) {
			System.out.println("");
			System.out.println("veil_switch :: Invalid number of arguements.");
			System.err.println("Format is: veil_switch Graph_Adjacency_File VID_Map_File switch_pid");
			System.out.println("");
			System.exit(1);
		}

		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("\u001b[0;33m********************************************************************************\u001b[m");
		System.out.println("\u001b[0;33m****    Veil Switch v1.0 \u001b[m");
		System.out.println("\u001b[0;33m****    Ji Xu \u001b[m");
		System.out.println("\u001b[0;33m********************************************************************************\u001b[m");
		System.out.println("");



		// ***********************************************
		// INITIALIZE SWITCH WITH COMMAND LINE INPUT
		// ***********************************************

		// Physical ID
		phys_ID = args[2];
		delim = ":";
		tokens = phys_ID.split(delim);
		my_host = tokens[0];
		my_port = Integer.parseInt(tokens[1]);
		System.out.print("Switch active at IP " + my_host + ", Port " + my_port);

		// Graph Adjacency List
		try {
			FileInputStream fstream = new FileInputStream(args[0]);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			// Read Graph Adjacency List Line by Line Until This Switch's pid is
			// found and store this Switch's physical neighbors
			while ((strLine = br.readLine()) != null) {
				delim = "[ ]";
				tokens = strLine.split(delim);
				if (tokens[0].equals(phys_ID)) {
					for (int i = 0; i < (tokens.length - 1); i++)
						adj_hosts[i][0] = tokens[i + 1];
					adjacent_count = tokens.length - 1;
				}
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

		// Veil ID List
		try {
			FileInputStream fstream = new FileInputStream(args[1]);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			// Read Veil ID List File Line by Line and Store in Array
			int i = 0;
			while ((strLine = br.readLine()) != null) {
				delim = "[ ]";
				tokens = strLine.split(delim);
				vid_hosts[i][0] = tokens[0];
				// Pad VID to make K bits - 32 In this Case
				vid_size = tokens[1].length();
				vid_hosts[i][1] = leftPadString(tokens[1], K, '0');
				if (tokens[0].equals(phys_ID)) {
					veil_ID = leftPadString(tokens[1], K, '0');
				}
				i++;
			}
			topology_size = i;
			in.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

		// Check to Make Sure This Switch's Physical ID was found in the Veil ID
		// List File
		if (veil_ID.equals(null)) {
			System.err.println("Unable to locate this hosts Veil ID from input lists.  Aborting...");
			System.exit(1);
		}
		System.out.println(", with Veil ID of " + veil_ID);

		// **************************************************
		// Spawn New Thread to Monitor Incoming Connections
		// **************************************************
		Thread thread = new Thread(new SocketMonitor(my_host, my_port, veil_ID));
		thread.start();



		// **********************************************************************
		// Print the Current Routing Table
		// **********************************************************************
		System.out.println("");
		System.out.println("Building Routing Table from Neighbors...");

		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			public void run() {
				// Build current routing table from neighbor node
				RTbuilder();

				printRT();

				// Contruct routing table by sending rdv packet
				RTConstrut();

				checkEntryExpire();

				printRT();

				clientHandler.updateRdvTable();

				clientHandler.printRdvTable();
			}
		};

		timer.scheduleAtFixedRate(task, RTinitialDelay, RTRefreshTime);

		// **************************************************
		// Spawn New Thread to Monitor Incoming Connections
		// **************************************************
		//Thread thread1 = new Thread(new RTupdater());
		//thread1.start();
		// print the # of control pck
		// System.out.println("num of control Packet have send is  " +
		// numControlPck);

	} // End of main

	// *********************************************************************
	// VEIL SWITCH METHODS
	// *********************************************************************

	// *********************************************************************
	// Method: rendezvousPublish
	//
	// Description: Initializes a RDV_PUBLISH packet to send to the
	// Rendezvous Point, and sends it for routing.
	//
	// Returns: (void)
	//
	// Arguments:
	// - k - Distance to Bucket
	// - destination - End host
	//
	// *********************************************************************
	public static void rendezvousPublish(int k, String destination) {

		String rdv_vid;
		Packet myPacket = new Packet();

		// Calculate Rendezvous Point VID
		rdv_vid = veil_ID.substring(0, (veil_ID.length() - k + 1));
		for (int i = 0; i < (k - 1); i++) {
			rdv_vid = rdv_vid + "0";
		}

		// Initialize Packet
		myPacket.setOpcode(RDV_PUBLISH);
		myPacket.setSource(veil_ID);
		myPacket.setDest(rdv_vid);
		myPacket.setPayload(destination);
		myPacket.setTTL("00111111");

		System.out.println("   Publishing Neighbor: " + destination + " to RdvNode: " + rdv_vid);

		// If this switch is the final destination, process the packet here
		if (rdv_vid.equals(veil_ID)) {
			processPacket(myPacket);
			return;
		}
		else{
			numCtrlPckSent++;
			routeCtrlPacket(myPacket);
		}


		myPacket = new Packet();

		// Calculate Backup Rendezvous Point VID
		rdv_vid = veil_ID.substring(0, (veil_ID.length() - k + 1));
		rdv_vid = rdv_vid + "1";
		for (int i = 0; i < (k - 2); i++) {
			rdv_vid = rdv_vid + "0";
		}

		// Initialize Packet
		myPacket.setOpcode(RDV_PUBLISH);
		myPacket.setSource(veil_ID);
		myPacket.setDest(rdv_vid);
		myPacket.setPayload(destination);
		myPacket.setTTL("00111111");

		System.out.println("   Publishing Neighbor: " + destination + " to RdvNode: " + rdv_vid);
		// If this switch is the final destination, process the packet here
		if (rdv_vid.equals(veil_ID)) {
			processPacket(myPacket);
			return;
		}
		numCtrlPckSent++;
		routeCtrlPacket(myPacket);

		return;

	} // End of RendezvousPublish

	// *********************************************************************
	// Method: rendezvousQuery
	//
	// Description: Initializes a RDV_QUERY packet to send to the
	// Rendezvous Point, and sends it for routing.
	//
	// Returns: Binary String formatted as: GATEWAY:NEXTHOP
	//
	// Arguments:
	// - k - Distance to Bucket
	// - vid - Veil ID of this Switch
	//
	// *********************************************************************
	public static void rendezvousQuery(int k) {

		String rdv_vid;
		Packet myPacket = new Packet();

		// Calculate rendezvous Point VID
		rdv_vid = veil_ID.substring(0, (veil_ID.length() - k + 1));
		for (int i = 0; i < (k - 1); i++) {
			rdv_vid = rdv_vid + "0";
		}

		// Initialize Packet
		myPacket.setOpcode(RDV_QUERY);
		myPacket.setSource(veil_ID);
		myPacket.setDest(rdv_vid);
		myPacket.setPayload(leftPadString(Integer.toBinaryString(k), 32, '0'));
		myPacket.setTTL("00111111");
		// finish formatting query packet

		System.out.println("   Querying Rdz: " + rdv_vid + " For bucket " + k);

		// If this switch is the final destination, process the packet here
		if (rdv_vid.equals(veil_ID)) {
			processPacket(myPacket);
			return;
		}

		else{
			numCtrlPckSent++;
			routeCtrlPacket(myPacket);
		}


		myPacket = new Packet();

		// Calculate rendezvous Point VID
		rdv_vid = veil_ID.substring(0, (veil_ID.length() - k + 1));
		rdv_vid = rdv_vid + "1";
		for (int i = 0; i < (k - 2); i++) {
			rdv_vid = rdv_vid + "0";
		}

		// Initialize Packet
		myPacket.setOpcode(RDV_QUERY);
		myPacket.setSource(veil_ID);
		myPacket.setDest(rdv_vid);
		myPacket.setPayload(leftPadString(Integer.toBinaryString(k), 32, '0'));
		myPacket.setTTL("00111111");
		// finish formatting query packet

		System.out.println("   Querying Rdz: " + rdv_vid + " For bucket " + k);

		// If this switch is the final destination, process the packet here
		if (rdv_vid.equals(veil_ID)) {
			processPacket(myPacket);
			return;
		}

		numCtrlPckSent++;
		routeCtrlPacket(myPacket);

		return;


	} // End of Rendezvous Query

	public static void rendezvousUpdate(int k, String destination) {

		String rdv_vid;
		Packet myPacket = new Packet();

		// Calculate Rendezvous Point VID
		rdv_vid = veil_ID.substring(0, (veil_ID.length() - k + 1));
		for (int i = 0; i < (k - 1); i++) {
			rdv_vid = rdv_vid + "0";
		}

		// Initialize Packet
		myPacket.setOpcode(RDV_UPDATE);
		myPacket.setSource(veil_ID);
		myPacket.setDest(rdv_vid);
		myPacket.setPayload(destination);
		myPacket.setTTL("00111111");

		System.out.println("   Updating Neighbor: " + destination + " to RdvNode: " + rdv_vid);
		// If this switch is the final destination, process the packet here
		if (rdv_vid.equals(veil_ID)) {
			processPacket(myPacket);
			return;
		}
		else{
			numCtrlPckSent++;
			routeCtrlPacket(myPacket);
		}


		myPacket = new Packet();
		// Calculate Rendezvous Point VID
		rdv_vid = veil_ID.substring(0, (veil_ID.length() - k + 1));
		rdv_vid = rdv_vid + "1";
		for (int i = 0; i < (k - 2); i++) {
			rdv_vid = rdv_vid + "0";
		}

		// Initialize Packet
		myPacket.setOpcode(RDV_UPDATE);
		myPacket.setSource(veil_ID);
		myPacket.setDest(rdv_vid);
		myPacket.setPayload(destination);
		myPacket.setTTL("00111111");

		System.out.println("   Updating Neighbor: " + destination + " to RdvNode: " + rdv_vid);
		// If this switch is the final destination, process the packet here
		if (rdv_vid.equals(veil_ID)) {
			processPacket(myPacket);
			return;
		}
		numCtrlPckSent++;
		routeCtrlPacket(myPacket);

		return;

	} // End of RendezvousUPDATE

	// *********************************************************************
	// Method: routeCtrlPacket
	//
	// Description:  Handle the control packet, decide its nexthop and send 
	// the packet
	//
	// Returns: Flag indicate whether the packet has been sent.
	//
	// Arguments:
	// - myPacket - Control Packet object to route
	//
	// *********************************************************************
	public static synchronized boolean routeCtrlPacket(Packet myPacket) {

		String nexthop = "";
		String dst = myPacket.getDest();
		StringBuffer buf = new StringBuffer();
		boolean sendResult = false;

		// Check to see if the destination is a physical neighbor
		for (int i = 0; i < adjacent_count; i++) {
			if (dst.equals(adj_hosts[i][1])) {
				nexthop = dst;
			}
		}

		int k = veil_ID.length() - longestCommonPrefixLength(veil_ID, dst);

		RTEntry entry = rTable.entryLookup(veil_ID,k);

		if (!myPacket.getOpcode().equals(RDV_PUBLISH) && !myPacket.getOpcode().equals(RDV_QUERY) && !myPacket.getOpcode().equals(RDV_UPDATE)){
			if (entry == null) {
				System.out.println("No route to host!!");
				return false;
			}
		}

		else{
			// while not successful sent and there are entry left that is smaller than k, keep trying
			while (!sendResult){
				//get a new entry to send
				while (entry == null){

					//Ji: not perform flip since it may miss many possible entry to dst, just decrement k instead
					//	dst = flipBit(dst,k);
					//	k = veil_ID.length() - longestCommonPrefixLength(veil_ID, dst);

					k--;


					if (0 == k){
						numControlPckDrop++;
						return false;
					}

					entry = rTable.entryLookup(veil_ID,k);
				}

				//sent to nexthop of that entry
				sendResult = sendPacket(myPacket,entry.nexthop);

				//delete the RT entry using failed nexthop
				if (sendResult){
					return true;
				}
				veil_switch.pDebug("Switch failure, try another one!");
				removeRTEntry(entry.nexthop);
				entry = rTable.entryLookup(veil_ID,k);
			}
		}

		return sendResult;

	} // End of routeCtrlPacket

	// *********************************************************************
	// Method: routeDataPacket
	//
	// Description:  Handle the data packet, decide its nexthop and send 
	// the packet
	//
	// Returns: Flag indicate whether the packet has been sent.
	//
	// Arguments:
	// - myPacket - Data Packet object to route
	//
	// *********************************************************************
	public static boolean routeDataPacket(Packet myPacket){
		String nexthop = "";
		String forwardVid = myPacket.getFwd();
		StringBuffer buf = new StringBuffer();
		boolean sendResult = false;

		//lookup the nexthop in RT and send packet until successful or no nexthop
		int tempDist = 	veil_switch.K - veil_switch.longestCommonPrefixLength(veil_ID,forwardVid);

		while (!sendResult){
			RTEntry entry = rTable.entryLookup(veil_ID,tempDist);
			if (entry == null){
				numCtrlPckHop++;
				return false;
			}

			sendResult = sendPacket(myPacket,entry.nexthop);
			//delete the RT entry using failed nexthop
			if (sendResult){
				return true;
			}
			veil_switch.pDebug("Switch failure, try another one!");
			removeRTEntry(entry.nexthop);
		}

		return sendResult;
	}

	// *********************************************************************
	// Method: flipBit 
	//
	// Description: Use to flip a paricular bit for a vid
	//
	// Returns: vid which has been fliped.
	//
	// Arguments:
	// - vid - the vid that required to be flip for a particular bit
	// - k - position of the fliped bit
	//
	// *********************************************************************
	public static String flipBit(String vid, int k){
		String temp = "";
		for (int i = 1; i <= vid.length(); i++) {
			if (i == (K - k + 1)) {
				if (vid.charAt(i - 1) == '0') {
					temp = temp + "1";
				} else {
					temp = temp + "0";
				}
			} else {
				temp = temp + vid.charAt(i - 1);
			}
		}
		return temp;
	}


	// *********************************************************************
	// Method: removeRTEntry
	//
	// Description: Use to remove an entry in Routing table
	//
	// Returns: No Return.
	//
	// Arguments:
	// - vid - the viro vid whose routing table required to be removed.
	//
	// *********************************************************************

	public static void removeRTEntry(String vid){

		int k = vidDist(veil_ID,vid);

		//inform the related RDV node 
		rendezvousUpdate(k, vid); 

		rTable.delEntry(vid);
	}

	// *********************************************************************
	// Method: processPacket
	//
	// Description: Handles 'packets' when the destination is this
	// switch instance.
	//
	// Returns: String
	// "ERROR" - If no Gateway has been found, or not requested
	// Binary Gateway - If requested edge was found
	//
	// Arguments:
	// - myPacket - Packet object to process
	//
	// *********************************************************************
	public static void processPacket(Packet myPacket) {

		int op = 0;
		String edge = null;
		Socket dummySocket = null;
		clientHandler myHandler = new clientHandler(dummySocket, "NOHOST", 1, veil_ID);
		StringBuffer buf = new StringBuffer();
		Packet replyPacket = new Packet();
		String edge_dest;
		String edge_source;
		RdvEdge pubEdge;

		if (myPacket.getOpcode().equals("1000"))
			op = 1;
		if (myPacket.getOpcode().equals("2000"))
			op = 2;
		if (myPacket.getOpcode().equals("3000"))
			op = 3;
		if (myPacket.getOpcode().equals("4000"))
			op = 4;

		switch (op) {

			// RDV_PUBLISH
			case 1:
				// Format Binary Source and Payload to Hex String
				buf.delete(0, buf.length());
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(myPacket.getSource().substring(0, 8), 2)), 2, '0'));
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(myPacket.getSource().substring(8, 16), 2)), 2, '0'));
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(myPacket.getSource().substring(16, 24), 2)), 2, '0'));
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(myPacket.getSource().substring(24, 32), 2)), 2, '0'));
				edge_source = buf.toString();

				buf.delete(0, buf.length());
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(myPacket.getPayload().substring(0, 8), 2)), 2, '0'));
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(myPacket.getPayload().substring(8, 16), 2)), 2, '0'));
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(myPacket.getPayload().substring(16, 24), 2)), 2, '0'));
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(myPacket.getPayload().substring(24, 32), 2)), 2, '0'));
				edge_dest = buf.toString();

				// If current edge is not already in Edge Table, Add It

				pubEdge = new RdvEdge(edge_source, edge_dest);
				if (myHandler.writeToRdvTable(pubEdge))
					pDebug("PUBLISH EDGE ADDED!");
				else
					pDebug("PUBLISH EDGE NOT ADDED!");
				break;

				// RDV_QUERY
			case 2:
				// Format Veil ID and Distance for the RdvTable Call
				int bucket = Integer.parseInt(myPacket.getPayload(), 2);
				buf.delete(0, buf.length());
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(veil_ID.substring(0, 8), 2)), 2, '0'));
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(veil_ID.substring(8, 16), 2)), 2, '0'));
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(veil_ID.substring(16, 24), 2)), 2, '0'));
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(veil_ID.substring(24, 32), 2)), 2, '0'));
				String hex_vid = buf.toString();
				buf.delete(0, buf.length());

				String gwList = myHandler.searchRdvTable(hex_vid, Integer.toString(bucket));
				if (gwList.equals("")){
					System.out.println("Ji: Process local Query! GW not available!");
					break;
				}
				else {
					buf.append(leftPadString(Integer.toHexString(Integer.parseInt(gwList.substring(0, 8), 2)), 2, '0'));
					buf.append(leftPadString(Integer.toHexString(Integer.parseInt(gwList.substring(8, 16), 2)), 2, '0'));
					buf.append(leftPadString(Integer.toHexString(Integer.parseInt(gwList.substring(16, 24), 2)), 2, '0'));
					buf.append(leftPadString(Integer.toHexString(Integer.parseInt(gwList.substring(24, 32), 2)), 2, '0'));
					buf.append(leftPadString(Integer.toHexString(Integer.parseInt(gwList.substring(32, 40), 2)), 2, '0'));
					buf.append(leftPadString(Integer.toHexString(Integer.parseInt(gwList.substring(40, 48), 2)), 2, '0'));
					buf.append(leftPadString(Integer.toHexString(Integer.parseInt(gwList.substring(48, 56), 2)), 2, '0'));
					buf.append(leftPadString(Integer.toHexString(Integer.parseInt(gwList.substring(56, 64), 2)), 2, '0'));
					buf.append(leftPadString(Integer.toHexString(Integer.parseInt(gwList.substring(64, 72), 2)), 2, '0'));
					buf.append(leftPadString(Integer.toHexString(Integer.parseInt(gwList.substring(72, 80), 2)), 2, '0'));
					buf.append(leftPadString(Integer.toHexString(Integer.parseInt(gwList.substring(80, 88), 2)), 2, '0'));
					buf.append(leftPadString(Integer.toHexString(Integer.parseInt(gwList.substring(88, 96), 2)), 2, '0'));
					gwList = buf.toString();
					System.out.println("Ji: Process local Query! gwList = " + gwList);
					updateRTEntry(gwList, bucket);
				}
				break;

				// RDV_UPDATE
			case 4:
				// Format Binary Source and Payload to Hex String
				buf.delete(0, buf.length());
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(myPacket.getSource().substring(0, 8), 2)), 2, '0'));
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(myPacket.getSource().substring(8, 16), 2)), 2, '0'));
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(myPacket.getSource().substring(16, 24), 2)), 2, '0'));
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(myPacket.getSource().substring(24, 32), 2)), 2, '0'));
				edge_source = buf.toString();

				buf.delete(0, buf.length());
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(myPacket.getPayload().substring(0, 8), 2)), 2, '0'));
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(myPacket.getPayload().substring(8, 16), 2)), 2, '0'));
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(myPacket.getPayload().substring(16, 24), 2)), 2, '0'));
				buf.append(leftPadString(Integer.toHexString(Integer.parseInt(myPacket.getPayload().substring(24, 32), 2)), 2, '0'));
				edge_dest = buf.toString();

				// If current edge is Rdv Table, 

				pubEdge = new RdvEdge(edge_source, edge_dest);
				myHandler.removeFromRdvTable(pubEdge);
				break;

				// RDV_REPLY - No Replies Should Be Handled Here Handling is contained Packet, flag this as an Error
			case 3:
			default:
				pDebug("ERROR: Invalid Packet OpCode received: " + myPacket.getOpcode());
				break;
		}
		return;

	} // End of processPacket

	// *********************************************************************
	// Method: sendPacket
	//
	// Description: Creates socket, formats packet data for sending,
	// and transmits packet to next host
	//
	// Returns: A flag indicate whether the packet has been sent to nexthop
	//
	// Arguments:
	// - myPacket - Packet object to process
	// - nexthop - Next hop to deliver packet to
	//
	// *********************************************************************
	public static synchronized boolean sendPacket(Packet myPacket, String nexthop) {

		try {
			String targetPhysID = "notset";
			int op = 0;
			if (myPacket.getOpcode().equals("0000"))
				op = 0;
			if (myPacket.getOpcode().equals("1000"))
				op = 16;
			if (myPacket.getOpcode().equals("2000"))
				op = 32;
			if (myPacket.getOpcode().equals("3000"))
				op = 48;
			if (myPacket.getOpcode().equals("4000"))
				op = 64;

			//myPacket.setTTL("00000000");
			for (int i = 0; i < adjacent_count; i++) {
				if (adj_hosts[i][1].equals(nexthop)) {
					targetPhysID = adj_hosts[i][0];
					break;
				}
			}

			if (targetPhysID.equals("notset")) {
				System.out.println("sendPacket:: Unable to resolve Physical Address of Host: " + myPacket.getDest());
				System.exit(-1);
			}

			System.out.println("open socket to " + targetPhysID);

			// Parse Host and Port from Target Physical ID
			String delim = ":";
			String tokens[] = targetPhysID.split(delim);
			String target_host = tokens[0];
			int target_port = Integer.parseInt(tokens[1]);

			Socket outgoing = new Socket(target_host, target_port);
			OutputStream osOut = outgoing.getOutputStream();
			InputStream isOut = outgoing.getInputStream();
			StringBuffer buf = new StringBuffer();

			// Extract Packet Contents and Format for Transmission
			if (myPacket.getOpcode().equals("1000"))
				op = 16;
			if (myPacket.getOpcode().equals("2000"))
				op = 32;
			if (myPacket.getOpcode().equals("3000"))
				op = 48;

			if ( op == 0 ){

				pDebug("DATA Packet Sent:");
				pDebug("SRC: " + myPacket.getSource());
				pDebug("DST: " + myPacket.getDest());
				pDebug("FWD: " + myPacket.getFwd());
				pDebug("TTL: " + myPacket.getTTL());

				byte[] b = { 0, 1, 8, 0, 6, 4, (byte) op, 0,
					(byte) Integer.parseInt(myPacket.getSource().substring(0, 8), 2),
					(byte) Integer.parseInt(myPacket.getSource().substring(8, 16), 2),
					(byte) Integer.parseInt(myPacket.getSource().substring(16, 24), 2),
					(byte) Integer.parseInt(myPacket.getSource().substring(24, 32), 2),
					(byte) Integer.parseInt(myPacket.getDest().substring(0, 8), 2),
					(byte) Integer.parseInt(myPacket.getDest().substring(8, 16), 2),
					(byte) Integer.parseInt(myPacket.getDest().substring(16, 24), 2),
					(byte) Integer.parseInt(myPacket.getDest().substring(24, 32), 2),
					(byte) Integer.parseInt(myPacket.getFwd().substring(0, 8), 2),
					(byte) Integer.parseInt(myPacket.getFwd().substring(8, 16), 2),
					(byte) Integer.parseInt(myPacket.getFwd().substring(16, 24), 2),
					(byte) Integer.parseInt(myPacket.getFwd().substring(24, 32), 2),
					(byte) Integer.parseInt(myPacket.getTTL(), 2) 
				};
				// Write Packet to Socket
				osOut.write(b);

				outgoing.close();
				return true;
			}
			else {

				pDebug("CTRL Packet Sent:");
				pDebug("SRC: " + myPacket.getSource());
				pDebug("DST: " + myPacket.getDest());
				pDebug("PLD: " + myPacket.getPayload());
				pDebug("TTL: " + myPacket.getTTL());

				byte[] b = { 0, 1, 8, 0, 6, 4, (byte) op, 0,
					(byte) Integer.parseInt(myPacket.getSource().substring(0, 8), 2),
					(byte) Integer.parseInt(myPacket.getSource().substring(8, 16), 2),
					(byte) Integer.parseInt(myPacket.getSource().substring(16, 24), 2),
					(byte) Integer.parseInt(myPacket.getSource().substring(24, 32), 2),
					(byte) Integer.parseInt(myPacket.getDest().substring(0, 8), 2),
					(byte) Integer.parseInt(myPacket.getDest().substring(8, 16), 2),
					(byte) Integer.parseInt(myPacket.getDest().substring(16, 24), 2),
					(byte) Integer.parseInt(myPacket.getDest().substring(24, 32), 2),
					(byte) Integer.parseInt(myPacket.getPayload().substring(0, 8), 2),
					(byte) Integer.parseInt(myPacket.getPayload().substring(8, 16), 2),
					(byte) Integer.parseInt(myPacket.getPayload().substring(16, 24), 2),
					(byte) Integer.parseInt(myPacket.getPayload().substring(24, 32), 2),
					(byte) Integer.parseInt(myPacket.getTTL(), 2) 
				};


				if (myPacket.getOpcode().equals("1000"))
					buf.append("   -> Sending RDZ PUBLISH = ");
				if (myPacket.getOpcode().equals("2000"))
					buf.append("   -> Sending RDZ QUERY = ");
				if (myPacket.getOpcode().equals("3000"))
					buf.append("   -> Sending RDZ REPLY = ");
				if (myPacket.getOpcode().equals("4000"))
					buf.append("   -> Sending RDZ UPDATE = ");

				for (int j = 0; j < 21; j++) {
					buf.append(Integer.toString((b[j] & 0xff) + 0x100, 16).substring(1));
				}
				System.out.println(buf.toString());
				buf.delete(0, buf.length());
				// Write Packet to Socket
				osOut.write(b);
				outgoing.close();

				return true;
			}



		} catch (IOException e) {
			System.out.println(" Not able to send Packet through this port");
			failSendPacketVid = nexthop;
			return false;
		}


	} // End of sendPacket 


	// *********************************************************************
	// Method: longestCommonPrefixLength
	//
	// Description: Returns the LCP of two strings
	//
	// Returns: (int) Longest Common Prefix of Two Strings
	//
	// Arguments:
	// - Strings to compare (s & t)
	// *********************************************************************
	public static int longestCommonPrefixLength(String s, String t) {
		int m = Math.min(s.length(), t.length());
		for (int k = 0; k < m; ++k)
			if (s.charAt(k) != t.charAt(k))
				return k;
		return m;

	} // End of longestCommonPrefixLength

	// *********************************************************************
	// Method: leftPadString
	//
	// Description: Left Pads a string with a given character
	//
	// Returns: (int) Longest Common Prefix of Two Strings
	//
	// Arguments:
	// - s - String to Pad
	// - n - Desired final length of string
	// - c - Character to use for padding
	// *********************************************************************
	public static String leftPadString(String s, int n, char c) {

		int add = n - s.length();

		if (add <= 0) {
			return s;
		}

		StringBuffer str = new StringBuffer(s);
		char[] ch = new char[add];
		Arrays.fill(ch, c);

		str.insert(0, ch);

		return str.toString();

	} // End of leftPadString

	// *********************************************************************
	// Method: rightPadString
	//
	// Description: Right Pads a string with a given character
	//
	// Returns: (int) Longest Common Prefix of Two Strings
	//
	// Arguments:
	// - s - String to Pad
	// - n - Desired final length of string
	// - c - Character to use for padding
	// *********************************************************************
	public static String rightPadString(String s, int n, char c) {

		int add = n - s.length();

		if (add <= 0) {
			return s;
		}

		StringBuffer str = new StringBuffer(s);
		char[] ch = new char[add];
		Arrays.fill(ch, c);

		str.insert(s.length(), ch);

		return str.toString();

	} // End of rightPadString

	// *********************************************************************
	// Method: getHosts
	//
	// Description: Returns a table of all hosts
	//
	// Returns: String [] [] - Array of hosts
	//
	// Arguments:
	// - None.
	// *********************************************************************
	public static String[][] getHosts() {
		return vid_hosts;
	}

	// *********************************************************************
	// Method: getTopoSize
	//
	// Description: Returns a table of all hosts
	//
	// Returns: String [] [] - Array of hosts
	//
	// Arguments:
	// - None.
	// *********************************************************************
	public static int getTopoSize() {
		return topology_size;
	}

	// *********************************************************************
	// Method: getPID
	//
	// Description: Returns Physical ID of this switch
	//
	// Returns: String
	//
	// Arguments:
	// - None.
	// *********************************************************************
	public static String getPID() {
		return phys_ID;
	}

	// *********************************************************************
	// Method: checkEntryExpire
	//
	// Description: Use to check whether Routing table has expired entry
	//
	// Returns: None.
	//
	// Arguments:
	// - None.
	// *********************************************************************
	public static synchronized void checkEntryExpire(){
		rTable.checkEntryExpire();
	}

	// *********************************************************************
	// Method: vidDist
	//
	// Description: Use to calculate the logical distance of two vids
	//
	// Returns:  an integer value.
	//
	// Arguments:
	// - s - the source vid
	// - t - the destination vid
	// *********************************************************************
	public static int vidDist(String s, String t) {
		return (veil_ID.length() - longestCommonPrefixLength(s,t));

	}

	// *********************************************************************
	// Method: pDebug
	//
	// Description: Use to print debug information if printDebug flag is set
	//
	// Returns:  None
	//
	// Arguments:
	// - str - the debug infomation to be printed.
	// *********************************************************************
	public static void pDebug(String str){
		if (printDebug)
			System.out.println(str);
	}

	// *********************************************************************
	// Method: compareEntry
	//
	// Description: Use to check whether two entries are identical
	//
	// Returns:  A flag indicate whether two entries are identical
	//
	// Arguments:
	// - str1 - The first entry
	// - str2 - The second entry
	// *********************************************************************
	public static boolean compareEntry(String[] str1, String[] str2) {
		if (str1[1].equals(str2[1]) && str1[2].equals(str2[2])) {
			return true;
		}
		return false;
	}

	// *********************************************************************
	// Method: getPrifix
	//
	// Description: Calculate the prefix String for a vid
	//
	// Returns:  an String prefix padding by '*'.
	//
	// Arguments:
	// - vid1 - the object vid
	// - k - the position for prefix
	// *********************************************************************
	public static String getPrefix(String vid1,int k){


		String prefix = vid1.substring(0, (vid1.length() - k));
		if (vid1.substring(vid1.length() - k, vid1.length() - k + 1).equals("0")) {
			prefix = prefix + "1";
		} else {
			prefix = prefix + "0";
		}
		prefix = rightPadString(prefix, vid1.length(), '*');

		return prefix;
	}
	// *********************************************************************
	// Method: updateRTEntry
	//
	// Description: Update routing table using the derived gateway list in 
	// particular bucket level
	//
	// Returns: None 
	//
	// Arguments:
	// - gwList - Gateway list
	// - bucket - The paricular bucket of the routing table need to be updated.
	// *********************************************************************
	public static synchronized void updateRTEntry(String gwList, int bucket) {

		if (gwList.equals("ERROR")) {
			return;
		} 

		else {
			for (int ij = 0; ij < 3; ij++) {
				boolean entryExist = false;
				String gateWay1 = gwList.substring(8 * ij, 8 * (ij + 1));

				String nexthop1 = new String();

				if (gateWay1.equals("ffffffff")) {
					continue;
				}
				gateWay1 = leftPadString(Integer.toBinaryString(Integer.parseInt(gateWay1, 16)), K, '0');
				int k = veil_ID.length() - longestCommonPrefixLength(veil_ID, gateWay1);
				if (k == 0) {
					for (int j = 0; j < adjacent_count; j++) {
						int dis = veil_ID.length() - longestCommonPrefixLength(veil_ID, adj_hosts[j][1]);
						if (bucket == dis) {
							nexthop1 = adj_hosts[j][1];
							break;
						}

					}

				} else {
					for (int i = 0; i < rTable.size(); i++) {
						RTEntry rtEntry = rTable.getEntry(i);
						if (rtEntry.bucket == k) {
							nexthop1 = rtEntry.nexthop;
							break;
						}

					}
				}

				if(nexthop1.equals("")){

					System.out.println("Ji: No Nexthop for Gateway, RT not updated!");
					return;
				}

				String prefix = getPrefix(veil_ID,bucket);

				RTEntry rtEntry = new RTEntry(bucket,nexthop1,gateWay1,prefix,"N");

				rTable.addEntry(rtEntry);


			}
			return;

		}
	}

	// *********************************************************************
	// Method: printRT
	//
	// Description: Use to print the routing table
	//
	// Returns:  None
	//
	// Arguments:
	// None.
	// *********************************************************************
	public static void printRT() {
		System.out.println("");
		System.out.println("**********************************************************   ROUTING TABLE START   ***********************************************************");
		rTable.printRT();
		System.out.println("**********************************************************    ROUTING TABLE END    ***********************************************************");
	}

	// *********************************************************************
	// Method: RTbuilder
	//
	// Description: Build Initial Routing Table By Looping Through All Known Neighbors
	//
	// Returns:  None
	//
	// Arguments:
	// None.
	// *********************************************************************
	public static void RTbuilder(){

		String adj_vid = null;
		for (int i = 0; i < adjacent_count; i++) {
			// Find Veil ID of Adjacent Host
			for (int j = 0; j < topology_size; j++) {
				if (vid_hosts[j][0].equals(adj_hosts[i][0])) {
					adj_vid = vid_hosts[j][1];
					adj_hosts[i][1] = adj_vid;
				}
			}

			int k = vidDist(veil_ID,adj_vid);

			String prefix = getPrefix(veil_ID,k);

			RTEntry rtEntry = new RTEntry(k,adj_vid,veil_ID,prefix,"Y");

			rTable.addEntry(rtEntry);
		}
	}


	// *********************************************************************
	// Method: RTbuilder
	//
	// Description: Routing Table Construction Loop
	//
	// Returns:  None
	//
	// Arguments:
	// None.
	// *********************************************************************
	public static void RTConstrut(){
		//for (int round = 1; round <=vid_size; round++)

		for (int bucket = 1; bucket <= vid_size; bucket++) {

			pDebug("######### BUCKET = " + bucket + "#########");
			int end_index = rTable.size();
			// Loop Through Current Routing Table Entries
			for (int j = 0; j < end_index; j++) {
				RTEntry rtEntry = rTable.getEntry(j);
				if (veil_ID.equals(rtEntry.gateway)) {
					rendezvousPublish(rtEntry.bucket,rtEntry.nexthop); 
				} 


			}

			rendezvousQuery(bucket);


			try {
				// Wait for a period of time before running the next round.
				Thread.sleep(WAIT_TIME);
			} catch (Exception e) {
				System.err.println("Error: " + e.getMessage());
			}
			printRT();

		}

	}
} // End of veil_switch Class
// END OF FILE

