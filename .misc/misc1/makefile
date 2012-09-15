SERVER_OBJS = server.o cpu.o headers.o
CLIENT_OBJS = client.o cpu.o headers.o
CC = g++
DEBUG = -g
GLIBTOP_INC = -I/usr/include/glib-2.0 -I/usr/lib/glib-2.0/include -I/usr/include/libgtop-2.0 -I/usr/lib/x86_64-linux-gnu/glib-2.0/include
GLIBTOP_LIB = -L/usr/lib -L/usr/lib/x86_64-linux-gnu
CFLAGS = -Wall $(GLIBTOP_INC) -c $(DEBUG)
LFLAGS = -Wall $(GLIBTOP_LIB) -pthread -lgtop-2.0 -lglib-2.0 $(DEBUG)

all : server client reader

server : $(SERVER_OBJS)
	$(CC) server.o -o server $(LFLAGS)

client : $(CLIENT_OBJS)
	$(CC) client.o -o client $(LFLAGS)
	
reader: reader.o cpu.o headers.o
	$(CC) reader.o -o reader $(LFLAGS)
	
reader.o : ../src/reader.cpp ../src/cpu.cpp ../src/headers.cpp
	$(CC) $(CFLAGS) ../src/reader.cpp

server.o : ../src/server.cpp ../src/cpu.cpp ../src/headers.cpp
	$(CC) $(CFLAGS) ../src/server.cpp

client.o : ../src/client.cpp ../src/cpu.cpp ../src/headers.cpp
	$(CC) $(CFLAGS) ../src/client.cpp

cpu.o : ../src/cpu.cpp ../src/headers.cpp
	$(CC) $(CFLAGS) ../src/cpu.cpp

headers.o : ../src/headers.cpp
	$(CC) $(CFLAGS) ../src/headers.cpp
	
clean:
	\rm *.o *~ server client -f

#tar:
#	tar cfv p1.tar server.cpp client.cpp die.h headers.cpp makefile
