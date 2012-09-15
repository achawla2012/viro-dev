JCC = javac

JFLAGS = -g

default: Packet.class clientHandler.class SocketMonitor.class veil_switch.class

Packet.class: Packet.java
	$(JCC) $(JFLAGS) Packet.java

clientHandler.class: clientHandler.java
	$(JCC) $(JFLAGS) clientHandler.java

SocketMonitor.class: SocketMonitor.java
	$(JCC) $(JFLAGS) SocketMonitor.java
 
veil_switch.class: veil_switch.java
	$(JCC) $(JFLAGS) veil_switch.java

clean: 
	$(RM) *.class
