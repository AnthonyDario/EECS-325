import socket
from struct import *

# some useful protocol definitions
icmp = socket.getprotobyname('icmp')
udp = socket.getprotobyname('udp')
name = 'google.com'
ttl = 255
addr = socket.gethostbyname(name)

# we will listen on port 33434, like traceroute
port = 33434

# create our sockets
recv_socket = socket.socket(socket.AF_INET, socket.SOCK_RAW, icmp)
send_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, udp)

# change the fields
send_socket.setsockopt(socket.SOL_IP, socket.IP_TTL, ttl)

# bind the sockets
recv_socket.bind(("", port))
send_socket.sendto("", (addr, port))

# get the data
data , address  = recv_socket.recvfrom(512)

# parse the data first 20 characters are the ip header
ip_header = data[0:20]
iph = unpack('BBHHHBBH4s4s', ip_header)

packet_ttl = iph[5]

print 'TTL: ' + str(packet_ttl)


