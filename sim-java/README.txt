1. Main Class files:

veil_switch.java

	Purpose: Creates an instance of a 'veil_switch'.  This switch emulates the 
           functionality of a routing node in VEIL.  Via command line input, it
           is initialized with the Physical ID it is to assume, a list of its
           neighboring nodes, as well as a mapping of all Physical IDs to their
           respective Veil IDs.  

           Once properly initialized, it will start building a routing table to be
           used for packet forwarding.  It will first add entries for all of its
           physical neighbors, and then subsequently attempt to discover routes to
           other switches through a series of publication and query messages.

           After the table has been built, the switch will print its final rounting
           table to the terminal and wait for a certain interval to restart the routing
		   table building process.
           

	Dependencies:
	  - clientHandler.java - Contains all the incoming client request handling
                             as well as the hosttable and rdvtable objects, 
                             along with their related methods.

      - socketMonitor.java - Used by veil_switch to create a seperate thread to
                             listen for incoming connections.  Connections are
                             handed off to a clientHandler thread for prccessing.

      - Packet.java        - Defines the data object 'Packet', and its related
                             methods.
  



							
clientHandler.java

 	Purpose: Communications processing thread.  Called by class
           veil_switch (via SocketMonitor) to process incoming requests 
           from clients, update list of host to Veil ID mappings, update
           rdvtable and generate response packets where applicable.

	State Variables:
	  - cltSocket - Socket object handed off from veil_switch
	  - remote_host - Hostname/IP of remote server (from veil_switch)
	  - remote_port - Listening Port of remote server (from veil_switch)
      - host_table - Two dimensional array to store host->VID mappings
      - curIndex - Next available index in host_table for writing
      - veil_ID - VID of this veil_switch instance
      - veil_ID_hex - Hex format of this switch's VID, used for assigning VID to hosts
      - host_ID_high - Most Significant Byte of next available Host ID
      - host_ID_low - Least Significant Byte of next available Host ID


	Class Methods:
	  - readIndex - Thread safe method for reading value of curIndex
      - incIndex - Thread safe method to increment curIndex
      - WriteHostTable - Thread safe method to write mappings to host_table
      - SearchHostTable - Thread safe method to find mappings in host_table
      - GetHostID - Thread safe method to calculate hostID portion of new Veil IDs
      - convertHex - Returns byte format when passed a one byte Hex String
      - FindEdge - Thread safe method to find an edge at a given logical distance
      - readEdgeIndex - Thread safe method for reading value of curEdgeIndex
      - incEdgeIndex - Thread safe method to increment curEdgeIndex
      - longestCommonPrefixLength - Gives LCP of two binary strings
      - leftPadString - Left pads a string with a given character

	  

SocketMonitor.java

 	Purpose: Monitors a Socket for incoming connection requests.  When a connection
           is received, it is handed off to the class clientHandler, and the 
           process returns to monitoring the socket.



Packet.java

 	Purpose: Object containing information for packets exchanged between
           veil switches.

	Variables:
	  - opCode     - Operation Code for Packet
      - src        - Source Host of Packet
      - dest       - Destination Host of Packet
      - payload    - First Payload Field
      - fwd		   - Fowording directive (not used for all packet types)
      - ttl		   - TTL (not used for all packet types)

	Methods:
      - getOpCode     - Return value of opCode
      - setOpcode     - Set value of opCode
      - getSource     - Return value of src
      - setSource     - Set value of sec
      - getDest       - Return value of dest
      - setDest       - Set value of dest
      - getFwd    	  - Return value of forwarding directive
      - setFwd    	  - Set value of payload
      - getPayload 	  - Return value of Payload
      - setPayload 	  - Set value of Payload
      - getTTL 		  - Return value of TTL
      - setTTL 		  - Set value of TTL


2. Auxillary Class files:
	  
RdvTable.java/RdvEdge.java/RdvTableEntry.java
	Purpose: Object use to manage rdv table and perform various 
			 of table operation such as add/update/delete/search.

	Variables:
	  - rdvtable    - one rdv table contains a list of rdv table entries.
	  - edge		- one rdv table entry contains a pair of src/dest pair.
      - dest        - first node in edge
      - src         - second node in edge
      - sTime       - the long type time entry has been created.
	  
	Methods:
      - addEntry     	- add entry into rdv table
      - addEdge      	- add edge into rdv table
      - delEdge      	- delete edge in rdv table
      - edgeExist    	- check whether edge exist in rdv table
      - delEntry        - delete entry in rdv table
      - updateEntry     - update existing entry in rdv table
      - findEntry    	- find a specific entry in rdv table
      - setTime 	  	- record the current time when new edge is created
      - printRdvTable   - Print entire rdv table
      - printEdge 	    - Print an edge 
      - printRdvEntry 	- Print an Rdv entry

RTable.java/RTEntry.java
	Purpose: Object use to manage routing table and perform various 
			 of table operation such as add/update/delete/search.

	Variables:
	  - rtable      - one routing table contains a list of routing table entries.
	  - nexthop		- the String nexthop vid in one routing table entry.
      - gateway     - the String gateway vid in one routing table entry.
      - prefix      - the String prefix in one routing table entry.
      - d_gateway   - the String default gateway in one routing table entry.
      - sTime       - the long type time entry has been created.
	  
	Methods:
      - addEntry     		 - add entry into routing table
      - delEntry        	 - delete entry in routing table
      - checkEntryExpire     - check whether an entry expired
      - numofEntryPerBucket  - get number of entries in a given bucket
      - entryLookup    		 - find a specific entry in routing table
      - setTime 	  		 - record the current when new entry is created
      - printRTEntry 	     - Print an routing table entry
      - printRT 		 	 - Print entire routing table
	  
      
3. Auxillary files:

*.vid
		
	Purpose: define the vid mapping file for each nodes in simulation.

	Format(each line):
			Physical_ID <space> Virtual_ID
	
	Note: since in current simulation all nodes are running on same machine. Physical_ID
		  can be represented as localhost:port_num
		
*.adlist

	Purpose: define the graph adjacency file for the network topology in simulation.

	Format(each line):
			Self_PhyID : Neighbor_PhyID1 Neighbor_PhyID2 Neighbor_PhyID3 ...
	
	Note: since in current simulation all nodes are running on same machine. PhyID
		  can be represented as localhost:port_num

*.workload

	Purpose: define the workload file for the simulation.
	
	Note: currently only use constant workflow. More complicated workflow model like Mapreduce 
		  should be developed.
			
***How to initiate a signle instance***	  
	
	Command:  java veil_switch adjacency_list vid_list physical_ID
					adjacency_list - File containing the network topology.  This is used
                            by veil_switch to determine its physical neighbors.
					vid_list       - File containing mappings of Physical IDs to Veil IDs
                            for all switches in the topology.
					physical_ID    - The physical ID (in the form of hostname:port) that
                            the switch is to assume.

					
4. Auxillary scripts:		

fat-tree.sh
	- One simulation scripts for 20-nodes fat-tree topology. 
	
kill.sh
	- Use to kill running instances silently. (make sure to kill existing hung process 
	  before new simulation)
	  
traffic-gen.py
	- use to generate the packet workflow.
	
parse.sh
	- parse the simulation result by printing out the sending and received data/ctrl packets. 
		

		
		
		
Some Implementation Detail

The routing table contains 5-tuple entry, which are Level, Prefix, Gateway, Nexthop and Default. 
The RDV table stores the available gateway entry, which is link pairs (<VID>,<VID>)
The host table stores the host entry under each switch, which is 
(<HOST_IP>,<HOST_VID>).

The timeout mechanism has been used for all these tables to let them expire and get removed after
certain amount of time. This way in case there is a link or switch failure, the change can be 
reflected in routing table/rendezvous table/host table.

Multipath Routing and Failure Re-routing Support

Data packet 
The forwarding directive field has been used to forward the packet along the route path to avoid 
the potential loop issue. Upon the arrival of a data packet, the VIRO switch will randomly pick up 
one of the three(at most) entries in that bucket to forward the packet. In case some of these entries 
fail, the VIRO switch can always re-route use other entries. One of the five cases would apply when 
a packet (whose destination field set to <DST>, forwarding directive set to <FWD>) are received by a 
VIRO switch whose own vid is <VID>. The VIRO switch will use following policy to handle the packet.

1.  If <VID> = <DST> then the packet has been delivered.
2.  If <DST> = one of my neighbor's vid, then packet sent to this neighbor directly.
3.  If <FWD> = <VID>, which means I am the initially selected gateway, then the <FWD> will be set same
 as <DST> and packet is forwarded to one of the neighbors in <DST> bucket.
4.  If <FWD> = <DST> , which means I am the source VIRO switch (host switch) or the nexthop of one gateway
 . The switch will look up the routing table and pick one entry in that bucket. Two sub cases applied 
 depended on the entry. If in the entry, the gateway vid is <VID>, then set the forwarding field same to
 <DST>, otherwise set the forwarding field same to <VID>.
5. If none of the above applies, which means I am the intermediate forwarder, then the packet just get 
  forwarding without change.


Control Packet
When receiving a control packet, the VIRO switch will check whether it¡¯s the destination , if not, it
 will randomly pick up one of the existing entries in that bucket to forward the packet. Since there 
 is no use of forward directive, I just use the TTL to eliminate the forwarding loop.
 
To handle the scenario when rdv switch crash, I use a backup rdv node. The primary RVs for bucket k 
will has vid ended with k's 0, and the backup RVs will have one 1 followed by (k-1)'s 0. For instance, 
if the node VID is 01001, then its two RDVs in bucket 3 will be 01000 and 01100, while bucket 4 will be
00000 and 01000. The reason for this approach is that we have to reserve VID space for new switch in last 
several bits. If the two RVs has larger prefix, it is likely that they are not available and control 
packet redirect to the same nearby existing switch.


Limitation and future work.
1.	Only one vid can be supported for each switch so far, therefore in order to support multi-tenant virtual 
VIRO, it is required to initiate several processes to represent the multiple VIRO instances in one switch.
2.	The workload file format does not match the simulation yet. More complicated workload should be developed 
to simulate the real data center network.
