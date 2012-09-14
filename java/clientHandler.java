import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class clientHandler implements Runnable {

	// *********************************************************************
	// clientHandler Class Constants
	// *********************************************************************
	// Declare Maximum Number of Hosts that can be stored by a single veil_switch
	// Real World Maximum is 65,535 based on 16-Bit Host ID portion of Veil ID,
	// due to 00:00 being reserved for switches.
	public static final int MAX_HOSTS = 65535;

	// Declare Maximum Number of Edges to Store
	public static final int MAX_EDGES = 512;

	// *********************************************************************
	// clientHandler Class Variable Declarations
	// *********************************************************************
	private Socket cltSocket;
	private String remote_host;
	private int remote_port;
	private static String[][] host_table = new String[MAX_HOSTS][3]; // [MAC]
	private static RdvTable rdvtable = new RdvTable();
	private static int curIndex = 0;
	private static int curEdgeIndex = 0;
	private String veil_ID;
	private String veil_ID_hex;
	private static int host_ID_high = 0;
	private static int host_ID_low = 1; // Start at 1, 0 is Reserved for

	// Switches

	// *********************************************************************
	// clientHandler Methods
	// *********************************************************************
	public static synchronized void updateRdvTable() {
		rdvtable.updateEntry();
	}

	// *********************************************************************
	// Method: readIndex
	//
	// Returns: (int) Next availabe index for writing to host_table
	//
	// Arguments:
	// - None.
	//
	// *********************************************************************
	public synchronized int readIndex() {
		return curIndex;
	}

	// *********************************************************************
	// Method: incIndex
	//
	// Returns: (void)
	//
	// Arguments:
	// - None.
	//
	// *********************************************************************
	public synchronized void incIndex() {
		this.curIndex = curIndex + 1;
	}

	// *********************************************************************
	// Method: WriteHostTable
	//
	// Returns: (void)
	//
	// Arguments:
	// - x - First index to write in host_table
	// - y - Second index to write in host_table
	// 0 - MAC Address
	// 1 - IP Address
	// 2 - Veil ID
	// - toStore - String to write in host_table at index x,y
	//
	// *********************************************************************
	public synchronized void WriteHostTable(int x, int y, String toStore) {
		host_table[x][y] = toStore;
	}

	// *********************************************************************
	// Method: writeToRdvTable
	//
	// Returns: boolean flag indicate whether successful
	//
	// Arguments:
	// - RdvEdge - The Edge that to be written
	//
	// *********************************************************************
	public synchronized boolean writeToRdvTable(RdvEdge rdvEdge) {
		return rdvtable.addEdge(rdvEdge);
	}

	// *********************************************************************
	// Method: removeFromRdvTable
	//
	// Returns: boolean flag indicate whether successful
	//
	// Arguments:
	// - RdvEdge - The Edge that to be removed
	//
	// *********************************************************************
	public synchronized boolean removeFromRdvTable(RdvEdge rdvEdge) {
		return rdvtable.delEdge(rdvEdge);
	}

	// *********************************************************************
	// Method: searchRdvTable
	//
	// Returns: The corresponding edges
	//
	// Arguments:
	// - host - The host vid to be searched
	// - k_str - The logic distance of bucket being searched
	// *********************************************************************
	public synchronized String searchRdvTable(String host, String k_str) {
		return rdvtable.findEntry(host, k_str);
	}

	// *********************************************************************
	// Method: printRdvTable
	//
	// Returns: None
	//
	// Arguments:
	// None
	// *********************************************************************

	public static void printRdvTable() {
		rdvtable.printRdvTable();
	}

	// *********************************************************************
	// Method: SearchHostTable
	//
	// Returns: (String) VID if found, or "NoMatch" for a failed search
	//
	// Arguments:
	// - toFind - IP address of Host to look up
	//
	// *********************************************************************
	public synchronized String SearchHostTable(String toFind) {

		for (int i = 0; i < readIndex(); i++) {
			if (toFind.equals(host_table[i][1])) {
				return host_table[i][2];
			}
		}

		// Didn't Find the Mapping
		return "NoMatch";
	}

	// *********************************************************************
	// Method: GetHostID
	//
	// Returns: (String) New unique HostID for creating VID when a
	// registration request is received.
	//
	// Arguments:
	// - None
	//
	// *********************************************************************
	public synchronized String GetHostID() {
		StringBuffer buf = new StringBuffer();
		int newValuelow = host_ID_low;
		int newValuehigh = host_ID_high;

		buf.append(Integer.toString((newValuehigh & 0xff) + 0x100, 16).substring(1)).append(":");
		buf.append(Integer.toString((newValuelow & 0xff) + 0x100, 16).substring(1));
		if (host_ID_low == 255) {
			host_ID_low = 0;
			host_ID_high = host_ID_high + 1;
		} else {
			host_ID_low = host_ID_low + 1;
		}

		return buf.toString();
	}

	// *********************************************************************
	// Method: longestCommonPrefixLength
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
	}

	// *********************************************************************
	// Method: leftPadString
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
	}

	// *********************************************************************
	// Method: clientHandler - Constructor
	//
	// Returns: None
	//
	// Arguments:
	// - cltSocket - Socketed handed off from veil_switch
	// - remote_host - Hostname/IP of Veil Master Server
	// - remote_port - Port to connect with Veil Master
	// - veil_ID - First four bytes of this veil_switch's Veil ID
	//
	// *********************************************************************
	public clientHandler(Socket cltSocket, String remote_host, int remote_port, String veil_ID) {
		this.cltSocket = cltSocket;
		this.remote_host = remote_host;
		this.remote_port = remote_port;
		this.veil_ID = veil_ID;

	}

	// *********************************************************************
	// Method: run
	//
	// Returns: None
	//
	// Arguments:
	// - None
	//
	// *********************************************************************
	public void run() {

		String[] input = new String[36];

		veil_switch mySwitch = new veil_switch();
		Packet myPacket = new Packet();

		try {
			InputStream is = cltSocket.getInputStream();
			OutputStream os = cltSocket.getOutputStream();
			StringBuffer buf = new StringBuffer();

			buf.delete(0, buf.length());
			buf.append(leftPadString(Integer.toHexString(Integer.parseInt(veil_ID.substring(0, 8), 2)), 2, '0'));
			buf.append(":");
			buf.append(leftPadString(Integer.toHexString(Integer.parseInt(veil_ID.substring(8, 16), 2)), 2, '0'));
			buf.append(":");
			buf.append(leftPadString(Integer.toHexString(Integer.parseInt(veil_ID.substring(16, 24), 2)), 2, '0'));
			buf.append(":");
			buf.append(leftPadString(Integer.toHexString(Integer.parseInt(veil_ID.substring(24, 32), 2)), 2, '0'));
			String veil_ID_hex = buf.toString();
			buf.delete(0, buf.length());

			int i = 0;

			// Read Input from Client, and Store in array 'input'.
			int in = is.read();
			while (in != -1) {
				input[i] = Integer.toString((in & 0xff) + 0x100, 16).substring(1);
				in = is.read();
				if (i == 0)
					buf.append("   <- Packet Received = ");
				buf.append(input[i]);
				i = i + 1;
				if (i >= 19) {
					if (input[7].equals("01") && i == 27)
						break;
					if (input[6].equals("05") && i == 20)
						break;
					if (input[6].equals("10") && i == 20)
						break;
					if (input[6].equals("20") && i == 20)
						break;
					if (input[6].equals("40") && i == 20)
						break;
					// Ji Three gateways i ==32
					// if (input[6].equals("30") && i == 23)
					// break;
				}
			}

			// Read Last Byte From Input Stream
			input[i] = Integer.toString((in & 0xff) + 0x100, 16).substring(1);
			buf.append(input[i]);
			i = 0;

			cltSocket.shutdownInput();

			System.out.println("----------");

			// End of Initial Read

			// Process the Packet that was Read in Based on its OpCode
			// 0000 - NORMAL DATA
			// 1000 - RDZ PUBLISH
			// 2000 - RDZ QUERY
			// 3000 - RDZ REPLY
			// 4000 - RDZ UPDATE
			
			// **************************************************
			// NORMAL DATA PACKET Handler - 0000
			// **************************************************
			if (input[6].equals("00") && input[7].equals("00")) {
				String sourceVid = new String();
				sourceVid = input[8] + input[9] + input[10] + input[11];
				sourceVid = veil_switch.leftPadString(Integer.toBinaryString(Integer.parseInt(sourceVid, 16)), 32, '0');

				String destVid = new String();
				destVid = input[12] + input[13] + input[14] + input[15];
				destVid = veil_switch.leftPadString(Integer.toBinaryString(Integer.parseInt(destVid, 16)), 32, '0');

				myPacket.setOpcode("0000");
				myPacket.setSource(sourceVid);
				myPacket.setDest(destVid);

				String forwardVid = new String();
				forwardVid = input[16] + input[17] + input[18] + input[19];
				forwardVid = veil_switch.leftPadString(Integer.toBinaryString(Integer.parseInt(forwardVid, 16)), 32, '0');

				String ttl = new String();
				ttl = input[20];
				if ((Integer.parseInt(ttl, 16) - 1) == 0) {
					// if ttl-1 == 0
					veil_switch.numDataPckTTL++;
					veil_switch.pDebug("Packet Drop: TTL");
					return;
				} else {
					ttl = veil_switch.leftPadString(Integer.toBinaryString(Integer.parseInt(ttl, 16) - 1), 8, '0');
				}
				myPacket.setTTL(ttl);

				int dist_K = veil_switch.K - veil_switch.longestCommonPrefixLength(sourceVid, destVid);

				veil_switch.pDebug("Rcv Packet format:");
				veil_switch.pDebug("SRC: " + sourceVid);
				veil_switch.pDebug("DST: " + destVid);
				veil_switch.pDebug("FWD: " + forwardVid);
				veil_switch.pDebug("TTL: " + ttl);

				veil_switch.numDataPckSent++;

				boolean sendResult = false;

				int isPhysicalNei = 0;

				for (int ij = 0; ij < veil_switch.adjacent_count; ij++) {
					if (veil_switch.adj_hosts[ij][1].equals(destVid))
						isPhysicalNei = 1;
				}

				// case 1: if destVid is myself
				if (destVid.equals(veil_ID)) {
					veil_switch.pDebug("case 1");
					veil_switch.numDataPckSent--;
					veil_switch.numDataPckRecv++;
					sendResult = true;

					// add some variables to record to the results
				}
				// case 2: if destVid is one of my physical neighbor
				else if (isPhysicalNei == 1) {
					veil_switch.pDebug("case 2");
					myPacket.setFwd(destVid);
					veil_switch.pDebug("plan to open socket to " + destVid);

					sendResult = veil_switch.sendPacket(myPacket, destVid);
					if (!sendResult) {

						veil_switch.pDebug("case 2: Drop packet!!");
						veil_switch.numDataPckDst++;
						return;
					}

				}

				// case 3: if I am the initially selected gateway
				else if (forwardVid.equals(veil_ID)) {
					veil_switch.pDebug("case 3");
					myPacket.setFwd(destVid);

					sendResult = veil_switch.routeDataPacket(myPacket);

					if (!sendResult) {
						veil_switch.numDataPckHop++;
						veil_switch.pDebug("case 3: No Nexthop, drop packet!");
					}

				}

				// case 4: if I am the source or the node after initial gateway
				else if (destVid.equals(forwardVid)) {
					veil_switch.pDebug("case 4");
					int tmpdist = veil_switch.K - veil_switch.longestCommonPrefixLength(veil_ID, destVid);
					while (!sendResult) {
						RTEntry entry = veil_switch.rTable.entryLookup(veil_ID, tmpdist);
						if (entry == null) {
							veil_switch.numDataPckHop++;
							veil_switch.pDebug("case 4: No Nexthop, drop packet!");
							return;
						}

						// the source happen to be the gateway
						if (entry.gateway.equals(veil_ID)) {
							veil_switch.pDebug("case 4.1");
							myPacket.setFwd(destVid);
						}

						// the source is not the gateway
						else {
							veil_switch.pDebug("case 4.2");
							myPacket.setFwd(entry.gateway);
						}

						sendResult = veil_switch.sendPacket(myPacket, entry.nexthop);

						if (!sendResult)
							veil_switch.removeRTEntry(entry.nexthop);
					}
				}

				// case 5: if I am the intermediate forwarder
				else {
					myPacket.setFwd(forwardVid);
					boolean isNbr = false;
					// case 5.1 if forwadVid is my neighbor
					for (int ij = 0; ij < veil_switch.adjacent_count; ij++) {
						if (veil_switch.adj_hosts[ij][1].equals(forwardVid)) {
							isNbr = true;
							break;
						}
					}

					if (isNbr) {
						veil_switch.pDebug("case 5.1");
						sendResult = veil_switch.sendPacket(myPacket, forwardVid);
					}

					if (!sendResult)
						sendResult = veil_switch.routeDataPacket(myPacket);

					if (!sendResult) {
						veil_switch.pDebug("case 5: No Nexthop, drop packet!");
						veil_switch.numDataPckHop++;
					}
				}

			}

			// **************************************************
			// RDZ PUBLISH Handler - 1000
			// **************************************************
			if (input[6].equals("10") && input[7].equals("00")) {

				buf.append(" -- RDV PUBLISH");
				System.out.println(buf.toString());

				String ttl = new String();
				ttl = input[20];
				if ((Integer.parseInt(ttl, 16) - 1) == 0) {
					// if ttl-1 == 0
					veil_switch.numCtrlPckTTL++;
					veil_switch.pDebug("Packet Drop: TTL");
					return;
				} else {
					ttl = veil_switch.leftPadString(Integer.toBinaryString(Integer.parseInt(ttl, 16) - 1), 8, '0');
				}

				// Extract Edge Dst from Packet
				String dstVid = input[12] + input[13] + input[14] + input[15];
				dstVid = veil_switch.leftPadString(Integer.toBinaryString(Integer.parseInt(dstVid, 16)), veil_switch.K, '0');

				if (dstVid.equals(veil_ID)) {

					veil_switch.pDebug("Ji: c1000:I am the destination");
					veil_switch.numCtrlPckRecv++;

					// Extract Edge Source from Packet
					buf.delete(0, buf.length());
					buf.append(input[8]);
					buf.append(input[9]);
					buf.append(input[10]);
					buf.append(input[11]);
					String edge_source = buf.toString();

					// Extract Edge Destination from Packet
					buf.delete(0, buf.length());
					buf.append(input[16]);
					buf.append(input[17]);
					buf.append(input[18]);
					buf.append(input[19]);
					String edge_dest = buf.toString();

					// Check to See if Edge is Already in Table
					// Only Add it if it is not already in the table.
					RdvEdge pubEdge = new RdvEdge(edge_source, edge_dest);
					if (rdvtable.addEdge(pubEdge))
						veil_switch.pDebug("PUBLISH EDGE ADDED!");
					else
						veil_switch.pDebug("PUBLISH EDGE NOT ADDED!");
				}

				else {
					veil_switch.pDebug("Ji: c1000: I am not the destination");

					String sourceVid = new String();
					sourceVid = input[8] + input[9] + input[10] + input[11];
					sourceVid = veil_switch.leftPadString(Integer.toBinaryString(Integer.parseInt(sourceVid, 16)), 32,
							'0');

					String RdvVid = new String();
					RdvVid = input[16] + input[17] + input[18] + input[19];
					RdvVid = veil_switch.leftPadString(Integer.toBinaryString(Integer.parseInt(RdvVid, 16)), 32, '0');

					myPacket.setOpcode("1000");
					myPacket.setSource(sourceVid);
					myPacket.setDest(dstVid);
					myPacket.setPayload(RdvVid);
					myPacket.setTTL(ttl);

					veil_switch.routeCtrlPacket(myPacket);

				}

			} // End of RDZ PUBLISH Handler

			// **************************************************
			// RDZ QUERY Handler - 2000
			// **************************************************
			if (input[6].equals("20") && input[7].equals("00")) {

				buf.append(" -- RDV QUERY");
				veil_switch.pDebug(buf.toString());

				String ttl = new String();
				ttl = input[20];
				if ((Integer.parseInt(ttl, 16) - 1) == 0) {
					// if ttl-1 == 0
					veil_switch.numCtrlPckTTL++;
					veil_switch.pDebug("Packet Drop: TTL");
					return;
				} else {
					ttl = veil_switch.leftPadString(Integer.toBinaryString(Integer.parseInt(ttl, 16) - 1), 8, '0');
				}

				// Extract Edge Dst from Packet
				String dstVid = input[12] + input[13] + input[14] + input[15];
				dstVid = veil_switch.leftPadString(Integer.toBinaryString(Integer.parseInt(dstVid, 16)), veil_switch.K,
						'0');

				if (dstVid.equals(veil_ID)) {

					veil_switch.pDebug("Ji: c2000:I am the destination");
					veil_switch.numCtrlPckRecv++;

					// Extract Source from Packet
					buf.delete(0, buf.length());
					buf.append(input[8]);
					buf.append(input[9]);
					buf.append(input[10]);
					buf.append(input[11]);
					String source = buf.toString();

					// Extract k from Packet
					buf.delete(0, buf.length());
					buf.append(input[16]);
					buf.append(input[17]);
					buf.append(input[18]);
					buf.append(input[19]);
					String k = buf.toString();

					String result = searchRdvTable(source, k);
					if (!result.equals("")) {

						String dst = leftPadString(Integer.toBinaryString(Integer.parseInt(source, 16)), veil_switch.K,
								'0');

						String targetPhysID = "";

						String nexthop = "";

						// Check to see if the destination is a physical
						// neighbor
						for (int ij = 0; ij < veil_switch.adjacent_count; ij++) {
							if (dst.equals(veil_switch.adj_hosts[ij][1])) {
								nexthop = dst;
							}
						}

						int dist = veil_ID.length() - longestCommonPrefixLength(veil_ID, dst);

						for (int ij = 0; ij < veil_switch.rTable.size(); ij++) {
							RTEntry rtEntry = veil_switch.rTable.getEntry(ij);
							if (dist == rtEntry.bucket) {
								nexthop = rtEntry.nexthop;
								break;
							}
						}

						for (int ij = 0; ij < veil_switch.topology_size; ij++) {
							if (veil_switch.vid_hosts[ij][1].equals(nexthop)) {
								targetPhysID = veil_switch.vid_hosts[ij][0];
								break;
							}
						}

						if (!targetPhysID.equals("")) {

							System.out.println("open socket to " + targetPhysID);

							// Parse Host and Port from Target Physical ID
							String delim = ":";
							String tokens[] = targetPhysID.split(delim);
							String target_host = tokens[0];
							int target_port = Integer.parseInt(tokens[1]);

							Socket cSocket = new Socket(target_host, target_port);
							OutputStream cltOut = cSocket.getOutputStream();
							InputStream cltIn = cSocket.getInputStream();

							byte[] b = { 
								0, 1, 8, 0, 6, 4, 48, 0, (byte) Integer.parseInt(veil_ID.substring(0, 8), 2),
								(byte) Integer.parseInt(veil_ID.substring(8, 16), 2),
								(byte) Integer.parseInt(veil_ID.substring(16, 24), 2),
								(byte) Integer.parseInt(veil_ID.substring(24, 32), 2), convertHex(input[8]),
								convertHex(input[9]), convertHex(input[10]), convertHex(input[11]),
								convertHex(input[16]), convertHex(input[17]), convertHex(input[18]),
								convertHex(input[19]), (byte) Integer.parseInt(result.substring(0, 8), 2),
								(byte) Integer.parseInt(result.substring(8, 16), 2),
								(byte) Integer.parseInt(result.substring(16, 24), 2),
								(byte) Integer.parseInt(result.substring(24, 32), 2),
								(byte) Integer.parseInt(result.substring(32, 40), 2),
								(byte) Integer.parseInt(result.substring(40, 48), 2),
								(byte) Integer.parseInt(result.substring(48, 56), 2),
								(byte) Integer.parseInt(result.substring(56, 64), 2),
								(byte) Integer.parseInt(result.substring(64, 72), 2),
								(byte) Integer.parseInt(result.substring(72, 80), 2),
								(byte) Integer.parseInt(result.substring(80, 88), 2),
								(byte) Integer.parseInt(result.substring(88, 96), 2),
								63
							};

							buf.delete(0, buf.length());
							buf.append("   -> Sending RDZ REPLY = ");
							for (int j = 0; j < 33; j++) {
								buf.append(Integer.toString((b[j] & 0xff) + 0x100, 16).substring(1));
							}

							System.out.println(buf.toString());
							cltOut.write(b);
							cSocket.close();
							veil_switch.numCtrlPckSent++;
						} else {
							System.out.println("Ji:c2000: targetPhysID not available!");
							veil_switch.numControlPckDrop++;
						}

					} else {
						System.out.println("Ji: Gateway not found, No RDZ REPLY!");

					}
				}

				else {
					veil_switch.pDebug("Ji: RDV QUERY: I am not the destination");

					String sourceVid = new String();
					sourceVid = input[8] + input[9] + input[10] + input[11];
					sourceVid = veil_switch.leftPadString(Integer.toBinaryString(Integer.parseInt(sourceVid, 16)), 32,
							'0');

					String Payload = new String();
					Payload = input[16] + input[17] + input[18] + input[19];
					Payload = veil_switch.leftPadString(Integer.toBinaryString(Integer.parseInt(Payload, 16)), 32, '0');

					myPacket.setOpcode("2000");
					myPacket.setSource(sourceVid);
					myPacket.setDest(dstVid);
					myPacket.setPayload(Payload);
					myPacket.setTTL(ttl);

					veil_switch.routeCtrlPacket(myPacket);

				}

			} // End of RDV QUERY Handler

			// **************************************************
			// RDZ REPLY Handler - 3000
			// **************************************************
			if (input[6].equals("30") && input[7].equals("00")) {
				buf.append("   <- RDZ REPLY Received = ");

				String ttl = new String();
				ttl = input[32];
				if ((Integer.parseInt(ttl, 16) - 1) == 0) {
					// if ttl-1 == 0
					veil_switch.numCtrlPckTTL++;
					veil_switch.pDebug("Packet Drop: TTL");
					return;
				} 
				else {
					ttl = veil_switch.leftPadString(Integer.toBinaryString(Integer.parseInt(ttl, 16) - 1), 8, '0');
				}


				// Extract Edge Dst from Packet
				String dstVid = input[12] + input[13] + input[14] + input[15];
				dstVid = veil_switch.leftPadString(Integer.toBinaryString(Integer.parseInt(dstVid, 16)), veil_switch.K,
						'0');

				if (dstVid.equals(veil_ID)) {
					veil_switch.pDebug("Ji: c3000:I am the destination,update rtable entry");
					veil_switch.numCtrlPckRecv++;

					for (int j = 0; j < 33; j++) {
						if (input[j] == null)
							break;
						buf.append(input[j]);
					}
					System.out.println(buf.toString());
					buf.delete(0, buf.length());

					for (int j = 20; j < 32; j++) {
						buf.append(input[j]);
					}

					System.out.println("gwList = " + buf.toString());
					String gwList = buf.toString();
					buf.delete(0, buf.length());
					buf.append(input[16]);
					buf.append(input[17]);
					buf.append(input[18]);
					buf.append(input[19]);
					String k_str = buf.toString();
					int bucket = Integer.parseInt(k_str);
					veil_switch.updateRTEntry(gwList, bucket);
				} else {

					veil_switch.pDebug("Ji: c3000:I not am the destination, forwarding");

					String targetPhysID = "";

					String nexthop = "";

					// Check to see if the destination is a physical neighbor
					for (int ij = 0; ij < veil_switch.adjacent_count; ij++) {
						if (dstVid.equals(veil_switch.adj_hosts[ij][1])) {
							nexthop = dstVid;
						}
					}

					int dist = veil_ID.length() - longestCommonPrefixLength(veil_ID, dstVid);

					for (int ij = 0; ij < veil_switch.rTable.size(); ij++) {
						RTEntry rtEntry = veil_switch.rTable.getEntry(ij);
						if (dist == rtEntry.bucket) {
							nexthop = rtEntry.nexthop;
							break;
						}
					}

					for (int ij = 0; ij < veil_switch.topology_size; ij++) {
						if (veil_switch.vid_hosts[ij][1].equals(nexthop)) {
							targetPhysID = veil_switch.vid_hosts[ij][0];
							break;
						}
					}

					if (!targetPhysID.equals("")) {

						System.out.println("open socket to " + targetPhysID);
						String delim = ":";
						String tokens[] = targetPhysID.split(delim);
						String target_host = tokens[0];
						int target_port = Integer.parseInt(tokens[1]);

						Socket cSocket = new Socket(target_host, target_port);
						OutputStream cltOut = cSocket.getOutputStream();
						InputStream cltIn = cSocket.getInputStream();
							System.out.println("Here: " + ttl);
							System.out.println("Here: " + Integer.parseInt(ttl, 2));
							System.out.println("Here: " + (byte) Integer.parseInt(ttl, 2));

						byte[] b = { convertHex(input[0]), convertHex(input[1]), convertHex(input[2]),
							convertHex(input[3]), convertHex(input[4]), convertHex(input[5]), convertHex(input[6]),
							convertHex(input[7]), convertHex(input[8]), convertHex(input[9]),
							convertHex(input[10]), convertHex(input[11]), convertHex(input[12]),
							convertHex(input[13]), convertHex(input[14]), convertHex(input[15]),
							convertHex(input[16]), convertHex(input[17]), convertHex(input[18]),
							convertHex(input[19]), convertHex(input[20]), convertHex(input[21]),
							convertHex(input[22]), convertHex(input[23]), convertHex(input[24]),
							convertHex(input[25]), convertHex(input[26]), convertHex(input[27]),
							convertHex(input[28]), convertHex(input[29]), convertHex(input[30]),
							convertHex(input[31]), 
							(byte) Integer.parseInt(ttl, 2) 
							};

						buf.delete(0, buf.length());
						buf.append("   -> Sending RDZ REPLY = ");
						for (int j = 0; j < 33; j++) {
							buf.append(Integer.toString((b[j] & 0xff) + 0x100, 16).substring(1));
						}

						System.out.println(buf.toString());
						cltOut.write(b);
						cSocket.close();
					} else {
						System.out.println("targetPhysID not available!");
						veil_switch.numControlPckDrop++;

					}

				}
			} // End of RDZ REPLY Handler

			// **************************************************
			// RDZ UPDATE Handler - 4000
			// **************************************************
			if (input[6].equals("40") && input[7].equals("00")) {

				buf.append(" -- RDV UPDATE");
				System.out.println(buf.toString());

				String ttl = new String();
				ttl = input[20];
				if ((Integer.parseInt(ttl, 16) - 1) == 0) {
					// if ttl-1 == 0
					veil_switch.numCtrlPckTTL++;
					veil_switch.pDebug("Packet Drop: TTL");
					return;
				} else {
					ttl = veil_switch.leftPadString(Integer.toBinaryString(Integer.parseInt(ttl, 16) - 1), 8, '0');
				}

				// Extract Edge Dst from Packet
				String dstVid = input[12] + input[13] + input[14] + input[15];
				dstVid = veil_switch.leftPadString(Integer.toBinaryString(Integer.parseInt(dstVid, 16)), veil_switch.K, '0');

				if (dstVid.equals(veil_ID)) {

					veil_switch.pDebug("Ji: c4000:I am the destination");
					veil_switch.numCtrlPckRecv++;

					// Extract Edge Source from Packet
					buf.delete(0, buf.length());
					buf.append(input[8]);
					buf.append(input[9]);
					buf.append(input[10]);
					buf.append(input[11]);
					String edge_source = buf.toString();

					// Extract Edge Destination from Packet
					buf.delete(0, buf.length());
					buf.append(input[16]);
					buf.append(input[17]);
					buf.append(input[18]);
					buf.append(input[19]);
					String edge_dest = buf.toString();

					// Check to See if Edge is Already in Table  Only remove it if it is already in the table.
					RdvEdge pubEdge = new RdvEdge(edge_source, edge_dest);
					if (rdvtable.delEdge(pubEdge))
						veil_switch.pDebug("UPDATE EDGE DELETED!");
					else
						veil_switch.pDebug("UPDATE EDGE NOT DELETED!");
				}

				else {
					veil_switch.pDebug("Ji: c4000: I am not the destination");

					String sourceVid = new String();
					sourceVid = input[8] + input[9] + input[10] + input[11];
					sourceVid = veil_switch.leftPadString(Integer.toBinaryString(Integer.parseInt(sourceVid, 16)), 32, '0');

					String PayLoad = new String();
					PayLoad = input[16] + input[17] + input[18] + input[19];
					PayLoad = veil_switch.leftPadString(Integer.toBinaryString(Integer.parseInt(PayLoad, 16)), 32, '0');

					myPacket.setOpcode("4000");
					myPacket.setSource(sourceVid);
					myPacket.setDest(dstVid);
					myPacket.setPayload(PayLoad);
					myPacket.setTTL(ttl);

					veil_switch.routeCtrlPacket(myPacket);

				}

			} // End of RDZ UPDATE Handler

			// **************************************************

			// HOST REGISTRATION Handler - 0002

			// **************************************************

			// Extra Checking is needed to ensure this is not simply an
			// ARP_REPLY

			// for an 'Unknown Host'.

			if (input[6].equals("00") && input[7].equals("02") && !input[8].equals("ff") && !input[9].equals("ff")
					&& !input[10].equals("ff") && !input[11].equals("ff") && !input[12].equals("ff")
					&& !input[13].equals("ff") && input[18].equals("ff") && input[19].equals("ff")
					&& input[20].equals("ff") && input[21].equals("ff") && input[22].equals("ff")
					&& input[23].equals("ff")) {

				buf.append("  -- HOST_REGISTRATION");

				System.out.println(buf.toString());

				// Parse New Host MAC Address and Store

				buf.delete(0, buf.length());
				buf.append(input[8]).append(":");
				buf.append(input[9]).append(":");
				buf.append(input[10]).append(":");
				buf.append(input[11]).append(":");
				buf.append(input[12]).append(":");
				buf.append(input[13]);

				WriteHostTable(readIndex(), 0, buf.toString());

				// Parse New Host IP Address and Store

				buf.delete(0, buf.length());
				buf.append(input[14]).append(":");
				buf.append(input[15]).append(":");
				buf.append(input[16]).append(":");
				buf.append(input[17]);

				WriteHostTable(readIndex(), 1, buf.toString());

				// Parse New Host VID and Store

				buf.delete(0, buf.length());
				buf.append(veil_ID_hex).append(":");
				buf.append(GetHostID());

				WriteHostTable(readIndex(), 2, buf.toString());

				System.out.println("IP \u001b[5;1m" + host_table[readIndex()][1]
						+ "\u001b[m added to hosts table at Index " + readIndex() + " with VID of \u001b[5;1m"
						+ host_table[readIndex()][2] + ".\u001b[m");

				// Send STORE REQUESTS for new host

				String hosts[][] = mySwitch.getHosts();

				System.out.println("Forwarding STORE REQUESTS for new host.");

				for (i = 0; i < mySwitch.getTopoSize(); i++) {

					if (!hosts[i][0].equals(mySwitch.getPID())) {

						String delim = ":";

						String tokens[] = hosts[i][0].split(delim);

						// System.out.print("Round " + i + " host: " + tokens[0]
						// + " " + tokens[1]);

						Socket phoneHome = new Socket(tokens[0], Integer.parseInt(tokens[1]));

						OutputStream osMaster = phoneHome.getOutputStream();

						byte[] b = { convertHex(input[0]), convertHex(input[1]), convertHex(input[2]),
							convertHex(input[3]), convertHex(input[4]), convertHex(input[5]), 7, 0,
							convertHex(veil_ID_hex.substring(0, 2)), convertHex(veil_ID_hex.substring(3, 5)),
							convertHex(veil_ID_hex.substring(6, 8)), convertHex(veil_ID_hex.substring(9, 11)), 0,
							0, 0, 0, 0, 0, 0, 0, convertHex(host_table[readIndex()][1].substring(0, 2)),
							convertHex(host_table[readIndex()][1].substring(3, 5)),
							convertHex(host_table[readIndex()][1].substring(6, 8)),
							convertHex(host_table[readIndex()][1].substring(9, 11)),
							convertHex(veil_ID_hex.substring(0, 2)), convertHex(veil_ID_hex.substring(3, 5)),
							convertHex(veil_ID_hex.substring(6, 8)), convertHex(veil_ID_hex.substring(9, 11)),
							convertHex(host_table[readIndex()][2].substring(12, 14)),
							convertHex(host_table[readIndex()][2].substring(15, 17)) };

						osMaster.write(b);
						phoneHome.close();

					}
				}

				incIndex(); // Move index to next empty table entry.

					} // End of HOST REGISTRATION Handler


			PrintStream writeLogFile = new PrintStream(new FileOutputStream(new File("data.txt"), true));
			writeLogFile.println(
					  "ID :" + veil_switch.my_port 
					+ "\tDPkt St:" 	+ veil_switch.numDataPckSent 
					+ "\tRv: " 	+ veil_switch.numDataPckRecv
					+ "\tDFail TTL:"+ veil_switch.numDataPckTTL 
					+ "\tNohop:" 	+ veil_switch.numDataPckHop 
					+ "\tDstFail:" 	+ veil_switch.numDataPckDst
					+ "----"
					+ "\tCPkt St:" 	+ veil_switch.numCtrlPckSent 
					+ "\tRv:" 	+ veil_switch.numCtrlPckRecv 
					+ "\tCFail TTL:"+ veil_switch.numCtrlPckTTL  
					+ "\tNohop:" 	+ veil_switch.numCtrlPckHop
					+ "\tDstFail:" 	+ veil_switch.numCtrlPckDst 
					);

			writeLogFile.close();

			cltSocket.close();

		} catch (IOException e) {
			System.out.println("clientHandler:: Exception on client connection.");
			System.exit(-1);
		}

	} // Thread Exits and Dies

	// *********************************************************************
	// Method: convertHex
	//
	// Returns: (byte) Byte value corresponding to one byte Hex String passed
	//
	// Arguments:
	// - input - One Byte Hex String to convert
	//
	// Reason:
	// - Sheer and utter frustration trying to get any of the native
	// conversion functions to work properly.
	// *********************************************************************
	public static byte convertHex(String input) {
		if (input.equals("00"))
			return (byte) 0;
		if (input.equals("01"))
			return (byte) 1;
		if (input.equals("02"))
			return (byte) 2;
		if (input.equals("03"))
			return (byte) 3;
		if (input.equals("04"))
			return (byte) 4;
		if (input.equals("05"))
			return (byte) 5;
		if (input.equals("06"))
			return (byte) 6;
		if (input.equals("07"))
			return (byte) 7;
		if (input.equals("08"))
			return (byte) 8;
		if (input.equals("09"))
			return (byte) 9;
		if (input.equals("0a"))
			return (byte) 10;
		if (input.equals("0b"))
			return (byte) 11;
		if (input.equals("0c"))
			return (byte) 12;
		if (input.equals("0d"))
			return (byte) 13;
		if (input.equals("0e"))
			return (byte) 14;
		if (input.equals("0f"))
			return (byte) 15;
		if (input.equals("10"))
			return (byte) 16;
		if (input.equals("11"))
			return (byte) 17;
		if (input.equals("12"))
			return (byte) 18;
		if (input.equals("13"))
			return (byte) 19;
		if (input.equals("14"))
			return (byte) 20;
		if (input.equals("15"))
			return (byte) 21;
		if (input.equals("16"))
			return (byte) 22;
		if (input.equals("17"))
			return (byte) 23;
		if (input.equals("18"))
			return (byte) 24;
		if (input.equals("19"))
			return (byte) 25;
		if (input.equals("1a"))
			return (byte) 26;
		if (input.equals("1b"))
			return (byte) 27;
		if (input.equals("1c"))
			return (byte) 28;
		if (input.equals("1d"))
			return (byte) 29;
		if (input.equals("1e"))
			return (byte) 30;
		if (input.equals("1f"))
			return (byte) 31;
		if (input.equals("20"))
			return (byte) 32;
		if (input.equals("21"))
			return (byte) 33;
		if (input.equals("22"))
			return (byte) 34;
		if (input.equals("23"))
			return (byte) 35;
		if (input.equals("24"))
			return (byte) 36;
		if (input.equals("25"))
			return (byte) 37;
		if (input.equals("26"))
			return (byte) 38;
		if (input.equals("27"))
			return (byte) 39;
		if (input.equals("28"))
			return (byte) 40;
		if (input.equals("29"))
			return (byte) 41;
		if (input.equals("2a"))
			return (byte) 42;
		if (input.equals("2b"))
			return (byte) 43;
		if (input.equals("2c"))
			return (byte) 44;
		if (input.equals("2d"))
			return (byte) 45;
		if (input.equals("2e"))
			return (byte) 46;
		if (input.equals("2f"))
			return (byte) 47;
		if (input.equals("30"))
			return (byte) 48;
		if (input.equals("31"))
			return (byte) 49;
		if (input.equals("32"))
			return (byte) 50;
		if (input.equals("33"))
			return (byte) 51;
		if (input.equals("34"))
			return (byte) 52;
		if (input.equals("35"))
			return (byte) 53;
		if (input.equals("36"))
			return (byte) 54;
		if (input.equals("37"))
			return (byte) 55;
		if (input.equals("38"))
			return (byte) 56;
		if (input.equals("39"))
			return (byte) 57;
		if (input.equals("3a"))
			return (byte) 58;
		if (input.equals("3b"))
			return (byte) 59;
		if (input.equals("3c"))
			return (byte) 60;
		if (input.equals("3d"))
			return (byte) 61;
		if (input.equals("3e"))
			return (byte) 62;
		if (input.equals("3f"))
			return (byte) 63;
		if (input.equals("40"))
			return (byte) 64;
		if (input.equals("41"))
			return (byte) 65;
		if (input.equals("42"))
			return (byte) 66;
		if (input.equals("43"))
			return (byte) 67;
		if (input.equals("44"))
			return (byte) 68;
		if (input.equals("45"))
			return (byte) 69;
		if (input.equals("46"))
			return (byte) 70;
		if (input.equals("47"))
			return (byte) 71;
		if (input.equals("48"))
			return (byte) 72;
		if (input.equals("49"))
			return (byte) 73;
		if (input.equals("4a"))
			return (byte) 74;
		if (input.equals("4b"))
			return (byte) 75;
		if (input.equals("4c"))
			return (byte) 76;
		if (input.equals("4d"))
			return (byte) 77;
		if (input.equals("4e"))
			return (byte) 78;
		if (input.equals("4f"))
			return (byte) 79;
		if (input.equals("50"))
			return (byte) 80;
		if (input.equals("51"))
			return (byte) 81;
		if (input.equals("52"))
			return (byte) 82;
		if (input.equals("53"))
			return (byte) 83;
		if (input.equals("54"))
			return (byte) 84;
		if (input.equals("55"))
			return (byte) 85;
		if (input.equals("56"))
			return (byte) 86;
		if (input.equals("57"))
			return (byte) 87;
		if (input.equals("58"))
			return (byte) 88;
		if (input.equals("59"))
			return (byte) 89;
		if (input.equals("5a"))
			return (byte) 90;
		if (input.equals("5b"))
			return (byte) 91;
		if (input.equals("5c"))
			return (byte) 92;
		if (input.equals("5d"))
			return (byte) 93;
		if (input.equals("5e"))
			return (byte) 94;
		if (input.equals("5f"))
			return (byte) 95;
		if (input.equals("60"))
			return (byte) 96;
		if (input.equals("61"))
			return (byte) 97;
		if (input.equals("62"))
			return (byte) 98;
		if (input.equals("63"))
			return (byte) 99;
		if (input.equals("64"))
			return (byte) 100;
		if (input.equals("65"))
			return (byte) 101;
		if (input.equals("66"))
			return (byte) 102;
		if (input.equals("67"))
			return (byte) 103;
		if (input.equals("68"))
			return (byte) 104;
		if (input.equals("69"))
			return (byte) 105;
		if (input.equals("6a"))
			return (byte) 106;
		if (input.equals("6b"))
			return (byte) 107;
		if (input.equals("6c"))
			return (byte) 108;
		if (input.equals("6d"))
			return (byte) 109;
		if (input.equals("6e"))
			return (byte) 110;
		if (input.equals("6f"))
			return (byte) 111;
		if (input.equals("70"))
			return (byte) 112;
		if (input.equals("71"))
			return (byte) 113;
		if (input.equals("72"))
			return (byte) 114;
		if (input.equals("73"))
			return (byte) 115;
		if (input.equals("74"))
			return (byte) 116;
		if (input.equals("75"))
			return (byte) 117;
		if (input.equals("76"))
			return (byte) 118;
		if (input.equals("77"))
			return (byte) 119;
		if (input.equals("78"))
			return (byte) 120;
		if (input.equals("79"))
			return (byte) 121;
		if (input.equals("7a"))
			return (byte) 122;
		if (input.equals("7b"))
			return (byte) 123;
		if (input.equals("7c"))
			return (byte) 124;
		if (input.equals("7d"))
			return (byte) 125;
		if (input.equals("7e"))
			return (byte) 126;
		if (input.equals("7f"))
			return (byte) 127;
		if (input.equals("80"))
			return (byte) 128;
		if (input.equals("81"))
			return (byte) 129;
		if (input.equals("82"))
			return (byte) 130;
		if (input.equals("83"))
			return (byte) 131;
		if (input.equals("84"))
			return (byte) 132;
		if (input.equals("85"))
			return (byte) 133;
		if (input.equals("86"))
			return (byte) 134;
		if (input.equals("87"))
			return (byte) 135;
		if (input.equals("88"))
			return (byte) 136;
		if (input.equals("89"))
			return (byte) 137;
		if (input.equals("8a"))
			return (byte) 138;
		if (input.equals("8b"))
			return (byte) 139;
		if (input.equals("8c"))
			return (byte) 140;
		if (input.equals("8d"))
			return (byte) 141;
		if (input.equals("8e"))
			return (byte) 142;
		if (input.equals("8f"))
			return (byte) 143;
		if (input.equals("90"))
			return (byte) 144;
		if (input.equals("91"))
			return (byte) 145;
		if (input.equals("92"))
			return (byte) 146;
		if (input.equals("93"))
			return (byte) 147;
		if (input.equals("94"))
			return (byte) 148;
		if (input.equals("95"))
			return (byte) 149;
		if (input.equals("96"))
			return (byte) 150;
		if (input.equals("97"))
			return (byte) 151;
		if (input.equals("98"))
			return (byte) 152;
		if (input.equals("99"))
			return (byte) 153;
		if (input.equals("9a"))
			return (byte) 154;
		if (input.equals("9b"))
			return (byte) 155;
		if (input.equals("9c"))
			return (byte) 156;
		if (input.equals("9d"))
			return (byte) 157;
		if (input.equals("9e"))
			return (byte) 158;
		if (input.equals("9f"))
			return (byte) 159;
		if (input.equals("a0"))
			return (byte) 160;
		if (input.equals("a1"))
			return (byte) 161;
		if (input.equals("a2"))
			return (byte) 162;
		if (input.equals("a3"))
			return (byte) 163;
		if (input.equals("a4"))
			return (byte) 164;
		if (input.equals("a5"))
			return (byte) 165;
		if (input.equals("a6"))
			return (byte) 166;
		if (input.equals("a7"))
			return (byte) 167;
		if (input.equals("a8"))
			return (byte) 168;
		if (input.equals("a9"))
			return (byte) 169;
		if (input.equals("aa"))
			return (byte) 170;
		if (input.equals("ab"))
			return (byte) 171;
		if (input.equals("ac"))
			return (byte) 172;
		if (input.equals("ad"))
			return (byte) 173;
		if (input.equals("ae"))
			return (byte) 174;
		if (input.equals("af"))
			return (byte) 175;
		if (input.equals("b0"))
			return (byte) 176;
		if (input.equals("b1"))
			return (byte) 177;
		if (input.equals("b2"))
			return (byte) 178;
		if (input.equals("b3"))
			return (byte) 179;
		if (input.equals("b4"))
			return (byte) 180;
		if (input.equals("b5"))
			return (byte) 181;
		if (input.equals("b6"))
			return (byte) 182;
		if (input.equals("b7"))
			return (byte) 183;
		if (input.equals("b8"))
			return (byte) 184;
		if (input.equals("b9"))
			return (byte) 185;
		if (input.equals("ba"))
			return (byte) 186;
		if (input.equals("bb"))
			return (byte) 187;
		if (input.equals("bc"))
			return (byte) 188;
		if (input.equals("bd"))
			return (byte) 189;
		if (input.equals("be"))
			return (byte) 190;
		if (input.equals("bf"))
			return (byte) 191;
		if (input.equals("c0"))
			return (byte) 192;
		if (input.equals("c1"))
			return (byte) 193;
		if (input.equals("c2"))
			return (byte) 194;
		if (input.equals("c3"))
			return (byte) 195;
		if (input.equals("c4"))
			return (byte) 196;
		if (input.equals("c5"))
			return (byte) 197;
		if (input.equals("c6"))
			return (byte) 198;
		if (input.equals("c7"))
			return (byte) 199;
		if (input.equals("c8"))
			return (byte) 200;
		if (input.equals("c9"))
			return (byte) 201;
		if (input.equals("ca"))
			return (byte) 202;
		if (input.equals("cb"))
			return (byte) 203;
		if (input.equals("cc"))
			return (byte) 204;
		if (input.equals("cd"))
			return (byte) 205;
		if (input.equals("ce"))
			return (byte) 206;
		if (input.equals("cf"))
			return (byte) 207;
		if (input.equals("d0"))
			return (byte) 208;
		if (input.equals("d1"))
			return (byte) 209;
		if (input.equals("d2"))
			return (byte) 210;
		if (input.equals("d3"))
			return (byte) 211;
		if (input.equals("d4"))
			return (byte) 212;
		if (input.equals("d5"))
			return (byte) 213;
		if (input.equals("d6"))
			return (byte) 214;
		if (input.equals("d7"))
			return (byte) 215;
		if (input.equals("d8"))
			return (byte) 216;
		if (input.equals("d9"))
			return (byte) 217;
		if (input.equals("da"))
			return (byte) 218;
		if (input.equals("db"))
			return (byte) 219;
		if (input.equals("dc"))
			return (byte) 220;
		if (input.equals("dd"))
			return (byte) 221;
		if (input.equals("de"))
			return (byte) 222;
		if (input.equals("df"))
			return (byte) 223;
		if (input.equals("e0"))
			return (byte) 224;
		if (input.equals("e1"))
			return (byte) 225;
		if (input.equals("e2"))
			return (byte) 226;
		if (input.equals("e3"))
			return (byte) 227;
		if (input.equals("e4"))
			return (byte) 228;
		if (input.equals("e5"))
			return (byte) 229;
		if (input.equals("e6"))
			return (byte) 230;
		if (input.equals("e7"))
			return (byte) 231;
		if (input.equals("e8"))
			return (byte) 232;
		if (input.equals("e9"))
			return (byte) 233;
		if (input.equals("ea"))
			return (byte) 234;
		if (input.equals("eb"))
			return (byte) 235;
		if (input.equals("ec"))
			return (byte) 236;
		if (input.equals("ed"))
			return (byte) 237;
		if (input.equals("ee"))
			return (byte) 238;
		if (input.equals("ef"))
			return (byte) 239;
		if (input.equals("f0"))
			return (byte) 240;
		if (input.equals("f1"))
			return (byte) 241;
		if (input.equals("f2"))
			return (byte) 242;
		if (input.equals("f3"))
			return (byte) 243;
		if (input.equals("f4"))
			return (byte) 244;
		if (input.equals("f5"))
			return (byte) 245;
		if (input.equals("f6"))
			return (byte) 246;
		if (input.equals("f7"))
			return (byte) 247;
		if (input.equals("f8"))
			return (byte) 248;
		if (input.equals("f9"))
			return (byte) 249;
		if (input.equals("fa"))
			return (byte) 250;
		if (input.equals("fb"))
			return (byte) 251;
		if (input.equals("fc"))
			return (byte) 252;
		if (input.equals("fd"))
			return (byte) 253;
		if (input.equals("fe"))
			return (byte) 254;
		if (input.equals("ff"))
			return (byte) 255;

		return 0;
	} // End of convertHex

} // End of clientHandler class

// END OF FILE
