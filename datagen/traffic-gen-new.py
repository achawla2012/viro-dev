#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Pengkui Luo <pengkui.luo@gmail.com>
#
# Created: Wed April 10 19:40:04 2011
# Updated: 2012-05-21
# Version: 0.2.2
#
"""
Traffic Generator for VIRO emulator

This script simulates all hosts attached to switch <my_ip:my_port>.
It scans the <workload_file>, forms data packets destined to switches specified
in <workload_file>, and injects the packets to its attached switch.

Usage: traffic-gen.py <my_ip:my_port>
where my_ip:my_port denotes the VIRO switch that this generator attaches to.

vid_file format: each line (starting from line 2) is
<pid> <vid>\n
e.g. "localhost:8001 011110\n"

workload_file format: each line (starting from line 2) is
<src_switch_pid> <dst_switch_pid> <rate_by_pkts_per_sec>\n
I assume that the order pair <src_pid, dst_pid> is unique in each file.

"""
import sys
import os
import socket
from threading import Thread
from random import expovariate, uniform
from time import sleep
from struct import pack

from constants import HTYPE, PTYPE, HLEN, PLEN

# Constants
# Note: I assume that for a fixed time-interval, a workload file is provided.
VID_FILE = 'vid.txt'
WORKLOAD_INTERVAL = 300  # in seconds
WORKLOAD_FILES = ['workload.00.txt', 'workload.01.txt', 'workload.02.txt']

# Global shared dictionaries: 'Packet' and 'DstVid2Rate':
#     'Packet' maps dst_vid to a pre-constructed dummpy packet;
#     'DstVid2Rate' maps dst_vid to the traffic rates.
# Both are read by multiple 'traffic_generator' threads concurrently,
# and updated by the 'workload_updater' thread with lock periodically.
Packet = {}
DstVid2Rate = {}
# Single TCP connection shared by all threads
Conn = socket.socket(socket.AF_INET, socket.SOCK_STREAM)


def traffic_generator(dst_vid):
    """
    Threading worker for generating traffic. It keeps injecting dummy
    packets to the attached switch.
    """
    global Packet, DstVid2Rate, Conn
    while True:
        try:
            Conn.send(Packet[dst_vid])
        except:
            print('Cannot send to %s. Exiting...' % dst_vid)
            sys.exit(1)
        try:
            rate = DstVid2Rate[dst_vid]
            sleep(expovariate(1.0 / rate))  # Poisson process, exp interval
        except:
            break  # if dst_vid not in DstVid2Rate, then exit this thread


#==============================================================================
# Main script
#==============================================================================
try:
    my_pid = sys.argv[1]  # e.g. 'localhost:8001'
    print('my_pid=%s' % my_pid)
    my_ip, my_pt = my_pid.split(':')
    print('my_ip=%s, my_pt=%s' % (my_ip, my_pt))
    Conn.connect((my_ip, int(my_pt)))
except:
    print('Failed to connect to %s:%s. Exiting.' % (my_ip, my_pt))
    sys.exit(1)
print ('succ')
sys.exit(0)
Conn.close()

# Parse VID_FILE, and store the pid-to-vid mapping in dict pid2vid
# Line format: <pid> <vid>\n
sleep(uniform(0, 1))
pid2vid = {}  # mapping pid to vid
with file(VID_FILE) as fin:
    for lineno, line in enumerate(fin):
        if lineno > 0:  # skip the first line
            pid, vid = line.strip().split(' ')
            pid2vid[pid] = vid


# Periodically parsing new WORKLOAD_FILES, and constructing new dummy packets
for workload_file in WORKLOAD_FILES:
    print('Loading %s' % workload_file)

    dst_vid_to_rate = {}  # mapping dst_vid to traffic rate
    with open(workload_file) as fin:
        for lineno, line in enumerate(fin):
            if lineno > 0:  # skip the first line
                src_pid, dst_pid, data_rate = line.strip().split(' ')
                if src_pid == my_pid:  # only parse traffic sourced at me
                    dst_vid = pid2vid[dst_pid]
                    dst_vid_to_rate[dst_vid] = float(data_rate)
    new_dst_vids = set(dst_vid_to_rate.keys()) - set(DstVid2Rate.keys())
    DstVid2Rate = dst_vid_to_rate

    for dst_vid in new_dst_vids:
        '''
        Construct a static/dummy data packet destined to dst_vid
        note: B:8bit, H:16bit, I:32bit.
        reference: http://docs.python.org/library/struct.html
            0x0000 for data pkts, 32bit src_vid, 32bit dst_vid,
            32bit fwd_vid (initialized to dst_vid),
            8bit TTL (initialized to 64)
        '''
        if dst_vid not in Packet:
            # Pre-construct a dummy packet for each destination
            Packet[dst_vid] \
                = pack('!HHBBH', HTYPE, PTYPE, HLEN, PLEN, 0x0000)\
                + pack('!I', int(pid2vid[my_pid], 2))\
                + pack('!I', int(dst_vid,2))\
                + pack('!I', int(dst_vid,2))\
                + pack('!B', 64)\
                + pack('!BHI', 0x00, 0x0000, 0x00000000)

        t = Thread(target=traffic_generator,
                   name='GenTrafficTo:%s' % dst_vid,
                   args=(dst_vid, ))
        t.setDaemon(True)  # burn if the main is killed
        t.start()

    sleep(WORKLOAD_INTERVAL)
print('All WORKLOAD_FILES have been read and parsed.')
DstVid2Rate = {}