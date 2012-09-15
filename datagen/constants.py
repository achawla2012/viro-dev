#########################################
#    GLOBAL VARIABLES
#########################################

# NEW OPERATIONS
RDV_PUBLISH = 0x1000
RDV_QUERY = 0x2000
RDV_REPLY = 0x3000

# OPERATIONS
ECHO_REQUEST = 0x0500
ECHO_REPLY = 0x0600
STORE_REQUEST = 0x0700
SWITCH_REGISTER_REQUEST = 0x0900
SWITCH_REGISTER_REPLY = 0x0A00

ARP_REPLY = 0x02
ARP_REQUEST = 0x01
R_ARP_REPLY = 0x04
R_ARP_REQUEST = 0x03
# OTHER PARTS OF THE HEADER
HTYPE = 0x1
PTYPE = 0x0800
HLEN = 0x06
PLEN = 0x04

# LENGTH OF THE HEADER in bytes
HEADER_LEN = 8

# HARDWARE ADDRESS FOR THE VEIL MASTER
VEIL_MASTER="00:00:00:00:00:00"

# OFFSET FOR THE OPER
OPER_OFFSET = 6
OPER_LEN = 2

# OFFSET FOR THE ECHO_SRC_VID
ECHO_SRC_OFFSET = 8

# ROUND_TIME this is the waiting time for each round in number of seconds
ROUND_TIME = 5

# TIME INTERVAL FOR PERIODIC ECHO REQUESTS SENT BY THE VEIL MASTER IN SECONDS
ECHO_REQUEST_INTERVAL = 20000

# OPERATION NUMBER TO STRING MAPPING
OPERATION_NAMES = {
    0x01:   "ARP_REQUEST",
    0x02:   "ARP_REPLY",
    0x03:   "R_ARP_REQUEST",
    0x04:   "R_ARP_REPLY",
    0x0500: "ECHO_REQUEST",
    0X0600: "ECHO_REPLY",
    0X0700: "STORE_REQUEST",
    0X0800: "STORE_REPLY",
    0x0900: 'SWITCH_REGISTRATION_REQUEST',
    0X0A00: 'SWITCH_REGISRATION_REPLY',
    0x1000: 'RDV_PUBLISH',
    0x2000: 'RDV_QUERY',
    0x3000: 'RDV_REPLY'}
