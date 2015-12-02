import urllib2
import json

addresses = open('targets.txt', 'r')

rad_earth = 6371000

location = json.loads(urllib2.urlopen("http://freegeoip.net/json/").read())
lat = location['latitude']
lon = location['longitude']

print lat
print lon

for address in addresses:
    address.rstrip()
    content = (urllib2.urlopen("http://freegeoip.net/json/" + address).read())
    data = json.loads(content)
    print data['ip']
    print '\tlatitude: ' + str(data['latitude'])
    print '\tlongitude: ' + str(data['longitude'])

