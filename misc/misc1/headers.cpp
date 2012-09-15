#ifndef HEADERS_H
#define HEADERS_H

#include <iostream>

#include <sys/socket.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <time.h>

#include <stdio.h>
#include <glib.h>
#include <glibtop.h>
#include <glibtop/cpu.h>
#include <glibtop/mem.h>
#include <glibtop/proclist.h>
#include <glibtop/netlist.h>
#include <glibtop/netload.h>
 
using namespace std;


typedef unsigned short ip_port;
typedef struct sockaddr_in ip_address;
typedef struct sockaddr address;
typedef unsigned char uchar;

const int MAX_BUFFER_SIZE = 5000;
const int MAX_IFACES = 10;
const int MAX_IFACE_NAME_LEN = 20;

int DieWithError(const char *msg, const char * fname = NULL, const int lnumber = 0) {
	printf("%s --- %s --- %d\n", msg, fname, lnumber);
	return 0;
}

#endif
