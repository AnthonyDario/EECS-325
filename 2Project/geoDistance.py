import urllib2
import json
from math import radians, sqrt, sin, cos, atan2

addresses = open('targets.txt', 'r')

RAD_EARTH = 6372.8

location = json.loads(urllib2.urlopen("http://freegeoip.net/json/").read())
lat = radians(location['latitude'])
lon = radians(location['longitude'])

print lat
print lon

for address in addresses:
    address.rstrip()
    content = (urllib2.urlopen("http://freegeoip.net/json/" + address).read())
    data = json.loads(content)

    addr_lat = radians(data['latitude'])
    addr_lon = radians(data['longitude'])

    dlon = (lon - addr_lon)

    y = sqrt(
        (cos(addr_lat) * sin(dlon)) ** 2 
        + (cos(lat) * sin(addr_lat) - sin(lat) * cos(addr_lat) * cos(dlon)) ** 2
        )

    x = sin(lat) * sin(addr_lat) + cos(lat) * cos(addr_lat) * cos(dlon)
    c = atan2(y, x)

    dist = RAD_EARTH * c

    print data['ip']
    print '\tlatitude: ' + str(data['latitude'])
    print '\tlongitude: ' + str(data['longitude'])
    print '\tdistance: ' + str(dist)

