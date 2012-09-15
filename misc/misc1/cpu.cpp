#include "headers.cpp"

// To compile: gcc -Wall -g cpu.c -I/usr/include/glib-2.0 -I/usr/lib/glib-2.0/include -I/usr/include/libgtop-2.0 -I/usr/lib/x86_64-linux-gnu/glib-2.0/include/ -lgtop-2.0 -lglib-2.0 -o cpu

#include <string>

#ifndef STAT_H
#define STAT_H

typedef struct {
	glibtop_cpu cpu;
	glibtop_mem mem;
	glibtop_proclist proc;
	char ifaces[MAX_IFACES][MAX_IFACE_NAME_LEN];
	glibtop_netlist netlist;
	glibtop_netload net[MAX_IFACES];
} data;

class Statistics {
private:
	
public:	
	data d;

	
	void collect() {
		glibtop_init();

		glibtop_get_cpu (&this->d.cpu);
		glibtop_get_mem(&this->d.mem);

		gint64 which = GLIBTOP_KERN_PROC_ALL, arg = 0;
		pid_t *pids = glibtop_get_proclist(&this->d.proc, which, arg);
		g_free(pids);

		char **ifaces = glibtop_get_netlist(&this->d.netlist);
		for(uint i = 0; i < this->d.netlist.number; i++) {
			strcpy(this->d.ifaces[i], ifaces[i]);
			glibtop_get_netload(&this->d.net[i], this->d.ifaces[i]);
			delete [] ifaces[i];
		}
		delete [] ifaces;		
	}
	

	void print() {
		printf(	"CPU TYPE INFORMATIONS\n"
				"\tCpu Total : %ld \n"
				"\tCpu User : %ld \n"
				"\tCpu Nice : %ld \n"
				"\tCpu Sys : %ld \n"
				"\tCpu Idle : %ld \n"
				"\tCpu Frequences : %ld \n",
				(unsigned long)d.cpu.total,
				(unsigned long)d.cpu.user,
				(unsigned long)d.cpu.nice,
				(unsigned long)d.cpu.sys,
				(unsigned long)d.cpu.idle,
				(unsigned long)d.cpu.frequency);

		printf(	"\nMEMORY USING\n"
				"\tMemory Total : %ld MB\n"
				"\tMemory Used : %ld MB\n"
				"\tMemory Free : %ld MB\n"
				"\tMemory Shared : %ld MB\n"				
				"\tMemory Buffered : %ld MB\n"
				"\tMemory Cached : %ld MB\n"
				"\tMemory user : %ld MB\n"
				"\tMemory Locked : %ld MB\n",
				(unsigned long)d.mem.total/(1024*1024),
				(unsigned long)d.mem.used/(1024*1024),
				(unsigned long)d.mem.free/(1024*1024),
				(unsigned long)d.mem.shared/(1024*1024),
				(unsigned long)d.mem.buffer/(1024*1024),
				(unsigned long)d.mem.cached/(1024*1024),
				(unsigned long)d.mem.user/(1024*1024),
				(unsigned long)d.mem.locked/(1024*1024));

		printf(	"\nProc List Number : %ld\nProc List Total : %ld\nProc List Size : %ld\n\n",
				(unsigned long)d.proc.number,
				(unsigned long)d.proc.total,
				(unsigned long)d.proc.size);
		
		for(uint i = 0; i < this->d.netlist.number; i++) {
			cout << d.ifaces[i] << endl;
		}
		
	}
};

#endif
