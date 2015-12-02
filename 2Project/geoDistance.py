import urllib2
import json
import csv
import socket
from math import radians, sqrt, sin, cos, atan2

RAD_EARTH = 6372.8

# file stuff
fieldnames = [ 'IP', 'distance' ]
addresses = open('targets.txt', 'r')
output = csv.DictWriter(open('distances.csv', 'w'), fieldnames=fieldnames)
output.writeheader()

# get the users location
location = json.loads(urllib2.urlopen("http://freegeoip.net/json/").read())
lat = radians(location['latitude'])
lon = radians(location['longitude'])

print 'your latitude: ' + str(lat)
print 'your longitude: ' + str(lon)

for location in addresses:
    location = location.rstrip()
    address = socket.gethostbyname(location)
    content = (urllib2.urlopen("http://freegeoip.net/json/" + address).read())
    data = json.loads(content)

    # the latitude and longitude of the destination
    addr_lat = radians(data['latitude'])
    addr_lon = radians(data['longitude'])

    dlon = (lon - addr_lon)

    # haversine formula
    y = sqrt(
        (cos(addr_lat) * sin(dlon)) ** 2 
        + (cos(lat) * sin(addr_lat) - sin(lat) * cos(addr_lat) * cos(dlon)) ** 2
        )

    x = sin(lat) * sin(addr_lat) + cos(lat) * cos(addr_lat) * cos(dlon)
    c = atan2(y, x)

    dist = RAD_EARTH * c

    output.writerow(
        { 
         'IP': address,
         'distance': dist
        })

    print data['ip']
    print '\tlatitude: ' + str(data['latitude'])
    print '\tlongitude: ' + str(data['longitude'])
    print '\tdistance: ' + str(dist) + 'km'

