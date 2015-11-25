import socket

# create the socket
#try:
#    s = socket.socket(socket.AF_INET, socket.SOCK_RAW, socket.IPPROTO_RAW)
#except socket.error , msg:
#    print 'Socket could not be created. Error Code : ' + str(msg[0]) + \
#          ' Message ' + msg[1]
#sys.exit()

def main(dest_name):

    # some useful vriables
    dest_addr = socket.gethostbyname(dest_name)
    port = 33434
    icmp = socket.getprotobyname('icmp')
    udp = socket.getprotobyname('udp')
    ttl = 1
    max_hops = 30

    while True:

        # create the sockets
        recv_socket = socket.socket(socket.AF_INET, socket.SOCK_RAW, icmp)
        send_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, udp)
        send_socket.setsockopt(socket.SOL_IP, socket.IP_TTL, ttl)

        # bind the recv socket to listen for all hosts on our port
        # bind the send socket to send to our destination
        recv_socket.bind(("", port))
        send_socket.sendto("", (dest_name, port))

        # try and get data from the address
        curr_addr = None
        try:
            # only want the address not the data
            _, curr_addr = recv_socket.recvfrom(512)
            curr_addr = curr_addr[0]

            # get the name
            try:
                curr_name = socket.gethostbyaddr(curr_addr)[0]
            except socket.error:
                curr_name = curr_addr
        except socket.error:
            pass
        finally:
            send_socket.close()
            recv_socket.close()

        if curr_addr is not None:
            curr_host = '%s %s' % (curr_name, curr_addr)
        else:
            curr_host = '*'
        
        print '%d\t%s' % (ttl, curr_host)

        ttl += 1
        if curr_addr == dest_addr or ttl > max_hops:
            break

if __name__ == '__main__':
    main('google.com')
