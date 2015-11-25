import time
import socket
from struct import *

# file stuff
addresses = open('targets.txt', 'r')

for addr in addresses:

    # some useful variables
    icmp = socket.getprotobyname('icmp')
    udp = socket.getprotobyname('udp')
    ttl = 255
    addr = addr.rstrip()

    # we will listen on port 33434, like traceroute
    port = 33434

    # create our sockets
    recv_socket = socket.socket(socket.AF_INET, socket.SOCK_RAW, icmp)
    send_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, udp)

    # change the fields
    send_socket.setsockopt(socket.SOL_IP, socket.IP_TTL, ttl)

    # bind the socket
    recv_socket.bind(("", port))

    # get the data
    start = time.time()
    send_socket.sendto("", (addr, port))
    data , address  = recv_socket.recvfrom(512)
    end = time.time()

    # parse the data first 20 characters are the ip header
    ip_header = data[0:20]
    iph = unpack('BBHHHBBH4s4s', ip_header)

    packet_ttl = iph[5]

    # we have the TTL from the source, how to figure out actual ttl...
    try:
        hostname = socket.gethostbyaddr(address[0])[0]
    except socket.error:
        hostname = 'unknown'
    print hostname 
    print '\tTTL: ' + str(packet_ttl)
    print '\tTime: ' + str(end - start)

