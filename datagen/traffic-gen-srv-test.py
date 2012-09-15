import socket, sys

srv = socket.socket (socket.AF_INET, socket.SOCK_STREAM)
srv.setsockopt (socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
srv.bind (('', 5001))
srv.listen (20)
print 'server started'
while 1:
    cx, addr = srv.accept()
    ip = str(addr[0])
    print 'ip =', ip
    sys.stdout.flush()
