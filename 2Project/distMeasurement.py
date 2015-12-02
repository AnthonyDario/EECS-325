import time
import select
import socket
import errno
import csv
from struct import *

id_num = 0

# prints out the RTT and number of hops to get to an address
def get_time(addr, times_checked):

    global id_num
    id_num += 1
    icmp = socket.getprotobyname('icmp')
    udp = socket.getprotobyname('udp')
    ttl = 32
    timeout = 3

    # we will listen on port 33434, like traceroute
    port = 33434

    # create our sockets
    recv_socket = socket.socket(socket.AF_INET, socket.SOCK_RAW, icmp)
    send_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, udp)

    # change the fields
    send_socket.setsockopt(socket.SOL_IP, socket.IP_TTL, ttl)

    # bind the socket
    recv_socket.bind(("", port))

    # for the select call
    incoming = [ recv_socket ]
    outgoing = [] 
    potential_errors = {}

    start = time.time()
    send_socket.sendto(str(id_num), (addr, port))

    readable, writable, errors = \
        select.select(incoming, outgoing, potential_errors, timeout)

    end = time.time()
    if len(readable) == 0:
        # we couldn't get any data try rechecking
        print 'could not get data from socket: ' + addr +\
              ' on attempt ' + str(3 - times_checked)
        if times_checked > 0:
            get_time(addr, times_checked - 1)
        else:
            output.writerow(
                {
                 'IP': addr, 
                 'RTT': 'N/A', 
                 'Hops': 'N/A'
                })
    else:
        
        data, address = recv_socket.recvfrom(512)

        # parsing the data 
        icmp_header = data[20:28]
        our_ip_header = data[28:48]
        contents = data[56:]

        iph = unpack('BBHHHBBH4s4s', our_ip_header)
        icmph = unpack('bbHHh', icmp_header)
        response_id = unpack(str(len(contents)) + 's', contents)[0]

        packet_ttl = iph[5] 
        icmp_type, icmp_code, _, _, _ = icmph

        # if the response ID isn't correct then we have not recieved the packet
        if response_id and response_id != str(id_num):
            print 'did not get a packet with the proper ID'
            output.writerow(
                {
                 'IP': address[0], 
                 'RTT': 'N/A', 
                 'Hops': 'N/A'
                })

        # icmp respons of 3 3 will return our IP header which is what
        elif icmp_type != 3 and icmp_code != 3:
            print 'Did not recieve proper response'
        else:

            # try to find hostname for printing purposes
            try:
                print 'resolving hostname...'
                hostname = socket.gethostbyaddr(address[0])[0]
            except socket.error:
                hostname = 'unknown'

            print address[0] + ' : ' + hostname 
            print '\tnumber of hops: ' + str(ttl - packet_ttl)
            print '\tTime: ' + str((end - start) * 1000) + 'ms'

            output.writerow(
                {
                 'IP': address[0], 
                 'RTT': (end - start) * 1000, 
                 'Hops': ttl - packet_ttl
                })

    send_socket.close()
    recv_socket.close()

# file stuff
addresses = open('targets.txt', 'r')
fieldnames = [ 'IP', 'RTT', 'Hops' ]
output = csv.DictWriter(open('times.csv', 'w'), fieldnames=fieldnames)
output.writeheader()

for address in addresses:

    print '\ncalling on address: ' + address.rstrip()
    address = socket.gethostbyname(address.rstrip())
    get_time(address, 2)
