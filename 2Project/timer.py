import socket
import sys

# create the socket
#try:
#    s = socket.socket(socket.AF_INET, socket.SOCK_RAW, socket.IPPROTO_RAW)
#except socket.error , msg:
#    print 'Socket could not be created. Error Code : ' + str(msg[0]) + \
#          ' Message ' + msg[1]
#sys.exit()

def main(dest):
    dest_addr = socket.gethostbyname(dest)

    icmp = socket.getprotobyname('icmp')
    udp = socket.getprototbyname('udp')
    while True:
        recv_socket = socket.socket(socket.AF_INET, socket.SOCK_RAW, icmp)
        send_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, udp)

        ttl += 1
        pass


if __name__ == __main__:
    main('google.com')
