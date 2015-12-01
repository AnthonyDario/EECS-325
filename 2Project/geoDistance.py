import urllib2

addresses = open('targets.txt', 'r')

for address in addresses:
    address.rstrip()
    content = urllib2.urlopen("http://freegeoip.net/json/" + address).read()
    print content

