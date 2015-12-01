import time
import select
import socket
import errno
from struct import *

id = 0

def get_time(addr, times_checked):

    global id
    icmp = socket.getprotobyname('icmp')
    udp = socket.getprotobyname('udp')
    ttl = 32
    timeout = 1

    # we will listen on port 33434, like traceroute
    port = 33434

    # create our sockets
    recv_socket = socket.socket(socket.AF_INET, socket.SOCK_RAW, icmp)
    send_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, udp)
    recv_socket.setblocking(0)

    # change the fields
    send_socket.setsockopt(socket.SOL_IP, socket.IP_TTL, ttl)

    # bind the socket
    recv_socket.bind((str(id), port))

    # for the select call
    incoming = [ recv_socket ]
    outgoing = [] 
    potential_errors = {}

    start = time.time()

    readable, writable, errors = \
        select.select(incoming, outgoing, potential_errors, timeout)

    send_socket.sendto("", (addr, port))
    print 'sent to ' + addr

    if len(readable) == 0:
        # we couldn't get any data try rechecking
        print 'could not get data from socket: ' + addr +\
              ' on attempt ' + str(3 - times_checked)
        if times_checked > 0:
            get_time(addr, times_checked - 1)

    else:
        data, address = recv_socket.recvfrom(1024)
        end = time.time()

        # parse the data first 20 characters are the ip header
        icmp_header = data[20:28]
        our_ip_header = data[28:48]
        iph = unpack('BBHHHBBH4s4s', our_ip_header)
        icmph = unpack('bbHHh', icmp_header)

        packet_ttl = iph[5] 
        icmp_type, icmp_code, _, _, _ = icmph

        # we have the TTL from the source, how to figure out actual ttl...
        try:
            hostname = socket.gethostbyaddr(address[0])[0]
        except socket.error:
            hostname = 'unknown'
        print address[0] + ' : ' + hostname 
        print '\tcode: ' + str(icmp_code)
        print '\ttype: ' + str(icmp_type)
        print '\tnumber of hops: ' + str(ttl - packet_ttl)
        print '\tTTL: ' + str(packet_ttl)
        print '\tTime: ' + str((end - start) * 1000)

        readable, writable, errors = \
            select.select(incoming, outgoing, potential_errors, timeout)

        # in case an address sends back multiple packets for whatever reason
        while len(readable) == 1:
            recv_socket.recvfrom(1024)
            readable, writable, errors = \
                select.select(incoming, outgoing, potential_errors, timeout)

    send_socket.close()
    recv_socket.close()

# match
# graphs
# write a report

# file stuff
addresses = open('targets.txt', 'r')

for address in addresses:

    address = address.rstrip()
    print '\ncalling on address: ' + address
    get_time(address, 2)
